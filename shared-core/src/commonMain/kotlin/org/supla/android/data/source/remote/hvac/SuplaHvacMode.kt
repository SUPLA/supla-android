package org.supla.android.data.source.remote.hvac
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
  UNKNOWN(-1),
  NOT_SET(0),
  OFF(1),
  HEAT(2),
  COOL(3),
  HEAT_COOL(4),
  FAN_ONLY(6),
  DRY(7),
  CMD_TURN_ON(8),
  CMD_WEEKLY_SCHEDULE(9),
  CMD_SWITCH_TO_MANUAL(10);

  companion object {
    fun from(byte: Byte): SuplaHvacMode {
      for (state in entries) {
        if (state.value == byte.toInt()) {
          return state
        }
      }

      return UNKNOWN
    }
  }
}
