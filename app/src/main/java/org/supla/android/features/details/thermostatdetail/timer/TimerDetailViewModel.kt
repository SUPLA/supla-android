package org.supla.android.features.details.thermostatdetail.timer
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
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.PublishSubject
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.db.Channel
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.DAY_IN_SEC
import org.supla.android.extensions.HOUR_IN_SEC
import org.supla.android.extensions.dayEnd
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.days
import org.supla.android.extensions.differenceInSeconds
import org.supla.android.extensions.fromSuplaTemperature
import org.supla.android.extensions.getTimerStateValue
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.hour
import org.supla.android.extensions.hoursInDay
import org.supla.android.extensions.ifTrue
import org.supla.android.extensions.minutesInHour
import org.supla.android.extensions.secondsInMinute
import org.supla.android.extensions.setHour
import org.supla.android.extensions.shift
import org.supla.android.extensions.yearNo
import org.supla.android.features.details.thermostatdetail.timer.ui.TimerDetailViewProxy
import org.supla.android.features.details.thermostatdetail.ui.TimerHeaderState
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteThermostatActionUseCase
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TimerDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val executeThermostatActionUseCase: ExecuteThermostatActionUseCase,
  private val suplaClientProvider: SuplaClientProvider,
  private val dateProvider: DateProvider,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val valuesFormatter: ValuesFormatter,
  schedulers: SuplaSchedulers
) : BaseViewModel<TimerDetailViewState, TimerDetailViewEvent>(TimerDetailViewState(), schedulers), TimerDetailViewProxy {

  private val channelSubject: PublishSubject<Channel> = PublishSubject.create()

  override val timerLeftTime: Int?
    get() {
      val state = currentState()
      val (timerEndTime) = guardLet(state.timerEndDate) { return null }
      val currentDate = dateProvider.currentDate()

      return if (currentDate.after(timerEndTime)) {
        null
      } else {
        timerEndTime.time.minus(currentDate.time).div(1000).toInt()
      }
    }

  override fun onViewCreated() {
    loadingTimeoutManager.watch({ currentState().loadingState }) {
      updateState { state ->
        state.remoteId?.let {
          loadData(it)
        }

        state.copy(loadingState = state.loadingState.changingLoading(false, dateProvider))
      }
    }.disposeBySelf()
  }

  fun loadData(remoteId: Int) {
    readChannelByRemoteIdUseCase.invoke(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = { channelSubject.onNext(it.getLegacyChannel()) },
        onError = defaultErrorHandler("observeData($remoteId)")
      )
      .disposeBySelf()
    suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
  }

  fun observeData(remoteId: Int) {
    Observable.combineLatest(
      channelSubject.hide(),
      channelConfigEventsManager.observerConfig(remoteId)
        .filter { it.config is SuplaChannelHvacConfig && it.result == ConfigResult.RESULT_TRUE }
    ) { channel, config -> Pair(channel, config) }
      .attachSilent()
      .subscribeBy(
        onNext = { handleData(it.first, it.second) },
        onError = defaultErrorHandler("observeData($remoteId)")
      )
      .disposeBySelf()
  }

  override fun toggleSelectorMode() {
    updateState {
      it.copy(showCalendar = it.showCalendar.not())
    }
  }

  override fun toggleDeviceMode(deviceMode: DeviceMode) {
    updateState {
      it.copy(selectedMode = deviceMode)
    }
  }

  override fun onDateChanged(selectedDateMillis: Long?) {
    updateState { state ->
      state.copy(calendarValue = selectedDateMillis?.let { Date(it) })
    }
  }

  override fun onTimeChanged(hour: Hour) {
    updateState { it.copy(calendarTimeValue = hour, showTimePicker = false) }
  }

  override fun onTimePickerDismiss() {
    updateState { it.copy(showTimePicker = false) }
  }

  override fun onTimeClicked() {
    updateState { it.copy(showTimePicker = true) }
  }

  override fun onTimerDaysChange(days: Int) {
    updateState {
      it.copy(timerDays = days)
    }
  }

  override fun onTimerHoursChange(hours: Int) {
    updateState { it.copy(timerHours = hours) }
  }

  override fun onTimerMinutesChange(minutes: Int) {
    updateState { it.copy(timerMinutes = minutes) }
  }

  override fun onTemperatureChange(temperature: Float) {
    updateState { it.copy(currentTemperature = temperature) }
  }

  override fun onTemperatureChange(step: TemperatureCorrection) {
    updateState { it.copy(currentTemperature = it.currentTemperature?.plus(step.step())) }
  }

  override fun onStartTimer() {
    val state = currentState()

    val (remoteId) = guardLet(state.remoteId) { return }
    val (duration) = guardLet(state.getTimerDuration(dateProvider.currentDate())) { return }

    updateState {
      it.copy(
        loadingState = it.loadingState.changingLoading(true, dateProvider),
        editTime = false
      )
    }

    val mode = if (state.selectedMode == DeviceMode.OFF) {
      SuplaHvacMode.OFF
    } else if (state.usingHeatSetpoint) {
      SuplaHvacMode.HEAT
    } else {
      SuplaHvacMode.COOL
    }
    val sendTemperature = state.selectedMode == DeviceMode.MANUAL

    executeThermostatActionUseCase.invoke(
      type = SubjectType.CHANNEL,
      remoteId = remoteId,
      mode = mode,
      setpointTemperatureHeat = (sendTemperature && state.usingHeatSetpoint).ifTrue(state.currentTemperature),
      setpointTemperatureCool = (sendTemperature && state.usingHeatSetpoint.not()).ifTrue(state.currentTemperature),
      durationInSec = duration.toLong()
    ).attachSilent()
      .subscribe()
      .disposeBySelf()
  }

  override fun cancelTimerStartManual() {
    val (remoteId) = guardLet(currentState().remoteId) { return }

    updateState { it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider)) }

    executeThermostatActionUseCase.invoke(
      type = SubjectType.CHANNEL,
      remoteId = remoteId,
      mode = SuplaHvacMode.CMD_SWITCH_TO_MANUAL
    ).attachSilent()
      .subscribe()
      .disposeBySelf()
  }

  override fun cancelTimerStartProgram() {
    val (remoteId) = guardLet(currentState().remoteId) { return }

    updateState { it.copy(loadingState = it.loadingState.changingLoading(true, dateProvider)) }

    executeThermostatActionUseCase.invoke(
      type = SubjectType.CHANNEL,
      remoteId = remoteId,
      mode = SuplaHvacMode.CMD_WEEKLY_SCHEDULE
    ).attachSilent()
      .subscribe()
      .disposeBySelf()
  }

  override fun editTimer() {
    updateState {
      if (it.timerEndDate != null && it.currentDate != null) {
        val timeDiff = it.timerEndDate.differenceInSeconds(it.currentDate)
        it.copy(
          editTime = true,
          timerDays = timeDiff.days,
          timerHours = timeDiff.hoursInDay,
          timerMinutes = timeDiff.minutesInHour,
          calendarValue = it.timerEndDate,
          calendarTimeValue = it.timerEndDate.hour(),
          selectedMode = if (it.currentMode == SuplaHvacMode.OFF) DeviceMode.OFF else DeviceMode.MANUAL
        )
      } else {
        it.copy(editTime = true)
      }
    }
  }

  override fun editTimerCancel() {
    updateState { it.copy(editTime = false) }
  }

  override fun formatLeftTime(leftTime: Int?): StringProvider {
    if (leftTime == null) {
      return { "" }
    }

    val days = leftTime.div(DAY_IN_SEC)
    val timeString = valuesFormatter.getTimeString(
      hour = leftTime.hoursInDay,
      minute = leftTime.minutesInHour,
      second = leftTime.secondsInMinute
    )

    if (days > 0) {
      return { context ->
        val daysString = context.resources.getQuantityString(R.plurals.day_pattern, days, days)
        "$daysString\n$timeString"
      }
    }

    return { timeString }
  }

  private fun handleData(channel: Channel, config: ChannelConfigEventsManager.ConfigEvent) {
    val (hvacConfig) = guardLet(config.config as? SuplaChannelHvacConfig) { return }
    val currentDate = dateProvider.currentDate()
    val timerState = channel.getTimerStateValue()
    val thermostatValue = channel.value.asThermostatValue()
    val isTimerOn = timerState != null && timerState.countdownEndsAt != null && timerState.countdownEndsAt.after(currentDate)

    val (configMinTemperature, configMaxTemperature) = guardLet(
      hvacConfig.temperatures.roomMin?.fromSuplaTemperature(),
      hvacConfig.temperatures.roomMax?.fromSuplaTemperature()
    ) { return }

    val initialCalendarDate = currentDate.shift(7)
    updateState {
      it.copy(
        remoteId = channel.remoteId,
        currentMode = thermostatValue.mode,
        currentDate = currentDate,
        calendarValue = initialCalendarDate,
        calendarTimeValue = initialCalendarDate.hour(),
        isTimerOn = isTimerOn,
        isChannelOnline = channel.onLine,
        timerEndDate = if (isTimerOn) timerState?.countdownEndsAt else null,

        subfunction = thermostatValue.subfunction,
        minTemperature = configMinTemperature,
        maxTemperature = configMaxTemperature,
        currentTemperature = getSetpointTemperature(channel, thermostatValue),
        usingHeatSetpoint = useHeatSetpoint(channel, thermostatValue),

        loadingState = it.loadingState.changingLoading(false, dateProvider)
      )
    }
  }

  private fun getSetpointTemperature(channel: Channel, thermostatValue: ThermostatValue): Float? {
    return when (channel.func) {
      SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ->
        thermostatValue.setpointTemperatureHeat

      SUPLA_CHANNELFNC_HVAC_THERMOSTAT ->
        if (thermostatValue.subfunction == ThermostatSubfunction.HEAT) {
          thermostatValue.setpointTemperatureHeat
        } else {
          thermostatValue.setpointTemperatureCool
        }

      else -> null
    }
  }

  private fun useHeatSetpoint(channel: Channel, thermostatValue: ThermostatValue): Boolean {
    return channel.func == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ||
      (channel.func == SUPLA_CHANNELFNC_HVAC_THERMOSTAT && thermostatValue.subfunction == ThermostatSubfunction.HEAT)
  }
}

data class TimerDetailViewState(
  val remoteId: Int? = null,
  val currentMode: SuplaHvacMode? = null,
  val currentDate: Date? = null,
  val channelFunction: Int? = null,
  val subfunction: ThermostatSubfunction? = null,
  val minTemperature: Float? = null,
  val maxTemperature: Float? = null,
  val currentTemperature: Float? = null,
  val usingHeatSetpoint: Boolean = false,
  var loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),

  val selectedMode: DeviceMode = DeviceMode.OFF,
  val isTimerOn: Boolean = false,
  val isChannelOnline: Boolean = false,
  val editTime: Boolean = false,
  val showCalendar: Boolean = false,
  val showTimePicker: Boolean = false,

  // Timer state
  val timerDays: Int = 0,
  val timerHours: Int = 3,
  val timerMinutes: Int = 0,

  // Calendar state
  val calendarValue: Date? = null,
  val calendarTimeValue: Hour? = null,

  // In progress state
  val timerEndDate: Date? = null

) : ViewState(), TimerHeaderState {

  // Temperature
  val temperaturesRange: ClosedFloatingPointRange<Float>
    get() {
      val (min, max) = guardLet(minTemperature, maxTemperature) { return 0f..0f }
      return min..max
    }

  val temperatureSteps: Int
    get() {
      val (min, max) = guardLet(minTemperature, maxTemperature) { return 0 }
      return max.minus(min).times(10).toInt()
    }

  // Timer
  private val timerValue: Int
    get() = timerMinutes.times(60)
      .plus(timerHours.times(HOUR_IN_SEC))
      .plus(timerDays.times(DAY_IN_SEC))

  // Calendar
  val yearsRange: IntRange
    get() = currentDate?.let {
      IntRange(it.yearNo, it.yearNo + 1)
    } ?: Date().let { IntRange(it.yearNo, it.yearNo + 1) }

  val dateValidator: (Date) -> Boolean
    get() = { date ->
      currentDate?.let {
        val finalDate = it.shift(365).dayEnd()

        date.after(it.dayStart()) && date.before(finalDate)
      } ?: true
    }

  val timerInfoText: StringProvider
    get() {
      val (timeDiff) = guardLet(getTimerDuration(currentDate)) { return { "" } }

      val days = timeDiff.days
      val hours = timeDiff.hoursInDay
      val minutes = timeDiff.minutesInHour

      return { context ->
        val daysString = context.resources.getQuantityString(R.plurals.day_pattern, days, days)
        val hoursString = context.resources.getQuantityString(R.plurals.hour_pattern, hours, hours)
        val minutesString = context.resources.getQuantityString(R.plurals.minute_pattern, minutes, minutes)
        val timeString = "$daysString $hoursString $minutesString"

        when {
          selectedMode == DeviceMode.OFF ->
            context.getString(R.string.details_timer_info_thermostat_off, timeString)

          channelFunction == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ||
            subfunction == ThermostatSubfunction.HEAT ->
            context.getString(R.string.details_timer_info_thermostat_heating, timeString)

          else -> context.getString(R.string.details_timer_info_thermostat_cooling, timeString)
        }
      }
    }

  override val endDateText: StringProvider
    get() = TimerHeaderState.endDateText(timerEndDate)

  override val currentStateIcon: Int?
    get() = TimerHeaderState.currentStateIcon(currentMode)

  override val currentStateIconColor: Int
    get() = TimerHeaderState.currentStateIconColor(currentMode)

  override val currentStateValue: StringProvider
    get() = TimerHeaderState.currentStateValue(currentMode, currentTemperature, currentTemperature)

  val startEnabled: Boolean =
    isChannelOnline && getTimerDuration(Date())?.let { it > 0 } ?: false

  val thumbIcon: Int
    get() {
      return if (channelFunction == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ||
        subfunction == ThermostatSubfunction.HEAT
      ) {
        R.drawable.ic_heat
      } else {
        R.drawable.ic_cool
      }
    }

  val thumbColor: Int
    get() {
      return if (channelFunction == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ||
        subfunction == ThermostatSubfunction.HEAT
      ) {
        R.color.red
      } else {
        R.color.blue
      }
    }

  fun getTimerDuration(date: Date?): Int? {
    return if (showCalendar) {
      val (currentDate) = guardLet(date) { return null }
      val (calendarDate) = guardLet(calendarValue) { return null }
      val (calendarHour) = guardLet(calendarTimeValue) { return null }

      val dateDateWithHour = calendarDate.setHour(calendarHour.hour, calendarHour.minute, 0)
      if (dateDateWithHour.before(currentDate)) {
        null
      } else {
        dateDateWithHour.differenceInSeconds(currentDate)
      }
    } else {
      timerValue
    }
  }
}

sealed class TimerDetailViewEvent : ViewEvent
