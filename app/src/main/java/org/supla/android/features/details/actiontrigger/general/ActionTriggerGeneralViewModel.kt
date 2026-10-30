package org.supla.android.features.details.actiontrigger.general
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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.label
import org.supla.android.data.source.remote.isAutomaticTimeSyncDisabled
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.detailbase.actiontrigger.ActionTriggerProgramInfoBuilder
import org.supla.android.features.details.relayschedule.OperatingMode
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.channelstate.ChannelCurrentData
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteActionTriggerActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.core.shared.data.model.channel.SuplaChannelFlag
import org.supla.core.shared.data.model.function.actiontrigger.SuplaActionTriggerFlag
import org.supla.core.shared.infrastructure.localizedString
import java.util.Optional
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ActionTriggerGeneralViewModel @Inject constructor(
  private val observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  private val executeActionTriggerActionUseCase: ExecuteActionTriggerActionUseCase,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val suplaClientProvider: SuplaClientProvider,
  private val dateProvider: DateProvider,
  threading: SuplaThreading
) : BaseViewModel<ActionTriggerGeneralViewState, ActionTriggerGeneralViewEvent>(ActionTriggerGeneralViewState(), threading),
  ActionTriggerGeneralScope {

  private var remoteId: Int? = null
  private var mode: SuplaButtonMode? = null

  fun observeData(remoteId: Int, deviceId: Int) {
    this.remoteId = remoteId

    val weeklySchedule = channelConfigEventsManager.observerConfig(remoteId)
      .filter { it.result == ConfigResult.RESULT_TRUE && it.config is SuplaChannelWeeklyScheduleConfig }
      .map { Optional.of(it.config as SuplaChannelWeeklyScheduleConfig) }
      .startWithItem(Optional.empty<SuplaChannelWeeklyScheduleConfig>())
      .distinctUntilChanged()

    val deviceConfig = if (deviceId == 0) {
      Observable.just(Optional.empty<SuplaDeviceConfig>())
    } else {
      deviceConfigEventsManager.observerConfig(deviceId)
        .filter { it.result == ConfigResult.RESULT_TRUE && it.config is SuplaDeviceConfig }
        .map { Optional.of(it.config as SuplaDeviceConfig) }
        .startWithItem(Optional.empty<SuplaDeviceConfig>())
        .distinctUntilChanged()
    }

    Observable.combineLatest(
      observeChannelWithChildrenUseCase(remoteId).subscribeOn(threading.schedulers.io),
      weeklySchedule,
      deviceConfig,
      Observable.interval(2, TimeUnit.SECONDS, threading.schedulers.computation).startWithItem(0L)
    ) { channelWithChildren, schedule, device, _ ->
      buildState(channelWithChildren.channel, schedule.orElse(null), device.orElse(null)?.isAutomaticTimeSyncDisabled() ?: false)
    }
      .observeOn(threading.schedulers.ui)
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { state -> updateState { state } },
        onError = defaultErrorHandler("observeData($remoteId)")
      )
      .disposeBySelf()

    requestConfig(remoteId, deviceId)
  }

  override fun onManual() {
    performAction(ActionId.SWITCH_TO_MANUAL_MODE)
  }

  override fun onWeekly() {
    performAction(ActionId.SWITCH_TO_PROGRAM_MODE)
  }

  override fun onAuto() {}

  override fun onLock() {
    performAtAction(SuplaButtonMode.LOCKED)
  }
  override fun onUnlock() {
    if (mode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
      performAtAction(SuplaButtonMode.CMD_TEMPORARY_UNLOCK)
    } else {
      performAtAction(SuplaButtonMode.NOT_SET)
    }
  }

  override fun onAllowTemporaryUnlockChanged(allow: Boolean) {
    if (allow) {
      performAtAction(SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED)
    } else {
      performAtAction(SuplaButtonMode.LOCKED)
    }
  }

  private fun performAction(actionId: ActionId) {
    val currentRemoteId = remoteId ?: return

    executeSimpleActionUseCase(actionId, SubjectType.CHANNEL, currentRemoteId)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("performAction($actionId, $currentRemoteId)"))
      .disposeBySelf()
  }

  private fun performAtAction(mode: SuplaButtonMode) {
    val currentRemoteId = remoteId ?: return

    executeActionTriggerActionUseCase(SubjectType.CHANNEL, currentRemoteId, mode)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("performRelayAction($mode, $currentRemoteId)"))
      .disposeBySelf()
  }

  private fun buildState(
    channel: ChannelDataEntity,
    scheduleConfig: SuplaChannelWeeklyScheduleConfig?,
    timeSyncDisabled: Boolean
  ): ActionTriggerGeneralViewState {
    val actionTriggerValue = channel.channelValueEntity.asAtValue()
    val weeklyActive = SuplaActionTriggerFlag.WEEKLY_SCHEDULE_ENABLED in actionTriggerValue.flags
    val offline = channel.status.offline
    val programInfo = if (!offline && weeklyActive && scheduleConfig != null) {
      ActionTriggerProgramInfoBuilder(scheduleConfig, dateProvider).build(timeSyncDisabled)
    } else {
      emptyList()
    }
    val lockButtonState = lockButtonState(actionTriggerValue.buttonMode, weeklyActive)
    val showAllowTemporaryUnlock =
      SuplaChannelFlag.BUTTON_TEMPORARY_UNLOCK_SUPPORTED inside channel.flags &&
        actionTriggerValue.buttonMode in setOf(
          SuplaButtonMode.LOCKED,
          SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED
        )

    mode = actionTriggerValue.buttonMode

    return ActionTriggerGeneralViewState(
      deviceStateData = currentStateData(weeklyActive, actionTriggerValue.buttonMode),
      programInfo = programInfo,
      operatingMode = operatingMode(channel.flags, weeklyActive),
      manualButtonDisabled = offline,
      weeklyButtonDisabled = offline,
      showLockButtons = SuplaChannelFlag.BUTTON_MODE_SUPPORTED inside channel.flags,
      showAllowTemporaryUnlock = showAllowTemporaryUnlock,
      allowTemporaryUnlock = actionTriggerValue.buttonMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED,
      allowTemporaryUnlockDisabled = weeklyActive,
      lockButtonActive = lockButtonState.lockActive,
      unlockButtonActive = lockButtonState.unlockActive,
      lockButtonDisabled = lockButtonState.lockDisabled,
      unlockButtonDisabled = lockButtonState.unlockDisabled,
      showTemporaryUnlockIcon = lockButtonState.showTemporaryUnlockIcon
    )
  }

  internal fun operatingMode(channelFlags: Long, weeklyActive: Boolean): OperatingMode? {
    val manualAllowed = true
    val weeklyAllowed = SuplaChannelFlag.WEEKLY_SCHEDULE inside channelFlags
    if (!manualAllowed || !weeklyAllowed) return null

    return OperatingMode(
      manualAllowed = manualAllowed,
      weeklyAllowed = true,
      autoAllowed = false,
      manualActive = !weeklyActive,
      weeklyActive = weeklyActive
    )
  }

  internal fun lockButtonState(buttonMode: SuplaButtonMode, weeklyActive: Boolean = false): ActionTriggerLockButtonState {
    val buttonState = when (buttonMode) {
      SuplaButtonMode.LOCKED -> ActionTriggerLockButtonState(
        lockActive = true,
        unlockActive = false,
        showTemporaryUnlockIcon = false
      )
      SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED -> ActionTriggerLockButtonState(
        lockActive = true,
        unlockActive = false,
        showTemporaryUnlockIcon = true
      )
      else -> ActionTriggerLockButtonState(
        lockActive = false,
        unlockActive = true,
        showTemporaryUnlockIcon = false
      )
    }
    return buttonState.copy(
      lockDisabled = weeklyActive,
      unlockDisabled = weeklyActive && buttonMode != SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED
    )
  }

  internal fun currentStateData(
    weeklyActive: Boolean,
    mode: SuplaButtonMode = SuplaButtonMode.NOT_SET
  ): ChannelCurrentData? =
    if (weeklyActive) {
      null
    } else {
      ChannelCurrentData(
        icon = null,
        label = localizedString(R.string.details_timer_state_label),
        value = mode.label
      )
    }

  private fun requestConfig(remoteId: Int, deviceId: Int) {
    suplaClientProvider.provide()?.let { client ->
      client.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
      if (deviceId != 0) {
        client.getDeviceConfig(deviceId)
      }
    }
  }
}

sealed interface ActionTriggerGeneralViewEvent : ViewEvent

internal data class ActionTriggerLockButtonState(
  val lockActive: Boolean,
  val unlockActive: Boolean,
  val showTemporaryUnlockIcon: Boolean,
  val lockDisabled: Boolean = false,
  val unlockDisabled: Boolean = false
)
