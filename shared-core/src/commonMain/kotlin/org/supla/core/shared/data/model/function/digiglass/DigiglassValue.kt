package org.supla.core.shared.data.model.function.digiglass
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
import kotlin.math.pow

private const val DIGIGLASS_VALUE_LENGTH = 8

data class DigiglassValue(
  val status: SuplaChannelAvailabilityStatus,
  val flags: List<SuplaDigiglassFlag>,
  val sectionCount: Int,
  val mask: Int
) {

  val isAnySectionTransparent: Boolean
    get() {
      val activeBits = 2.0.pow(sectionCount).toInt().minus(1)
      return (mask and activeBits) > 0
    }

  companion object {
    fun from(status: SuplaChannelAvailabilityStatus, bytes: ByteArray): DigiglassValue {
      if (bytes.size < DIGIGLASS_VALUE_LENGTH) {
        return DigiglassValue(status, emptyList(), 0, 0)
      }

      return DigiglassValue(
        status = status,
        flags = SuplaDigiglassFlag.from(bytes[0].toInt()),
        sectionCount = bytes[1].toInt(),
        mask = bytes[2].toInt() or bytes[3].toInt().shl(8)
      )
    }
  }
}
