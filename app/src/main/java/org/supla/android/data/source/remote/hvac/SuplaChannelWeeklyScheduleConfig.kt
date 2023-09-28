package org.supla.android.data.source.remote.hvac

import org.supla.android.R
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.SuplaChannelConfig

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

enum class SuplaHvacMode(val value: Int) {
  NOT_SET(0),
  OFF(1),
  HEAT(2),
  COOL(3),
  AUTO(4),
  FAN_ONLY(6),
  DRY(7),
  CMD_TURN_ON(8),
  CMD_WEEKLY_SCHEDULE(9),
  CMD_SWITCH_TO_MANUAL(10);

  val icon: Int?
    get() {
      return when (this) {
        OFF -> R.drawable.ic_power_button
        HEAT -> R.drawable.ic_heat
        COOL -> R.drawable.ic_cool
        else -> null
      }
    }

  val iconColor: Int?
    get() {
      return when (this) {
        OFF -> R.color.gray
        HEAT -> R.color.red
        COOL -> R.color.blue
        else -> null
      }
    }

  companion object {
    fun from(byte: Byte): SuplaHvacMode {
      for (state in SuplaHvacMode.values()) {
        if (state.value == byte.toInt()) {
          return state
        }
      }

      throw IllegalArgumentException("Could not create ThermostatMode from $byte")
    }
  }
}

enum class SuplaScheduleProgram(val value: Int) {
  OFF(0),
  PROGRAM_1(1),
  PROGRAM_2(2),
  PROGRAM_3(3),
  PROGRAM_4(4)
}

data class SuplaWeeklyScheduleProgram( /* aka TWeeklyScheduleProgram */
  val program: SuplaScheduleProgram,
  val mode: SuplaHvacMode,
  val setpointTemperatureHeat: Short? = null,
  val setpointTemperatureCool: Short? = null
) {
  companion object
}

data class SuplaWeeklyScheduleEntry(
  val dayOfWeek: DayOfWeek,
  val hour: Int,
  val quarterOfHour: QuarterOfHour,
  val program: SuplaScheduleProgram
)

data class SuplaChannelWeeklyScheduleConfig( /* aka TChannelConfig_WeeklySchedule */
  override val remoteId: Int,
  override val func: Int?,
  val programConfigurations: List<SuplaWeeklyScheduleProgram>,
  val schedule: List<SuplaWeeklyScheduleEntry>
) : SuplaChannelConfig(remoteId, func)
