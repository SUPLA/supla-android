package org.supla.android.data.source.remote.thermostat
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

import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

fun ThermostatValue.getIndicatorIcon() = when {
  status.online && flags.contains(SuplaThermostatFlag.FORCED_OFF_BY_SENSOR) -> ThermostatIndicatorIcon.FORCED_OFF_BY_SENSOR
  status.online && flags.contains(SuplaThermostatFlag.COOLING) -> ThermostatIndicatorIcon.COOLING
  status.online && flags.contains(SuplaThermostatFlag.HEATING) -> ThermostatIndicatorIcon.HEATING
  status.online && mode != SuplaHvacMode.OFF -> ThermostatIndicatorIcon.STANDBY
  status.online -> ThermostatIndicatorIcon.OFF
  else -> ThermostatIndicatorIcon.OFFLINE
}

fun ThermostatValue.getSetpointText(formatter: ValueFormatter): String {
  val temperatureMin = formatter.format(setpointTemperatureHeat, ValueFormat.WithoutUnit)
  val temperatureMax = formatter.format(setpointTemperatureCool, ValueFormat.WithoutUnit)
  return when {
    status.offline -> ""
    mode == SuplaHvacMode.COOL -> temperatureMax
    mode == SuplaHvacMode.HEAT_COOL -> "$temperatureMin - $temperatureMax"
    mode == SuplaHvacMode.HEAT -> temperatureMin
    mode == SuplaHvacMode.OFF -> "Off"
    else -> ""
  }
}
