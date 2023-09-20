package org.supla.android.features.thermostatdetail.scheduledetail.extensions
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
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.extensions.fromSuplaTemperature
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxKey
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxValue
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailProgramBox
import org.supla.android.lib.SuplaConst

fun SuplaChannelWeeklyScheduleConfig.viewScheduleBoxesMap() =
  mutableMapOf<ScheduleDetailEntryBoxKey, ScheduleDetailEntryBoxValue>().apply {
    for (entry in schedule) {
      val key = ScheduleDetailEntryBoxKey(entry.dayOfWeek, entry.hour.toShort())
      if (containsKey(key)) {
        val value = this[key]!!
        this[key] = when (entry.quarterOfHour) {
          QuarterOfHour.FIRST -> value.copy(firstQuarterProgram = entry.program)
          QuarterOfHour.SECOND -> value.copy(secondQuarterProgram = entry.program)
          QuarterOfHour.THIRD -> value.copy(thirdQuarterProgram = entry.program)
          QuarterOfHour.FOURTH -> value.copy(fourthQuarterProgram = entry.program)
        }
      } else {
        val value = ScheduleDetailEntryBoxValue(SuplaScheduleProgram.OFF)
        this[key] = when (entry.quarterOfHour) {
          QuarterOfHour.FIRST -> value.copy(firstQuarterProgram = entry.program)
          QuarterOfHour.SECOND -> value.copy(secondQuarterProgram = entry.program)
          QuarterOfHour.THIRD -> value.copy(thirdQuarterProgram = entry.program)
          QuarterOfHour.FOURTH -> value.copy(fourthQuarterProgram = entry.program)
        }
      }
    }
  }

fun SuplaChannelWeeklyScheduleConfig.viewProgramBoxesList(subfunction: ThermostatSubfunction) =
  mutableListOf<ScheduleDetailProgramBox>().apply {
    for (program in programConfigurations) {
      val icon = when {
        func == SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO && program.mode == SuplaHvacMode.HEAT -> R.drawable.ic_heat
        func == SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO && program.mode == SuplaHvacMode.COOL -> R.drawable.ic_cool
        else -> null
      }
      ScheduleDetailProgramBox(
        channelFunction = func ?: 0,
        thermostatFunction = subfunction,
        program = program.program,
        mode = program.mode,
        setpointTemperatureHeat = program.setpointTemperatureHeat?.fromSuplaTemperature(),
        setpointTemperatureCool = program.setpointTemperatureCool?.fromSuplaTemperature(),
        iconRes = icon
      ).also {
        add(it)
      }
    }

    // Server list doesn't contain the off program, so we need to add it manually
    ScheduleDetailProgramBox(
      channelFunction = func ?: 0,
      thermostatFunction = subfunction,
      program = SuplaScheduleProgram.OFF,
      mode = SuplaHvacMode.OFF,
      iconRes = R.drawable.ic_power_button
    ).also { add(it) }
  }
