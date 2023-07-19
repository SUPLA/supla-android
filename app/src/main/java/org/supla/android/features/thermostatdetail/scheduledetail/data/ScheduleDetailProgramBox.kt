package org.supla.android.features.thermostatdetail.scheduledetail.data
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
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.extensions.valuesFormatter
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT

data class ScheduleDetailProgramBox(
  val channelFunction: Int,
  val program: SuplaScheduleProgram,
  val mode: SuplaHvacMode,
  val setpointTemperatureMin: Float? = null,
  val setpointTemperatureMax: Float? = null,
  @DrawableRes val iconRes: Int? = null
) {

  val textProvider: StringProvider
    get() = when {
      program == SuplaScheduleProgram.OFF -> { context -> context.resources.getString(R.string.turn_off) }
      mode == SuplaHvacMode.HEAT -> { context -> context.valuesFormatter.getTemperatureString(setpointTemperatureMin?.toDouble()) }
      mode == SuplaHvacMode.COOL -> { context -> context.valuesFormatter.getTemperatureString(setpointTemperatureMax?.toDouble()) }
      mode == SuplaHvacMode.AUTO -> { context ->
        val minTemperature = context.valuesFormatter.getTemperatureString(setpointTemperatureMin?.toDouble())
        val maxTemperature = context.valuesFormatter.getTemperatureString(setpointTemperatureMax?.toDouble())
        "$minTemperature - $maxTemperature"
      }
      else -> { _ -> ValuesFormatter.NO_VALUE_TEXT }
    }

  val modeForModify: SuplaHvacMode
    get() = if (mode == SuplaHvacMode.NOT_SET) {
      when (channelFunction) {
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT,
        SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER -> SuplaHvacMode.HEAT
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL -> SuplaHvacMode.COOL
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO -> SuplaHvacMode.AUTO
        else -> mode
      }
    } else {
      mode
    }

  val temperatureMinForModify: Float
    get() {
      if (setpointTemperatureMin != null && setpointTemperatureMin > 0) {
        return setpointTemperatureMin
      }
      return if (channelFunction == SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER && mode == SuplaHvacMode.HEAT) {
        40f
      } else {
        21f
      }
    }

  val temperatureMaxForModify: Float
    get() {
      if (setpointTemperatureMax != null && setpointTemperatureMax > 0) {
        return setpointTemperatureMax
      }
      return 24f
    }

  companion object {
    fun default() = listOf(
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT,
        SuplaScheduleProgram.PROGRAM_1,
        SuplaHvacMode.HEAT,
        20f,
        null,
        R.drawable.ic_heat
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT,
        SuplaScheduleProgram.PROGRAM_2,
        SuplaHvacMode.COOL,
        null,
        22.5f,
        R.drawable.ic_cool
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT,
        SuplaScheduleProgram.PROGRAM_3,
        SuplaHvacMode.AUTO,
        21f,
        22.5f,
        R.drawable.ic_heat
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT,
        SuplaScheduleProgram.PROGRAM_4,
        SuplaHvacMode.HEAT,
        23f,
        null,
        R.drawable.ic_cool
      ),
      ScheduleDetailProgramBox(
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT,
        SuplaScheduleProgram.OFF,
        SuplaHvacMode.OFF,
        null,
        null,
        R.drawable.ic_power_button
      )
    )
  }
}
