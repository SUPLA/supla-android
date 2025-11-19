package org.supla.core.shared.data.model.function.rgbanddimmer
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

private const val RGB_VALUE_LENGTH = 5

data class RgbValue(
  override val status: SuplaChannelAvailabilityStatus,
  override val on: Boolean,
  override val colorBrightness: Int,
  override val red: Int,
  override val green: Int,
  override val blue: Int
) : RgbBaseValue {
  companion object {
    fun from(status: SuplaChannelAvailabilityStatus, bytes: ByteArray): RgbValue {
      if (bytes.size < RGB_VALUE_LENGTH) {
        return RgbValue(status, false, 0, 0, 0, 0)
      }

      return RgbValue(
        status = status,
        on = bytes[0] >= 1,
        colorBrightness = bytes[1].toInt().coerceAtLeast(0).coerceAtMost(100),
        red = bytes[4].toInt(),
        green = bytes[3].toInt(),
        blue = bytes[2].toInt()
      )
    }
  }
}
