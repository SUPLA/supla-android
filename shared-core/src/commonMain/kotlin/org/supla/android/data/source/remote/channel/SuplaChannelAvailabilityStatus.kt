package org.supla.android.data.source.remote.channel
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

enum class SuplaChannelAvailabilityStatus(val rawValue: Int) {
  ONLINE(1),
  OFFLINE(0),
  ONLINE_BUT_NOT_AVAILABLE(2),
  OFFLINE_REMOTE_WAKEUP_NOT_SUPPORTED(3),
  FIRMWARE_UPDATE_ONGOING(4);

  val online: Boolean
    get() = this == ONLINE || this == FIRMWARE_UPDATE_ONGOING

  val offline: Boolean
    get() = !online

  companion object {
    fun from(value: Int): SuplaChannelAvailabilityStatus {
      for (status in entries) {
        if (status.rawValue == value) {
          return status
        }
      }

      return OFFLINE
    }

    fun from(online: Boolean): SuplaChannelAvailabilityStatus =
      if (online) ONLINE else OFFLINE
  }
}
