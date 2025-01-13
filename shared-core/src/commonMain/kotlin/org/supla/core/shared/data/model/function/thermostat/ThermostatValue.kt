package org.supla.core.shared.data.model.function.thermostat
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
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag.HEAT_OR_COOL
import org.supla.core.shared.extensions.toShort
import org.supla.core.shared.extensions.toTemperature

private const val THERMOSTAT_VALUE_LENGTH = 8

@ExposedCopyVisibility
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

  companion object {
    fun from(online: Boolean, bytes: ByteArray): ThermostatValue {
      if (bytes.size < THERMOSTAT_VALUE_LENGTH) {
        return ThermostatValue(online, ThermostatState(0), SuplaHvacMode.UNKNOWN, 0f, 0f, emptyList())
      }

      return ThermostatValue(
        online = online,
        state = ThermostatState(bytes[0].toShort()),
        mode = SuplaHvacMode.from(bytes[1].toInt()),
        setpointTemperatureHeat = bytes.toTemperature(2, 3),
        setpointTemperatureCool = bytes.toTemperature(4, 5),
        flags = SuplaThermostatFlag.from(bytes.toShort(6, 7))
      )
    }
  }
}

data class ThermostatState(val value: Short) {
  val power: Float? = if (value > 1) value.minus(2).toFloat() else null

  fun isOn() = value.toInt() == 1 || value > 2
  fun isOff() = value.toInt() == 0 || value.toInt() == 2
}
