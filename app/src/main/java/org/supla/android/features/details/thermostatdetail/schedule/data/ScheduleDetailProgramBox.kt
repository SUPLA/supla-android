package org.supla.android.features.details.thermostatdetail.schedule.data
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

import androidx.annotation.DrawableRes
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.features.details.thermostatdetail.ui.OFF
import org.supla.android.features.details.thermostatdetail.ui.description
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL
import org.supla.core.shared.extensions.fromSuplaTemperature

data class ScheduleDetailProgramBox(
  val channelFunction: Int,
  val thermostatFunction: ThermostatSubfunction,
  val scheduleProgram: SuplaWeeklyScheduleProgram,
  @DrawableRes val iconRes: Int? = null
) {

  val setpointTemperatureHeat: Float?
    get() = scheduleProgram.setpointTemperatureHeat?.fromSuplaTemperature()

  val setpointTemperatureCool: Float?
    get() = scheduleProgram.setpointTemperatureCool?.fromSuplaTemperature()

  val textProvider: StringProvider
    get() = scheduleProgram.description

  val modeForModify: SuplaHvacMode
    get() = if (scheduleProgram.mode == SuplaHvacMode.NOT_SET) {
      when {
        channelFunction == SUPLA_CHANNELFNC_HVAC_THERMOSTAT && thermostatFunction == ThermostatSubfunction.HEAT -> SuplaHvacMode.HEAT
        channelFunction == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER -> SuplaHvacMode.HEAT
        channelFunction == SUPLA_CHANNELFNC_HVAC_THERMOSTAT && thermostatFunction == ThermostatSubfunction.COOL -> SuplaHvacMode.COOL
        channelFunction == SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL -> SuplaHvacMode.HEAT_COOL
        else -> scheduleProgram.mode
      }
    } else {
      scheduleProgram.mode
    }

  val temperatureMinForModify: Float
    get() {
      setpointTemperatureHeat?.let {
        if (it > 0) {
          return it
        }
      }

      return if (channelFunction == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER && scheduleProgram.mode == SuplaHvacMode.HEAT) {
        40f
      } else {
        21f
      }
    }

  val temperatureMaxForModify: Float
    get() {
      setpointTemperatureCool?.let {
        if (it > 0) {
          return it
        }
      }
      return 24f
    }

  companion object {
    fun default() = listOf(
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
        ThermostatSubfunction.HEAT,
        SuplaWeeklyScheduleProgram(
          SuplaScheduleProgram.PROGRAM_1,
          SuplaHvacMode.HEAT,
          2000,
          null
        ),
        R.drawable.ic_heat
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
        ThermostatSubfunction.HEAT,
        SuplaWeeklyScheduleProgram(
          SuplaScheduleProgram.PROGRAM_2,
          SuplaHvacMode.COOL,
          null,
          2250
        ),
        R.drawable.ic_cool
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
        ThermostatSubfunction.HEAT,
        SuplaWeeklyScheduleProgram(
          SuplaScheduleProgram.PROGRAM_3,
          SuplaHvacMode.HEAT_COOL,
          2100,
          2250
        ),
        R.drawable.ic_heat
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
        ThermostatSubfunction.HEAT,
        SuplaWeeklyScheduleProgram(
          SuplaScheduleProgram.PROGRAM_4,
          SuplaHvacMode.HEAT,
          2300,
          null
        ),
        R.drawable.ic_cool
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
        ThermostatSubfunction.HEAT,
        SuplaWeeklyScheduleProgram.OFF,
        R.drawable.ic_power_button
      )
    )
  }
}
