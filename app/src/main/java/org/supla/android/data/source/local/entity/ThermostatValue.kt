package org.supla.android.data.source.local.entity
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
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.extensions.fromSuplaTemperature
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ThermostatValue(
  val state: ThermostatState,
  val mode: SuplaHvacMode,
  val setpointTemperatureMin: Float,
  val setpointTemperatureMax: Float,
  val flags: List<SuplaThermostatFlags>
) {
  companion object {
    fun from(bytes: ByteArray): ThermostatValue {
      return ThermostatValue(
        state = ThermostatState(bytes[0].toShort()),
        mode = SuplaHvacMode.from(bytes[1]),
        setpointTemperatureMin = bytes.toTemperature(2, 3),
        setpointTemperatureMax = bytes.toTemperature(4, 5),
        flags = SuplaThermostatFlags.from(bytes[6])
      )
    }
  }
}

data class ThermostatState(val value: Short) {
  fun isOn() = value > 0
  fun isOff() = value.toInt() == 0
}

private fun ByteArray.toTemperature(vararg byteIndices: Int): Float {
  val bytes = ByteArray(byteIndices.size)
  byteIndices.sorted().forEachIndexed { index, byte -> bytes[index] = this[byte] }
  return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short.fromSuplaTemperature()
}
