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

import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlag.HEAT_OR_COOL
import org.supla.android.extensions.toShortVararg
import org.supla.android.extensions.toTemperature
import org.supla.android.ui.lists.data.ChannelIssueItem

@Suppress("DataClassPrivateConstructor")
data class ThermostatValue private constructor(
  val online: Boolean,
  val state: ThermostatState,
  val mode: SuplaHvacMode,
  val setpointTemperatureHeat: Float,
  val setpointTemperatureCool: Float,
  val flags: List<SuplaThermostatFlag>
) {

  val subfunction: ThermostatSubfunction
    get() = if (flags.contains(HEAT_OR_COOL)) ThermostatSubfunction.COOL else ThermostatSubfunction.HEAT

  fun getIndicatorIcon() = when {
    online && flags.contains(SuplaThermostatFlag.FORCED_OFF_BY_SENSOR) -> ThermostatIndicatorIcon.FORCED_OFF_BY_SENSOR
    online && flags.contains(SuplaThermostatFlag.COOLING) -> ThermostatIndicatorIcon.COOLING
    online && flags.contains(SuplaThermostatFlag.HEATING) -> ThermostatIndicatorIcon.HEATING
    online && mode != SuplaHvacMode.OFF -> ThermostatIndicatorIcon.STANDBY
    online -> ThermostatIndicatorIcon.OFF
    else -> ThermostatIndicatorIcon.OFFLINE
  }

  fun getChannelIssues(): List<ChannelIssueItem> = when {
    online && flags.contains(SuplaThermostatFlag.THERMOMETER_ERROR) -> listOf(ChannelIssueItem.Error(R.string.thermostat_thermometer_error))
    online && flags.contains(SuplaThermostatFlag.BATTERY_COVER_OPEN) -> listOf(
      ChannelIssueItem.Error(R.string.thermostat_battery_cover_open)
    )
    online && flags.contains(SuplaThermostatFlag.CLOCK_ERROR) -> listOf(ChannelIssueItem.Warning(R.string.thermostat_clock_error))
    else -> emptyList()
  }

  fun getSetpointText(valuesFormatter: ValuesFormatter): String {
    val temperatureMin = valuesFormatter.getTemperatureString(setpointTemperatureHeat, false)
    val temperatureMax = valuesFormatter.getTemperatureString(setpointTemperatureCool, false)
    return when {
      online.not() -> ""
      mode == SuplaHvacMode.COOL -> temperatureMax
      mode == SuplaHvacMode.HEAT_COOL -> "$temperatureMin - $temperatureMax"
      mode == SuplaHvacMode.HEAT -> temperatureMin
      mode == SuplaHvacMode.OFF -> "Off"
      else -> ""
    }
  }

  companion object {
    fun from(online: Boolean, bytes: ByteArray): ThermostatValue {
      return ThermostatValue(
        online = online,
        state = ThermostatState(bytes[0].toShort()),
        mode = SuplaHvacMode.from(bytes[1]),
        setpointTemperatureHeat = bytes.toTemperature(2, 3),
        setpointTemperatureCool = bytes.toTemperature(4, 5),
        flags = SuplaThermostatFlag.from(bytes.toShortVararg(6, 7))
      )
    }
  }
}

data class ThermostatState(val value: Short) {
  val power: Float? = if (value > 1) value.minus(1).toFloat() else null

  fun isOn() = value > 0
  fun isOff() = value.toInt() == 0
}
