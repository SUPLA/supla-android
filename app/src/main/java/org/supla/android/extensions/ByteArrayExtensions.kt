package org.supla.android.extensions
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

import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray.toShortVararg(vararg byteIndices: Int): Short {
  return toShort(byteIndices)
}

fun ByteArray.toShort(byteIndices: IntArray): Short {
  val bytes = ByteArray(byteIndices.size)
  byteIndices.sorted().forEachIndexed { index, byte -> bytes[index] = this[byte] }
  return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).short
}

fun ByteArray.toTemperature(vararg byteIndices: Int): Float {
  val bytes = ByteArray(byteIndices.size)
  byteIndices.sorted().forEachIndexed { index, byte -> bytes[index] = this[byte] }
  return toShort(byteIndices).fromSuplaTemperature()
}

fun ByteArray.toHex(): String = joinToString(separator = "") { "%02x".format(it) }
