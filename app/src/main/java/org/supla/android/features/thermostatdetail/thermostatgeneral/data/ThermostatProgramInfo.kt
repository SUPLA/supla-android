package org.supla.android.features.thermostatdetail.thermostatgeneral.data
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
import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.isAutomaticTimeSyncDisabled
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.thermostatdetail.ui.OFF
import org.supla.android.features.thermostatdetail.ui.description

data class ThermostatProgramInfo(
  val type: Type,
  val time: StringProvider? = null,
  @DrawableRes val icon: Int?,
  @ColorRes val iconColor: Int?,
  val descriptionProvider: StringProvider?,
  val manualActive: Boolean = false
) {
  enum class Type(@StringRes val stringRes: Int) {
    CURRENT(R.string.thermostat_detail_program_current), NEXT(R.string.thermostat_detail_program_next)
  }

  class Builder {
    // external
    var dateProvider: DateProvider? = null
    var weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig? = null
    var deviceConfig: SuplaDeviceConfig? = null
    var thermostatFlags: List<SuplaThermostatFlags>? = null
    var currentMode: SuplaHvacMode? = null
    var currentTemperature: Float? = null
    var channelOnline: Boolean? = null

    // internal
    internal var currentDayOfWeek: DayOfWeek? = null
    internal var currentHour: Int? = null
    internal var currentMinute: Int? = null

    internal var foundCurrentProgram: SuplaScheduleProgram? = null
    internal var foundNextProgram: SuplaScheduleProgram? = null
    internal var quartersToNextProgram: Int? = null
  }
}

fun ThermostatProgramInfo.Builder.build(): List<ThermostatProgramInfo> {
  val (dateProvider) = guardLet(dateProvider) { throw IllegalStateException("Date provider cannot be null") }
  val (config) = guardLet(weeklyScheduleConfig) { throw IllegalStateException("Config cannot be null") }
  val (flags) = guardLet(thermostatFlags) { throw IllegalStateException("Thermostat flags cannot be null") }
  guardLet(currentMode) { throw IllegalStateException("Current mode cannot be null") }
  guardLet(currentTemperature) { throw IllegalStateException("Current temperature cannot be null") }
  val (isOnline) = guardLet(channelOnline) { throw IllegalStateException("Channel online cannot be null") }

  if (isOnline.not() || config.schedule.isEmpty() || config.programConfigurations.isEmpty()) {
    return emptyList()
  }
  if (flags.contains(SuplaThermostatFlags.WEEKLY_SCHEDULE).not()) {
    return emptyList()
  }
  if (flags.contains(SuplaThermostatFlags.CLOCK_ERROR)) {
    return clockErrorList()
  }
  currentDayOfWeek = dateProvider.currentDayOfWeek()
  currentHour = dateProvider.currentHour()
  currentMinute = dateProvider.currentMinute()

  identifyPrograms()

  if (quartersToNextProgram == null || foundNextProgram == null) {
    return emptyList()
  }

  return createList()
}

private fun ThermostatProgramInfo.Builder.identifyPrograms() {
  val currentQuarter = QuarterOfHour.from(currentMinute!!)

  var idx = 0
  while (true) {
    val entry = weeklyScheduleConfig!!.schedule[idx % weeklyScheduleConfig!!.schedule.size]
    if (foundCurrentProgram != null) {
      if (entry.program != foundCurrentProgram) {
        foundNextProgram = entry.program
        break
      }

      quartersToNextProgram = quartersToNextProgram?.plus(1)
    }
    if (entry.dayOfWeek.day == currentDayOfWeek!!.day && entry.hour == currentHour && entry.quarterOfHour == currentQuarter) {
      foundCurrentProgram = entry.program
      quartersToNextProgram = 0
    }

    idx++
    if (idx > weeklyScheduleConfig!!.schedule.size.times(2)) {
      break
    }
  }
}

private fun ThermostatProgramInfo.Builder.clockErrorList() =
  listOf(
    ThermostatProgramInfo(
      type = ThermostatProgramInfo.Type.CURRENT,
      time = { context -> context.getString(R.string.thermostat_clock_error) },
      icon = currentMode!!.icon,
      iconColor = currentMode!!.iconColor,
      descriptionProvider = { context -> context.valuesFormatter.getTemperatureString(currentTemperature) }
    )
  )

private fun ThermostatProgramInfo.Builder.createList(): List<ThermostatProgramInfo> {
  val minutesToNextProgram = quartersToNextProgram!! * 15 + (15 - (currentMinute!! % 15))
  val nextScheduleProgram = getProgram(foundNextProgram)
  val descriptionProvider: StringProvider = { context -> context.valuesFormatter.getTemperatureString(currentTemperature) }

  // If time synchronization disabled show only current program
  if (deviceConfig.isAutomaticTimeSyncDisabled()) {
    return listOf(
      ThermostatProgramInfo(
        type = ThermostatProgramInfo.Type.CURRENT,
        icon = currentMode!!.icon,
        iconColor = currentMode!!.iconColor,
        descriptionProvider = if (currentMode == SuplaHvacMode.OFF) null else descriptionProvider,
        manualActive = thermostatFlags!!.contains(SuplaThermostatFlags.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE)
      )
    )
  }

  return listOf(
    ThermostatProgramInfo(
      type = ThermostatProgramInfo.Type.CURRENT,
      time = { context ->
        context.getString(
          R.string.thermostat_detail_program_time,
          context.valuesFormatter.getHourWithMinutes(minutesToNextProgram)(context)
        )
      },
      icon = currentMode!!.icon,
      iconColor = currentMode!!.iconColor,
      descriptionProvider = if (currentMode == SuplaHvacMode.OFF) null else descriptionProvider,
      manualActive = thermostatFlags!!.contains(SuplaThermostatFlags.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE)
    ),
    ThermostatProgramInfo(
      type = ThermostatProgramInfo.Type.NEXT,
      icon = nextScheduleProgram?.mode?.icon,
      iconColor = nextScheduleProgram?.mode?.iconColor,
      descriptionProvider = nextScheduleProgram?.description
    )
  )
}

private fun ThermostatProgramInfo.Builder.getProgram(program: SuplaScheduleProgram?) =
  if (program == SuplaScheduleProgram.OFF) {
    SuplaWeeklyScheduleProgram.OFF
  } else {
    weeklyScheduleConfig!!.programConfigurations.firstOrNull { it.program == program }
  }
