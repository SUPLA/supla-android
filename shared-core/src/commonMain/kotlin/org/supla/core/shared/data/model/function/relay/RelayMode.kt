package org.supla.core.shared.data.model.function.relay

import org.supla.core.shared.infrastructure.logging.Logger

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

enum class RelayMode(val value: Int) {
  NOT_SET(0),
  START_ON(1),
  START_OFF(2),
  FORCED_ON(3),
  FORCED_OFF(4),
  AUTOMATIC(5),
  CMD_WEEKLY_SCHEDULE(6),
  CMD_SWITCH_TO_MANUAL(7);

  companion object {
    const val TAG = "RelayMode"

    fun from(value: Int): RelayMode {
      for (mode in entries) {
        if (mode.value == value) {
          return mode
        }
      }

      Logger.w(TAG, "Unknown value: $value")
      return NOT_SET
    }
  }
}
