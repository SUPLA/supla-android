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

private const val RGB_VALUE_LENGTH = 8

data class RgbwwValue(
  override val status: SuplaChannelAvailabilityStatus,
  override val on: Boolean,
  override val brightness: Int,
  override val colorBrightness: Int,
  override val red: Int,
  override val green: Int,
  override val blue: Int,
  override val cct: Int
) : RgbBaseValue, DimmerCctBaseValue {

  val rgb: Int
    get() = (red and 0x00000FF) or ((green shl 8) and 0x0000FF00) or ((blue shl 16) and 0x00FF0000)

  companion object {
    fun from(status: SuplaChannelAvailabilityStatus, bytes: ByteArray): RgbwwValue {
      if (bytes.size < RGB_VALUE_LENGTH) {
        return RgbwwValue(status, false, 0, 0, 0, 0, 0, 0)
      }

      return RgbwwValue(
        status = status,
        on = bytes[0] >= 1,
        brightness = bytes[0].toInt().coerceIn(0, 100),
        colorBrightness = bytes[1].toInt().coerceIn(0, 100),
        red = bytes[4].toInt() and 0xFF,
        green = bytes[3].toInt() and 0xFF,
        blue = bytes[2].toInt() and 0xFF,
        cct = bytes[7].toInt().coerceIn(0, 100)
      )
    }
  }
}
