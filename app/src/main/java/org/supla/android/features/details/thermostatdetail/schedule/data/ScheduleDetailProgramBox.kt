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
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

data class ScheduleDetailProgramBox(
  val channelFunction: Int,
  val thermostatFunction: ThermostatSubfunction,
  val program: SuplaScheduleProgram,
  val mode: SuplaHvacMode,
  val setpointTemperatureHeat: Float?,
  val setpointTemperatureCool: Float?,
  val label: LocalizedString,
  @param:DrawableRes val iconRes: Int? = null
) {

  val modeForModify: SuplaHvacMode
    get() = if (mode == SuplaHvacMode.NOT_SET) {
      when (channelFunction) {
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT if thermostatFunction == ThermostatSubfunction.HEAT -> SuplaHvacMode.HEAT
        SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER -> SuplaHvacMode.HEAT
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT if thermostatFunction == ThermostatSubfunction.COOL -> SuplaHvacMode.COOL
        SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL -> SuplaHvacMode.HEAT_COOL
        else -> mode
      }
    } else {
      mode
    }

  companion object {
    operator fun invoke(
      channelFunction: Int,
      thermostatFunction: ThermostatSubfunction,
      program: SuplaScheduleProgram,
      mode: SuplaHvacMode,
      setpointTemperatureHeat: Float?,
      setpointTemperatureCool: Float?,
      valueFormatter: ValueFormatter,
      @DrawableRes iconRes: Int? = null
    ): ScheduleDetailProgramBox =
      ScheduleDetailProgramBox(
        channelFunction = channelFunction,
        thermostatFunction = thermostatFunction,
        program = program,
        mode = mode,
        setpointTemperatureHeat = setpointTemperatureHeat,
        setpointTemperatureCool = setpointTemperatureCool,
        label = createLabel(program, mode, setpointTemperatureHeat, setpointTemperatureCool, valueFormatter),
        iconRes = iconRes
      )

    private fun createLabel(
      program: SuplaScheduleProgram,
      mode: SuplaHvacMode,
      temperatureHeat: Float?,
      temperatureCool: Float?,
      valueFormatter: ValueFormatter
    ): LocalizedString {
      return when {
        program == SuplaScheduleProgram.OFF -> localizedString(R.string.turn_off)
        mode == SuplaHvacMode.HEAT -> LocalizedString.Constant(valueFormatter.format(temperatureHeat, ValueFormat.TemperatureWithDegree))
        mode == SuplaHvacMode.COOL -> LocalizedString.Constant(valueFormatter.format(temperatureCool, ValueFormat.TemperatureWithDegree))
        mode == SuplaHvacMode.HEAT_COOL -> {
          val minTemperature = valueFormatter.format(temperatureHeat, ValueFormat.TemperatureWithDegree)
          val maxTemperature = valueFormatter.format(temperatureCool, ValueFormat.TemperatureWithDegree)
          LocalizedString.Constant("$minTemperature - $maxTemperature")
        }
        else -> LocalizedString.Constant(NO_VALUE_TEXT)
      }
    }
  }
}
