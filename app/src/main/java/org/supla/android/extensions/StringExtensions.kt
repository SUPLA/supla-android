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

import androidx.compose.ui.graphics.Color
import java.util.Locale

fun String.ucFirst(locale: Locale = Locale.getDefault()) = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(locale) else it.toString()
}

fun String.skipQuotation() =
  if (startsWith("\"") && endsWith("\"")) {
    substring(1, length - 1)
  } else {
    this
  }

/**
 * Converts a hexadecimal string representation (e.g., "#RRGGBB") to an RGB Color object.
 */
fun String.toColor(): Color {
  return toColorOrNull() ?: throw IllegalArgumentException("Invalid hex string format: $this. Must be RRGGBB.")
}

fun String.toColorOrNull(): Color? {
  var hex = this
  if (hex.startsWith("#")) {
    hex = hex.substring(1)
  }
  if (hex.length == 6) {
    val r = hex.take(2).toInt(16) / 255f
    val g = hex.substring(2, 4).toInt(16) / 255f
    val b = hex.substring(4, 6).toInt(16) / 255f
    return Color(r, g, b)
  }

  if (hex.length == 3) {
    val r = hex.take(1).toInt(16) / 255f
    val g = hex[1].code / 255f
    val b = hex[2].code / 255f
    return Color(r, g, b)
  }

  return null
}

fun String.filterHexDigits(): String =
  this.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
