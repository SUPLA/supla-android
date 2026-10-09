package org.supla.android.features.details.relayschedule
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import io.reactivex.rxjava3.core.Observable
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.isAutomaticTimeSyncDisabled
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.programinfo.ProgramInfo
import org.supla.android.features.details.relayschedule.data.RelayProgramInfoBuilder
import org.supla.android.features.details.relayschedule.ui.OperatingButtonsScope
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.buttons.LockIconType
import org.supla.android.usecases.client.ExecuteRelayActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.group.GroupWithChannels
import org.supla.android.usecases.group.ReadGroupWithChannelsUseCase
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag
import java.util.Optional
import java.util.concurrent.TimeUnit

abstract class BaseRelayGeneralViewModel<S : ViewState, E : ViewEvent>(
  defaultState: S,
  private val readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val executeRelayActionUseCase: ExecuteRelayActionUseCase,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val suplaClientProvider: SuplaClientProvider,
  protected val dateProvider: DateProvider,
  threading: SuplaThreading
) : BaseViewModel<S, E>(defaultState, threading), OperatingButtonsScope {

  private var remoteId: Int? = null
  private var itemType: ItemType? = null

  fun observeData(remoteId: Int, itemType: ItemType, deviceId: Int = 0) {
    this.remoteId = remoteId
    this.itemType = itemType

    when (itemType) {
      ItemType.CHANNEL -> {
        observeChannel(remoteId, deviceId)
        requestConfig(remoteId, deviceId)
      }
      ItemType.GROUP -> observeGroup(remoteId)
    }
  }

  override fun onManual() {
    performAction(ActionId.SWITCH_TO_MANUAL_MODE)
  }

  override fun onWeekly() {
    performAction(ActionId.SWITCH_TO_PROGRAM_MODE)
  }

  override fun onAuto() {
    performRelayAction(SuplaRelayMode.AUTOMATIC)
  }

  protected fun performAction(actionId: ActionId) {
    val currentRemoteId = remoteId ?: return
    val currentItemType = itemType ?: return
    performAction(actionId, currentItemType, currentRemoteId)
  }

  protected fun performAction(actionId: ActionId, itemType: ItemType, remoteId: Int) {
    executeSimpleActionUseCase(actionId, itemType.subjectType, remoteId)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("performAction($actionId, $itemType, $remoteId)"))
      .disposeBySelf()
  }

  protected fun performRelayAction(mode: SuplaRelayMode) {
    val currentRemoteId = remoteId ?: return
    val currentItemType = itemType ?: return
    executeRelayActionUseCase(currentItemType.subjectType, currentRemoteId, mode)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("performRelayAction($mode, $currentItemType, $currentRemoteId)"))
      .disposeBySelf()
  }

  protected abstract fun observeChannel(remoteId: Int, deviceId: Int)

  protected abstract fun handleGroup(groupWithChannels: GroupWithChannels)

  protected fun <T : Any> observeChannel(
    source: Observable<T>,
    remoteId: Int,
    deviceId: Int,
    buildState: (T, RelayScheduleContext) -> S
  ) {
    combineWithSchedule(source.subscribeOn(threading.schedulers.io), remoteId, deviceId)
      .observeOn(threading.schedulers.ui)
      .map { (data, schedule) -> buildState(data, schedule) }
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { state -> updateState { state } },
        onError = defaultErrorHandler("observeChannel($remoteId)")
      )
      .disposeBySelf()
  }

  private fun observeGroup(remoteId: Int) {
    readGroupWithChannelsUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onNext = this::handleGroup,
        onError = defaultErrorHandler("observeGroup($remoteId)")
      )
      .disposeBySelf()
  }

  protected data class RelayScheduleContext(
    val weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig?,
    val timeSyncDisabled: Boolean
  )

  private fun <T : Any> combineWithSchedule(
    source: Observable<T>,
    remoteId: Int,
    deviceId: Int
  ): Observable<Pair<T, RelayScheduleContext>> {
    val weeklySchedule = channelConfigEventsManager.observerConfig(remoteId)
      .filter { it.result == ConfigResult.RESULT_TRUE && it.config is SuplaChannelWeeklyScheduleConfig }
      .map { Optional.of(it.config as SuplaChannelWeeklyScheduleConfig) }
      .startWithItem(Optional.empty<SuplaChannelWeeklyScheduleConfig>())
      .distinctUntilChanged()

    return Observable.combineLatest(
      source,
      weeklySchedule,
      observeDeviceConfig(deviceId),
      Observable.interval(1, TimeUnit.MINUTES, threading.schedulers.computation).startWithItem(0L)
    ) { data, schedule, device, _ ->
      data to RelayScheduleContext(schedule.orElse(null), device.orElse(null)?.isAutomaticTimeSyncDisabled() ?: false)
    }
  }

  private fun requestConfig(remoteId: Int, deviceId: Int) {
    suplaClientProvider.provide()?.let { client ->
      client.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
      if (deviceId != 0) {
        client.getDeviceConfig(deviceId)
      }
    }
  }

  private fun observeDeviceConfig(deviceId: Int): Observable<Optional<SuplaDeviceConfig>> =
    if (deviceId == 0) {
      Observable.just(Optional.empty())
    } else {
      deviceConfigEventsManager.observerConfig(deviceId)
        .filter { it.result == ConfigResult.RESULT_TRUE && it.config is SuplaDeviceConfig }
        .map { Optional.of(it.config as SuplaDeviceConfig) }
        .startWithItem(Optional.empty<SuplaDeviceConfig>())
        .distinctUntilChanged()
    }

  protected val RelayValue.weeklyScheduleEnabled: Boolean
    get() = flags.contains(SuplaRelayFlag.WEEKLY_SCHEDULE_ENABLED)

  protected val RelayValue.forced: Boolean
    get() = mode == SuplaRelayMode.FORCED_ON || mode == SuplaRelayMode.FORCED_OFF

  protected fun lockIconType(value: RelayValue): LockIconType =
    when {
      value.forced && value.weeklyScheduleEnabled -> LockIconType.TEMPORARY_CLOSED
      value.forced -> LockIconType.CLOSED
      else -> LockIconType.OPENED
    }

  protected val ChannelDataEntity.forceSupported: Boolean
    get() = SuplaChannelFlag.RELAY_MODE_FORCED_SUPPORTED inside flags

  protected fun ChannelDataEntity.forceActive(value: RelayValue): Boolean = forceSupported && value.forced

  protected fun RelayScheduleContext.programInfo(offline: Boolean, value: RelayValue): List<ProgramInfo> =
    if (offline || !value.weeklyScheduleEnabled) {
      emptyList()
    } else {
      weeklyScheduleConfig?.let { RelayProgramInfoBuilder(it, dateProvider).build(timeSyncDisabled) }.orEmpty()
    }

  protected fun forceDeactivationAction(weeklyScheduleEnabled: Boolean): ActionId =
    if (weeklyScheduleEnabled) ActionId.SWITCH_TO_PROGRAM_MODE else ActionId.SWITCH_TO_MANUAL_MODE
}
