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

enum class SuplaThermostatFlag(val value: Int) {
  SETPOINT_TEMP_MIN_SET(1),
  SETPOINT_TEMP_MAX_SET(1 shl 1),
  HEATING(1 shl 2),
  COOLING(1 shl 3),
  WEEKLY_SCHEDULE(1 shl 4),
  COUNTDOWN_TIMER(1 shl 5),
  FAN_ENABLED(1 shl 6),
  THERMOMETER_ERROR(1 shl 7),
  CLOCK_ERROR(1 shl 8),
  FORCED_OFF_BY_SENSOR(1 shl 9),
  HEAT_OR_COOL(1 shl 10), // If set cool else heat
  WEEKLY_SCHEDULE_TEMPORAL_OVERRIDE(1 shl 11),
  BATTERY_COVER_OPEN(1 shl 12),
  CALIBRATION_ERROR(1 shl 13);

  companion object {
    fun from(short: Short): List<SuplaThermostatFlag> {
      val result = mutableListOf<SuplaThermostatFlag>()
      for (flag in entries) {
        if (flag.value and short.toInt() > 0) {
          result.add(flag)
        }
      }

      return result
    }
  }
}
