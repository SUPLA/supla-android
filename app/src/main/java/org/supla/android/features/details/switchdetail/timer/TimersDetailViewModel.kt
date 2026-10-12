package org.supla.android.features.details.switchdetail.timer
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

import android.content.Context
import android.text.format.DateFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.RuntimeStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.extensions.onlineState
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.isAutomaticTimeSyncDisabled
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.DeviceConfigEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.relayschedule.data.RelayProgramInfoBuilder
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.views.channelstate.ChannelCurrentData
import org.supla.android.ui.views.channelstate.ChannelProgramData
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.StartTimerUseCase
import org.supla.android.usecases.client.StartTimerUseCase.InvalidTimeException
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.relay.SuplaRelayFlag
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import timber.log.Timber
import java.util.Date
import java.util.Optional
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TimersDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val runtimeStateHolder: RuntimeStateHolder,
  private val startTimerUseCase: StartTimerUseCase,
  private val dateProvider: DateProvider,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val deviceConfigEventsManager: DeviceConfigEventsManager,
  private val suplaClientProvider: SuplaClientProvider,
  @param:ApplicationContext private val context: Context,
  threading: SuplaThreading
) : BaseViewModel<TimersDetailViewState, TimersDetailViewEvent>(TimersDetailViewState(), threading),
  TimerDetailViewScope {

  private var remoteId: Int = 0
  private var deviceId: Int = 0

  fun onViewCreated(remoteId: Int, deviceId: Int = 0) {
    this.remoteId = remoteId
    this.deviceId = deviceId

    updateState { it.copy(timerTimeInSeconds = runtimeStateHolder.getLastTimerValue(remoteId)) }

    observeData()
    askForConfigs()
  }

  override fun onStartTimer() {
    val state = currentState()
    if (!state.online || state.forceActive) return

    val action = state.targetAction ?: TimerTargetAction.TURN_ON
    runtimeStateHolder.setLastTimerValue(remoteId, state.timerTimeInSeconds)
    startTimer(action == TimerTargetAction.TURN_ON, state.timerTimeInSeconds)

    updateState { it.copy(editMode = false) }
  }

  override fun stopTimer() {
    abortCompletable(remoteId, currentState().timerData?.timerValue == TimerValue.ON)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("stopTimer($remoteId)"))
      .disposeBySelf()
  }

  override fun cancelTimer() {
    abortCompletable(remoteId, currentState().timerData?.timerValue == TimerValue.OFF)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("cancelTimer($remoteId)"))
      .disposeBySelf()
  }

  fun startEditMode() {
    updateState { it.copy(editMode = true) }
  }

  override fun startEditMode(leftTimeInSecs: Int) {
    runtimeStateHolder.setLastTimerValue(remoteId, leftTimeInSecs)
    updateState { it.copy(editMode = true, timerTimeInSeconds = leftTimeInSecs) }
  }

  override fun cancelEditMode() {
    updateState { it.copy(editMode = false) }
  }

  override fun updateAction(action: TimerTargetAction) {
    updateState { it.copy(targetAction = action) }
  }

  override fun updateTimerTime(timeInSeconds: Int) {
    runtimeStateHolder.setLastTimerValue(remoteId, timeInSeconds)
    updateState { it.copy(timerTimeInSeconds = timeInSeconds) }
  }

  override fun onTimerFinished() {
  }

  fun calculateProgressViewData(startTime: Date, endTime: Date): ProgressViewData {
    val leftTime = calculateLeftTime(endTime)
    val wholeTime = endTime.time.minus(startTime.time)

    return ProgressViewData(
      progress = 1 - leftTime.div(wholeTime.toFloat()),
      leftTimeValues = TimeValues.of(leftTime.div(1000).plus(1))
    )
  }

  private fun startTimer(turnOn: Boolean, durationInSecs: Int) {
    startTimerUseCase(remoteId, turnOn, durationInSecs)
      .attach()
      .subscribeBy(
        onError = {
          if (it is InvalidTimeException) {
            sendEvent(TimersDetailViewEvent.ShowInvalidTimeToast)
          } else {
            defaultErrorHandler("startTimer($remoteId, $turnOn, $durationInSecs)")(it)
          }
        }
      )
      .disposeBySelf()
  }

  private fun calculateLeftTime(endTime: Date) = endTime.time.minus(dateProvider.currentTimestamp())

  private fun abortCompletable(remoteId: Int, turnOn: Boolean): Completable {
    val actionId = if (turnOn) ActionId.TURN_ON else ActionId.TURN_OFF
    return executeSimpleActionUseCase(actionId, SubjectType.CHANNEL, remoteId)
  }

  private fun observeData() {
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
      readChannelByRemoteIdUseCase(remoteId).subscribeOn(threading.schedulers.io),
      weeklySchedule,
      deviceConfig,
      Observable.interval(1, TimeUnit.SECONDS, threading.schedulers.computation).startWithItem(0L)
    ) { channel, schedule, device, _ ->
      Triple(channel, schedule.orElse(null), device.orElse(null)?.isAutomaticTimeSyncDisabled() ?: false)
    }
      .observeOn(threading.schedulers.ui)
      .map { (channel, schedule, syncDisabled) -> handleChannel(channel, schedule, syncDisabled) }
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { viewState -> updateState { viewState } },
        onError = defaultErrorHandler("observeData($remoteId, $deviceId)")
      )
      .disposeBySelf()
  }

  private fun askForConfigs() {
    suplaClientProvider.provide()?.let { client ->
      client.getChannelConfig(remoteId, ChannelConfigType.WEEKLY_SCHEDULE)
      if (deviceId != 0) client.getDeviceConfig(deviceId)
    }
  }

  private fun handleChannel(
    channelDataEntity: ChannelDataEntity,
    weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig?,
    timeSyncDisabled: Boolean
  ): TimersDetailViewState {
    val currentTime = dateProvider.currentDate()
    val timerState = channelDataEntity.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue
    val isTimerOn = timerState != null && timerState.countdownEndsAt?.after(currentTime) == true
    val startDate = channelDataEntity.channelExtendedValueEntity?.timerStartTime
    Timber.d("Handling channel update $timerState")

    val state = currentState()
    val isOn = channelDataEntity.channelValueEntity.asRelayValue().on
    val targetAction = state.targetAction
      ?: if (state.editMode && isOn) {
        TimerTargetAction.TURN_OFF
      } else {
        TimerTargetAction.TURN_ON
      }

    return state.copy(
      icon = getChannelIconUseCase(channelDataEntity),
      online = channelDataEntity.channelValueEntity.onlineState.online,
      on = isOn,
      forceActive = channelDataEntity.channelValueEntity.asRelayValue().mode in listOf(
        SuplaRelayMode.FORCED_ON,
        SuplaRelayMode.FORCED_OFF
      ),
      deviceStateData = ChannelCurrentData(
        label = deviceStateLabel(currentTime, timerState?.countdownEndsAt),
        icon = getChannelIconUseCase(channelDataEntity),
        value = deviceStateValue(channelDataEntity.channelValueEntity)
      ),
      programInfo = if (channelDataEntity.channelValueEntity.onlineState.online &&
        !isTimerOn &&
        channelDataEntity.channelValueEntity.asRelayValue().flags.contains(SuplaRelayFlag.WEEKLY_SCHEDULE_ENABLED)
      ) {
        weeklyScheduleConfig?.let { RelayProgramInfoBuilder(it, dateProvider).build(timeSyncDisabled) }.orEmpty()
      } else {
        emptyList()
      },
      timerData = if (isTimerOn) {
        TimerProgressData(
          endTime = timerState.countdownEndsAt!!,
          startTime = startDate ?: currentTime,
          indeterminate = startDate == null,
          timerValue = if (timerState.expectedHiValue()) TimerValue.OFF else TimerValue.ON
        )
      } else {
        null
      },
      targetAction = targetAction,
    )
  }

  private fun deviceStateLabel(currentDate: Date, endDate: Date?): LocalizedString =
    if (endDate != null && endDate.after(currentDate)) {
      val timeFormat = context.getString(R.string.hour_string_format)
      localizedString(R.string.details_timer_state_label_for_timer, DateFormat.format(timeFormat, endDate))
    } else {
      localizedString(R.string.details_timer_state_label)
    }

  private fun deviceStateValue(channelValue: ChannelValueEntity): LocalizedString =
    when {
      !channelValue.onlineState.online -> localizedString(R.string.offline)
      channelValue.asRelayValue().on -> localizedString(R.string.details_timer_device_on)
      else -> localizedString(R.string.details_timer_device_off)
    }
}

sealed class TimersDetailViewEvent : ViewEvent {
  object ShowInvalidTimeToast : TimersDetailViewEvent()
}

data class TimersDetailViewState(
  val online: Boolean = false,
  val forceActive: Boolean = false,
  val on: Boolean = false,
  val timerData: TimerProgressData? = null,
  val deviceStateData: ChannelCurrentData? = null,
  val programInfo: List<ChannelProgramData> = emptyList(),
  val editMode: Boolean = false,
  val targetAction: TimerTargetAction? = null,
  val timerTimeInSeconds: Int = 0,
  val icon: ImageId? = null
) : ViewState()

data class TimerProgressData(
  val startTime: Date,
  val endTime: Date,
  val indeterminate: Boolean,
  val timerValue: TimerValue
)

data class ProgressViewData(
  val progress: Float,
  val leftTimeValues: TimeValues
)

enum class TimerValue {
  ON, OFF
}

data class TimeValues(val hours: Int, val minutes: Int, val seconds: Int) {
  companion object {
    fun of(time: Long) = TimeValues(
      seconds = (time % 60).toInt(),
      minutes = ((time / 60) % 60).toInt(),
      hours = (time / 3600).toInt()
    )
  }
}
