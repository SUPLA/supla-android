package org.supla.core.shared.data.model.function.container
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

enum class ContainerFlag(val value: Int) {
  WARNING_LEVEL(1),
  ALARM_LEVEL(1 shl 1),
  INVALID_SENSOR_STATE(1 shl 2),
  SOUND_ALARM_ON(1 shl 3);

  companion object {
    fun from(short: Short): List<ContainerFlag> {
      val result = mutableListOf<ContainerFlag>()
      for (flag in entries) {
        if (flag.value and short.toInt() > 0) {
          result.add(flag)
        }
      }

      return result
    }
  }
}
