package org.supla.android.data.source.remote
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

enum class SuplaEventType(val value: Int) {
  UNKNOWN(-1),
  CONTROLLING_THE_GATEWAY_LOCK(10),
  CONTROLLING_THE_GATE(20),
  CONTROLLING_THE_GARAGE_DOOR(30),
  CONTROLLING_THE_DOOR_LOCK(40),
  CONTROLLING_THE_ROLLER_SHUTTER(50),
  CONTROLLING_THE_ROOF_WINDOW(55),
  POWER_ON_OFF(60),
  LIGHT_ON_OFF(70),
  VALVE_OPEN_CLOSE(90),
  SET_BRIDGE_VALUE_FAILED(100);

  companion object {
    fun from(value: Int): SuplaEventType? {
      for (entry in entries) {
        if (entry.value == value) {
          return entry
        }
      }

      return null
    }
  }
}
