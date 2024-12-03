package org.supla.core.shared.extensions
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

import org.kotlincrypto.endians.LittleEndian

fun ByteArray.toShort(first: Int = 0, second: Int = 1): Short {
  return LittleEndian.bytesToShort(this[first], this[second])
}

fun ByteArray.toTemperature(first: Int = 0, second: Int = 1): Float {
  return toShort(first, second).fromSuplaTemperature()
}

@OptIn(ExperimentalStdlibApi::class)
fun ByteArray.toHex(separator: String = ""): String = joinToString(separator = separator) {
  it.toHexString(HexFormat { })
}
