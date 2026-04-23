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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.DAY_IN_SEC
import org.supla.android.extensions.HOUR_IN_SEC
import org.supla.android.extensions.dayEnd
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.days
import org.supla.android.extensions.differenceInSeconds
import org.supla.android.extensions.hour
import org.supla.android.extensions.hoursInDay
import org.supla.android.extensions.minutesInHour
import org.supla.android.extensions.secondsInMinute
import org.supla.android.extensions.setHour
import org.supla.android.extensions.shift
import org.supla.android.extensions.subscribeBy
import org.supla.android.extensions.yearNo
import org.supla.android.features.details.thermostatdetail.timer.ui.ThermostatTimerViewScope
import org.supla.android.features.details.thermostatdetail.ui.TimerHeaderState
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteThermostatActionUseCase
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TimerDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val executeThermostatActionUseCase: ExecuteThermostatActionUseCase,
  private val suplaClientProvider: SuplaClientProvider,
  private val dateProvider: DateProvider,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  @param:Named(FORMATTER_THERMOMETER) private val thermometerValueFormatter: ValueFormatter,
  schedulers: SuplaSchedulers
) : BaseViewModel<TimerDetailViewState, TimerDetailViewEvent>(TimerDetailViewState(thermometerValueFormatter), schedulers),
  ThermostatTimerViewScope {

  private val channelSubject: PublishSubject<ChannelDataEntity> = PublishSubject.create()

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
        onSuccess = { channelSubject.onNext(it) },
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
      it.copy(
        selectedMode = deviceMode,
        currentTemperatureString = it.temperature?.toString(deviceMode, thermometerValueFormatter)
      )
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
    updateState {
      val newTemperature = when (it.temperature) {
        is SetpointTemperature.Heat -> SetpointTemperature.Heat(temperature)
        is SetpointTemperature.Cool -> SetpointTemperature.Cool(temperature)
        is SetpointTemperature.HeatAndCool -> when (it.selectedMode) {
          DeviceMode.HEATING -> it.temperature.copy(heat = temperature)
          DeviceMode.COOLING -> it.temperature.copy(cool = temperature)
          else -> null
        }
        else -> null
      }

      it.copy(
        temperature = newTemperature,
        currentTemperatureString = newTemperature?.toString(it.selectedMode, thermometerValueFormatter)
      )
    }
  }

  override fun onTemperatureChange(range: ClosedFloatingPointRange<Float>) {
    updateState {
      val lastCool = it.temperature?.setpointCool ?: 0f
      val lastTouchOnHeat = lastCool == range.endInclusive
      val temperature = SetpointTemperature.HeatAndCool(
        heat = range.start,
        cool = range.endInclusive,
        lastTouchOnHeat = lastTouchOnHeat
      )

      it.copy(
        temperature = temperature,
        currentTemperatureString = temperature.toString(it.selectedMode, thermometerValueFormatter)
      )
    }
  }

  override fun onTemperatureChange(step: TemperatureCorrection) {
    updateState {
      val temperature = when (val temperature = it.temperature) {
        is SetpointTemperature.Heat -> SetpointTemperature.Heat(temperature.value + step.step())
        is SetpointTemperature.Cool -> SetpointTemperature.Cool(temperature.value + step.step())
        is SetpointTemperature.HeatAndCool ->
          when (it.selectedMode) {
            DeviceMode.AUTO ->
              if (temperature.lastTouchOnHeat) {
                temperature.copy(heat = temperature.heat + step.step())
              } else {
                temperature.copy(cool = temperature.cool + step.step())
              }
            DeviceMode.HEATING -> it.temperature.copy(heat = it.temperature.heat + step.step())
            DeviceMode.COOLING -> it.temperature.copy(cool = it.temperature.cool + step.step())
            else -> null
          }
        else -> null
      }
      it.copy(
        temperature = temperature,
        currentTemperatureString = temperature?.toString(it.selectedMode, thermometerValueFormatter)
      )
    }
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

    val mode = when {
      state.selectedMode == DeviceMode.OFF -> SuplaHvacMode.OFF
      state.temperature is SetpointTemperature.Heat -> SuplaHvacMode.HEAT
      state.temperature is SetpointTemperature.Cool -> SuplaHvacMode.COOL
      state.temperature is SetpointTemperature.HeatAndCool ->
        when (state.selectedMode) {
          DeviceMode.AUTO -> SuplaHvacMode.HEAT_COOL
          DeviceMode.HEATING -> SuplaHvacMode.HEAT
          DeviceMode.COOLING -> SuplaHvacMode.COOL
          else -> null
        }
      else -> null
    }
    val sendTemperature = state.selectedMode == DeviceMode.MANUAL

    executeThermostatActionUseCase.invoke(
      type = SubjectType.CHANNEL,
      remoteId = remoteId,
      mode = mode,
      setpointTemperatureHeat = sendTemperature.ifTrue(state.temperature?.setpointHeat),
      setpointTemperatureCool = sendTemperature.ifTrue(state.temperature?.setpointCool),
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
          calendarTimeValue = it.timerEndDate.hour()
        )
      } else {
        it.copy(editTime = true)
      }
    }
  }

  override fun editTimerCancel() {
    updateState { it.copy(editTime = false) }
  }

  override fun formatLeftTime(leftTime: Int?): LocalizedString {
    if (leftTime == null) {
      return LocalizedString.Empty
    }

    val days = leftTime.div(DAY_IN_SEC)
    val timeString = ValuesFormatter.getTimeString(
      hour = leftTime.hoursInDay,
      minute = leftTime.minutesInHour,
      second = leftTime.secondsInMinute
    )

    if (days > 0) {
      return localizedString("%s %s", LocalizedString.Quantity(R.plurals.day_pattern, days), LocalizedString.Constant(timeString))
    }

    return LocalizedString.Constant(timeString)
  }

  private fun handleData(channel: ChannelDataEntity, config: ChannelConfigEventsManager.ConfigEvent) {
    val (hvacConfig) = guardLet(config.config as? SuplaChannelHvacConfig) { return }
    val currentDate = dateProvider.currentDate()
    val timerState = channel.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue
    val thermostatValue = channel.channelValueEntity.asThermostatValue()
    val isTimerOn = timerState != null && timerState.countdownEndsAt?.after(currentDate) == true
    val (minTemperature, maxTemperature) = guardLet(hvacConfig.minTemperature, hvacConfig.maxTemperature) { return }

    val initialCalendarDate = currentDate.shift(7)
    val temperature = getSetpointTemperature(channel, thermostatValue)

    updateState {
      it.copy(
        remoteId = channel.remoteId,
        currentMode = thermostatValue.mode,
        currentDate = currentDate,
        channelFunction = channel.function.value,
        calendarValue = initialCalendarDate,
        calendarTimeValue = initialCalendarDate.hour(),
        isTimerOn = isTimerOn,
        isChannelOnline = channel.status.online,
        timerEndDate = if (isTimerOn) timerState.countdownEndsAt else null,

        subfunction = thermostatValue.subfunction,
        minTemperature = minTemperature,
        maxTemperature = maxTemperature,
        temperature = temperature,
        currentTemperatureString = temperature?.toString(it.selectedMode, thermometerValueFormatter),

        loadingState = it.loadingState.changingLoading(false, dateProvider)
      )
    }
  }

  private fun getSetpointTemperature(channel: ChannelDataEntity, thermostatValue: ThermostatValue): SetpointTemperature? {
    return when (channel.function) {
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER ->
        SetpointTemperature.Heat(thermostatValue.setpointTemperatureHeat)
      SuplaFunction.HVAC_THERMOSTAT ->
        if (thermostatValue.subfunction == ThermostatSubfunction.HEAT) {
          SetpointTemperature.Heat(thermostatValue.setpointTemperatureHeat)
        } else {
          SetpointTemperature.Cool(thermostatValue.setpointTemperatureCool)
        }
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL -> {
        SetpointTemperature.HeatAndCool(
          heat = thermostatValue.setpointTemperatureHeat,
          cool = thermostatValue.setpointTemperatureCool,
          lastTouchOnHeat = false
        )
      }
      else -> null
    }
  }
}

data class TimerDetailViewState(
  val thermometerValueFormatter: ValueFormatter,
  val remoteId: Int? = null,
  val currentMode: SuplaHvacMode? = null,
  val currentDate: Date? = null,
  val channelFunction: Int? = null,
  val subfunction: ThermostatSubfunction? = null,
  val minTemperature: Float? = null,
  val maxTemperature: Float? = null,
  val temperature: SetpointTemperature? = null,
  val currentTemperatureString: String? = null,
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

  val timerInfoText: LocalizedString
    get() {
      val (timeDiff) = guardLet(getTimerDuration(currentDate)) { return LocalizedString.Empty }

      val days = timeDiff.days
      val hours = timeDiff.hoursInDay
      val minutes = timeDiff.minutesInHour

      val daysString = LocalizedString.Quantity(R.plurals.day_pattern, days)
      val hoursString = LocalizedString.Quantity(R.plurals.hour_pattern, hours)
      val minutesString = LocalizedString.Quantity(R.plurals.minute_pattern, minutes)
      val timeString = localizedString("%s %s %s", daysString, hoursString, minutesString)

      return when (selectedMode) {
        DeviceMode.OFF ->
          localizedString(R.string.details_timer_info_thermostat_off, timeString)
        DeviceMode.MANUAL ->
          if (subfunction == ThermostatSubfunction.HEAT) {
            localizedString(R.string.details_timer_info_thermostat_heating, timeString)
          } else {
            localizedString(R.string.details_timer_info_thermostat_cooling, timeString)
          }
        DeviceMode.HEATING ->
          localizedString(R.string.details_timer_info_thermostat_cooling, timeString)
        DeviceMode.COOLING ->
          localizedString(R.string.details_timer_info_thermostat_heating, timeString)
        DeviceMode.AUTO ->
          localizedString(R.string.details_timer_info_thermostat_auto, timeString)
      }
    }

  override val endDateText: LocalizedString
    get() = TimerHeaderState.endDateText(timerEndDate)

  override val currentStateIcon: Int?
    get() = TimerHeaderState.currentStateIcon(currentMode)

  override val currentStateIconColor: Int
    get() = TimerHeaderState.currentStateIconColor(currentMode)

  override val currentStateValue: LocalizedString =
    TimerHeaderState.currentStateValue(
      currentMode,
      temperature?.setpointHeat,
      temperature?.setpointCool,
      thermometerValueFormatter
    )

  val startEnabled: Boolean =
    isChannelOnline && getTimerDuration(Date())?.let { it > 0 } ?: false

  val timerLeftTime: Int?
    get() {
      val (timerEndTime) = guardLet(timerEndDate) { return null }
      val currentDate = Date()

      return if (currentDate.after(timerEndTime)) {
        null
      } else {
        timerEndTime.time.minus(currentDate.time).div(1000).toInt()
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

sealed interface SetpointTemperature {
  val setpointHeat: Float?
  val setpointCool: Float?

  @get:ColorRes
  val thumbColorRes: Int

  @get:ColorRes
  val thumbIconRes: Int

  @get:ColorRes
  val activeSetpointColorRes: Int

  val availableModes: List<DeviceMode>
    get() = DeviceMode.defaultModes

  fun toString(deviceMode: DeviceMode?, valueFormatter: ValueFormatter): String

  data class Heat(val value: Float) : SetpointTemperature {
    override val setpointHeat: Float = value
    override val setpointCool: Float? = null
    override val thumbColorRes: Int = R.color.red
    override val thumbIconRes: Int = R.drawable.ic_heat
    override val activeSetpointColorRes: Int = thumbColorRes

    override fun toString(deviceMode: DeviceMode?, valueFormatter: ValueFormatter) = valueFormatter.format(value)
  }

  data class Cool(val value: Float) : SetpointTemperature {
    override val setpointHeat: Float? = null
    override val setpointCool: Float = value
    override val thumbColorRes: Int = R.color.secondary
    override val thumbIconRes: Int = R.drawable.ic_cool
    override val activeSetpointColorRes: Int = thumbColorRes

    override fun toString(deviceMode: DeviceMode?, valueFormatter: ValueFormatter) = valueFormatter.format(value)
  }

  data class HeatAndCool(val heat: Float, val cool: Float, val lastTouchOnHeat: Boolean) : SetpointTemperature {
    override val setpointHeat: Float = heat
    override val setpointCool: Float = cool
    override val thumbColorRes: Int = R.color.red
    override val thumbIconRes: Int = R.drawable.ic_heat

    @DrawableRes
    val secondThumbIconRes: Int = R.drawable.ic_cool

    @ColorRes
    val secondThumbColorRes: Int = R.color.secondary

    override val activeSetpointColorRes: Int = if (lastTouchOnHeat) thumbColorRes else secondThumbColorRes
    override val availableModes: List<DeviceMode>
      get() = DeviceMode.heatCoolModes

    override fun toString(deviceMode: DeviceMode?, valueFormatter: ValueFormatter): String =
      when (deviceMode) {
        DeviceMode.AUTO -> {
          val heat = valueFormatter.format(heat)
          val cool = valueFormatter.format(cool)

          "$heat - $cool"
        }
        DeviceMode.HEATING -> valueFormatter.format(heat)
        DeviceMode.COOLING -> valueFormatter.format(cool)
        else -> ""
      }
  }
}

sealed class TimerDetailViewEvent : ViewEvent
