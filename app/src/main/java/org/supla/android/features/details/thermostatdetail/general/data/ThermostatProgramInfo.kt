package org.supla.android.features.details.thermostatdetail.general.data
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

import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.icon
import org.supla.android.data.source.remote.hvac.iconColor
import org.supla.android.data.source.remote.isAutomaticTimeSyncDisabled
import org.supla.android.features.details.programinfo.ProgramInfo
import org.supla.android.features.details.thermostatdetail.ui.OFF
import org.supla.android.features.details.thermostatdetail.ui.description
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

class ThermostatProgramInfoBuilder(val thermometerValueFormatter: ValueFormatter) {
  // external
  var dateProvider: DateProvider? = null
  var weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig? = null
  var deviceConfig: SuplaDeviceConfig? = null
  var thermostatFlags: List<SuplaThermostatFlag>? = null
  var currentMode: SuplaHvacMode? = null
  var currentTemperature: Float? = null
  var channelOnline: Boolean? = null

  val currentTemperatureString: String
    get() = thermometerValueFormatter.format(currentTemperature, ValueFormat.TemperatureWithDegree)

  // internal
  internal var currentDayOfWeek: DayOfWeek? = null
  internal var currentHour: Int? = null
  internal var currentMinute: Int? = null

  internal var foundCurrentProgram: SuplaScheduleProgram? = null
  internal var foundNextProgram: SuplaScheduleProgram? = null
  internal var quartersToNextProgram: Int? = null
}

fun ThermostatProgramInfoBuilder.build(): List<ProgramInfo> {
  val (dateProvider) = guardLet(dateProvider) { throw IllegalStateException("Date provider cannot be null") }
  val (config) = guardLet(weeklyScheduleConfig) { throw IllegalStateException("Config cannot be null") }
  val (flags) = guardLet(thermostatFlags) { throw IllegalStateException("Thermostat flags cannot be null") }
  guardLet(currentMode) { throw IllegalStateException("Current mode cannot be null") }
  guardLet(currentTemperature) { throw IllegalStateException("Current temperature cannot be null") }
  val (isOnline) = guardLet(channelOnline) { throw IllegalStateException("Channel online cannot be null") }

  if (isOnline.not() || config.schedule.isEmpty() || config.programConfigurations.isEmpty()) {
    return emptyList()
  }
  if (flags.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE).not()) {
    return emptyList()
  }
  if (flags.contains(SuplaThermostatFlag.CLOCK_ERROR)) {
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

private fun ThermostatProgramInfoBuilder.identifyPrograms() {
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

private fun ThermostatProgramInfoBuilder.clockErrorList() =
  listOf(
    ProgramInfo(
      type = ProgramInfo.Type.CURRENT,
      time = localizedString(R.string.thermostat_clock_error),
      icon = currentMode!!.icon,
      iconColor = currentMode!!.iconColor,
      description = LocalizedString.Constant(currentTemperatureString)
    )
  )

private fun ThermostatProgramInfoBuilder.createList(): List<ProgramInfo> {
  val minutesToNextProgram = quartersToNextProgram!! * 15 + (15 - (currentMinute!! % 15))
  val nextScheduleProgram = getProgram(foundNextProgram)
  val descriptionProvider: LocalizedString = LocalizedString.Constant(currentTemperatureString)

  // If time synchronization disabled show only current program
  if (deviceConfig.isAutomaticTimeSyncDisabled()) {
    return listOf(
      ProgramInfo(
        type = ProgramInfo.Type.CURRENT,
        icon = currentMode!!.icon,
        iconColor = currentMode!!.iconColor,
        description = if (currentMode == SuplaHvacMode.OFF) null else descriptionProvider,
        indicatorIcon = R.drawable.ic_manual.takeIf { thermostatFlags!!.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE) },
        indicatorIconColor = R.color.primary.takeIf { thermostatFlags!!.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE) }
      )
    )
  }

  return listOf(
    ProgramInfo(
      type = ProgramInfo.Type.CURRENT,
      time = localizedString(R.string.program_info_time, ValuesFormatter.getHourWithMinutes(minutesToNextProgram)),
      icon = currentMode!!.icon,
      iconColor = currentMode!!.iconColor,
      description = if (currentMode == SuplaHvacMode.OFF) null else descriptionProvider,
      indicatorIcon = R.drawable.ic_manual.takeIf { thermostatFlags!!.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE) },
      indicatorIconColor = R.color.primary.takeIf { thermostatFlags!!.contains(SuplaThermostatFlag.WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE) }
    ),
    ProgramInfo(
      type = ProgramInfo.Type.NEXT,
      icon = nextScheduleProgram?.mode?.icon,
      iconColor = nextScheduleProgram?.mode?.iconColor,
      description = nextScheduleProgram?.description(thermometerValueFormatter)
    )
  )
}

private fun ThermostatProgramInfoBuilder.getProgram(program: SuplaScheduleProgram?) =
  if (program == SuplaScheduleProgram.OFF) {
    SuplaWeeklyScheduleProgram.OFF
  } else {
    weeklyScheduleConfig!!.programConfigurations.firstOrNull { it.program == program }
  }
