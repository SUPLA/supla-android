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
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.actiontrigger.SuplaChannelActionTriggerConfig
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
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
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
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
  private var temporaryUnlockActive: Boolean = false
  private var currentTemporaryUnlockDurationS: Int? = null
  private var defaultTemporaryUnlockDurationS: Int? = null

  fun observeData(remoteId: Int, deviceId: Int) {
    this.remoteId = remoteId

    val weeklySchedule = channelConfigEventsManager.observerConfig(remoteId)
      .filter { it.result == ConfigResult.RESULT_TRUE && it.config is SuplaChannelWeeklyScheduleConfig }
      .map { Optional.of(it.config as SuplaChannelWeeklyScheduleConfig) }
      .startWithItem(Optional.empty<SuplaChannelWeeklyScheduleConfig>())
      .distinctUntilChanged()

    val defaultChannelConfig = channelConfigEventsManager.observerConfig(remoteId)
      .filter { it.result == ConfigResult.RESULT_TRUE && it.config is SuplaChannelActionTriggerConfig }
      .map { Optional.of(it.config as SuplaChannelActionTriggerConfig) }
      .startWithItem(Optional.empty<SuplaChannelActionTriggerConfig>())
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
      defaultChannelConfig,
      deviceConfig,
      Observable.interval(2, TimeUnit.SECONDS, threading.schedulers.computation).startWithItem(0L)
    ) { channelWithChildren, schedule, defaultConfig, device, _ ->
      defaultTemporaryUnlockDurationS = defaultConfig.orElse(null)?.temporaryUnlockDurationSec?.toInt() ?: 0
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
    if (mode == SuplaButtonMode.LOCKED) {
      return
    } else if (mode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED && !temporaryUnlockActive) {
      return
    } else if (mode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
      performAtAction(SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED)
    } else {
      performAtAction(SuplaButtonMode.LOCKED)
    }
  }

  override fun onUnlock() {
    if (mode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
      updateState { state ->
        val unlockDuration = currentTemporaryUnlockDurationS ?: defaultTemporaryUnlockDurationS ?: 0
        state.copy(
          unlockDialog = UnlockDialogState(
            temporaryUnlockDurationS = unlockDuration,
            unlockTemporaryAllowed = unlockDuration > 0
          ),
        )
      }
    } else {
      performAtAction(SuplaButtonMode.NOT_SET)
    }
  }

  override fun onUnlockDialogDurationChange(value: Int) {
    updateState { state ->
      val newValue = value.coerceIn(0, 64_800)
      state.copy(
        unlockDialog = state.unlockDialog?.copy(
          temporaryUnlockDurationS = newValue,
          unlockTemporaryAllowed = newValue > 0
        ),
      )
    }
  }

  override fun onUnlockDialogDismiss() {
    updateState { it.copy(unlockDialog = null) }
  }

  override fun onUnlockDialogUnlock() {
    performAtAction(SuplaButtonMode.NOT_SET)
    onUnlockDialogDismiss()
  }

  override fun onUnlockDialogTemporaryUnlock() {
    currentState().unlockDialog?.temporaryUnlockDurationS?.let { seconds ->
      performAtAction(SuplaButtonMode.CMD_TEMPORARY_UNLOCK, seconds)
    }
    onUnlockDialogDismiss()
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

  private fun performAtAction(mode: SuplaButtonMode, durationSec: Int? = null) {
    val currentRemoteId = remoteId ?: return

    executeActionTriggerActionUseCase(SubjectType.CHANNEL, currentRemoteId, mode, durationSec?.toLong())
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
      ActionTriggerProgramInfoBuilder(scheduleConfig, dateProvider, actionTriggerValue).build(timeSyncDisabled)
    } else {
      emptyList()
    }
    temporaryUnlockActive = actionTriggerValue.temporaryUnlockActive
    val lockButtonState = lockButtonState(actionTriggerValue.buttonMode, weeklyActive, temporaryUnlockActive, offline)
    val showAllowTemporaryUnlock =
      SuplaChannelFlag.BUTTON_TEMPORARY_UNLOCK_SUPPORTED inside channel.flags &&
        actionTriggerValue.buttonMode in setOf(
          SuplaButtonMode.LOCKED,
          SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED
        )

    mode = actionTriggerValue.buttonMode
    currentTemporaryUnlockDurationS = weeklyActive.forTrue { scheduleConfig?.let(::currentProgramTemporaryUnlockDuration) }

    return ActionTriggerGeneralViewState(
      deviceStateData = currentStateData(weeklyActive, temporaryUnlockActive, actionTriggerValue.buttonMode, offline),
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
      showTemporaryUnlockIcon = lockButtonState.showTemporaryUnlockIcon,
      unlockDialog = currentState().unlockDialog
    )
  }

  private fun currentProgramTemporaryUnlockDuration(scheduleConfig: SuplaChannelWeeklyScheduleConfig): Int? {
    val currentEntry = scheduleConfig.schedule.firstOrNull { entry ->
      entry.dayOfWeek == dateProvider.currentDayOfWeek() &&
        entry.hour == dateProvider.currentHour() &&
        entry.quarterOfHour == QuarterOfHour.from(dateProvider.currentMinute())
    } ?: return null

    return scheduleConfig.programConfigurations
      .firstOrNull { it.program == currentEntry.program }
      ?.buttonTemporaryUnlockDurationS
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

  internal fun lockButtonState(
    buttonMode: SuplaButtonMode,
    weeklyActive: Boolean = false,
    temporaryUnlockActive: Boolean,
    offline: Boolean = false
  ): ActionTriggerLockButtonState {
    val buttonState = when (buttonMode) {
      SuplaButtonMode.LOCKED -> ActionTriggerLockButtonState(
        lockActive = !temporaryUnlockActive,
        unlockActive = temporaryUnlockActive,
        showTemporaryUnlockIcon = false
      )
      SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED -> ActionTriggerLockButtonState(
        lockActive = !temporaryUnlockActive,
        unlockActive = temporaryUnlockActive,
        showTemporaryUnlockIcon = true
      )
      else -> ActionTriggerLockButtonState(
        lockActive = false,
        unlockActive = true,
        showTemporaryUnlockIcon = false
      )
    }
    return buttonState.copy(
      lockDisabled = offline || weeklyActive,
      unlockDisabled = offline || (weeklyActive && buttonMode != SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED)
    )
  }

  internal fun currentStateData(
    weeklyActive: Boolean,
    temporaryUnlockActive: Boolean = false,
    mode: SuplaButtonMode = SuplaButtonMode.NOT_SET,
    offline: Boolean = false
  ): ChannelCurrentData? =
    if (offline) {
      ChannelCurrentData(
        icon = null,
        label = LocalizedString.WithFormat("%s:", listOf(localizedString(R.string.action_trigger_schedule_button_mode))),
        value = localizedString(R.string.offline)
      )
    } else if (weeklyActive) {
      null
    } else {
      ChannelCurrentData(
        icon = null,
        label = LocalizedString.WithFormat("%s:", listOf(localizedString(R.string.action_trigger_schedule_button_mode))),
        value = currentModeLabel(mode, temporaryUnlockActive)
      )
    }

  private fun currentModeLabel(mode: SuplaButtonMode, temporaryUnlockActive: Boolean) =
    when (mode) {
      SuplaButtonMode.LOCKED -> localizedString(R.string.general_locked)
      SuplaButtonMode.NOT_SET -> localizedString(R.string.general_unlocked)
      SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED -> localizedString(
        temporaryUnlockActive.forTrue { R.string.action_trigger_temporary_unlocked } ?: R.string.general_locked
      )
      SuplaButtonMode.CMD_WEEKLY_SCHEDULE,
      SuplaButtonMode.CMD_SWITCH_TO_MANUAL,
      SuplaButtonMode.CMD_TEMPORARY_UNLOCK -> LocalizedString.Constant(NO_VALUE_TEXT)
    }

  private fun requestConfig(remoteId: Int, deviceId: Int) {
    suplaClientProvider.provide()?.let { client ->
      client.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
      client.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
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
