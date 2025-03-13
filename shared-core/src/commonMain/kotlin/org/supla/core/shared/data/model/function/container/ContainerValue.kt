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

import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.extensions.toShort

private const val CONTAINER_VALUE_LENGTH = 8

data class ContainerValue(
  val status: SuplaChannelAvailabilityStatus,
  val flags: List<ContainerFlag>,
  private val rawLevel: Int
) {

  val level: Int
    get() = rawLevel - 1
  val levelKnown: Boolean
    get() = rawLevel != 0

  companion object {
    fun from(status: SuplaChannelAvailabilityStatus, bytes: ByteArray): ContainerValue {
      if (bytes.count() < CONTAINER_VALUE_LENGTH) {
        return ContainerValue(status, emptyList(), 0)
      }

      return ContainerValue(
        status = status,
        flags = ContainerFlag.from(bytes.toShort(1, 2)),
        rawLevel = bytes[0].toInt()
      )
    }
  }
}
