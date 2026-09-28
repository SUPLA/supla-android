package org.supla.core.shared.data.model.function.relay
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

import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.hvac.SuplaRelayMode

private const val RELAY_VALUE_LENGTH = 4

data class RelayValue(
  val status: SuplaChannelAvailabilityStatus,
  val on: Boolean,
  val flags: List<SuplaRelayFlag>,
  val mode: SuplaRelayMode
) {

  companion object {
    fun from(status: SuplaChannelAvailabilityStatus, bytes: ByteArray): RelayValue {
      if (bytes.size < RELAY_VALUE_LENGTH) {
        return RelayValue(status, false, emptyList(), SuplaRelayMode.NOT_SET)
      }

      return RelayValue(
        status = status,
        on = bytes[0] >= 1,
        flags = SuplaRelayFlag.from(
          (bytes[1].toInt() and 0xFF) or ((bytes[2].toInt() and 0xFF) shl 8)
        ),
        mode = SuplaRelayMode.from(bytes[3].toInt() and 0xFF)
      )
    }
  }
}
