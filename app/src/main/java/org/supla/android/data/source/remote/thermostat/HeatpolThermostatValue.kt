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

import org.supla.core.shared.extensions.toTemperature

private const val HEATPOL_THERMOSTAT_VALUE_LENGTH = 6

data class HeatpolThermostatValue(
  val online: Boolean,
  val on: Boolean,
  val flags: List<SuplaHeatpolThermostatFlag>,
  val measuredTemperature: Float,
  val presetTemperature: Float
) {

  companion object {
    fun from(online: Boolean, bytes: ByteArray): HeatpolThermostatValue {
      if (bytes.size < HEATPOL_THERMOSTAT_VALUE_LENGTH) {
        return HeatpolThermostatValue(online, false, emptyList(), 0f, 0f)
      }

      return HeatpolThermostatValue(
        online = online,
        on = bytes[0].toInt() == 1,
        flags = SuplaHeatpolThermostatFlag.from(bytes[1].toShort()),
        measuredTemperature = bytes.toTemperature(2, 3),
        presetTemperature = bytes.toTemperature(4, 5)
      )
    }
  }
}
