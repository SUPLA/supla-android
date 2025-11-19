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
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

data class HsvColor(
  val hue: Float = 0f,
  val saturation: Float = 0f,
  val value: Float = 0f
) {
  val color: Color = Color.fromHsv(hue, saturation, value)
  val fullBrightnessColor = Color.fromHsv(hue, saturation, 1f)
  val valueAsPercentage = value.times(100).roundToInt().coerceIn(0, 100)
}

/**
 * Converts an RGB Color object to HSV (Hue, Saturation, Value/Brightness) components.
 * Hue is in degrees (0-360), Saturation and Value are in the range [0, 1].
 */
fun Color.toHsv(brightness: Int? = null): HsvColor {
  val r = red
  val g = green
  val b = blue

  val cMax = maxOf(r, g, b)
  val cMin = minOf(r, g, b)
  val delta = cMax - cMin

  val h: Float
  val s: Float
  val v = brightness?.div(100f)?.coerceIn(0f, 1f) ?: cMax // Value is equal to the maximum component

  if (delta == 0f) {
    h = 0f
    s = 0f
  } else {
    s = delta / cMax
    h = when (cMax) {
      r -> (60 * (((g - b) / delta) % 6) + 360) % 360
      g -> (60 * ((b - r) / delta) + 120)
      else -> (60 * ((r - g) / delta) + 240)
    }
  }
  return HsvColor(h, s, v)
}

fun Color.applyBrightness(brightness: Int): Color =
  toHsv().let { (hue, saturation, _) ->
    Color.fromHsv(hue, saturation, brightness.div(100f))
  }

/**
 * Converts HSV (Hue, Saturation, Value/Brightness) color components to an RGB Color object.
 * Hue is in degrees (0-360), Saturation and Value are in the range [0, 1].
 */
fun Color.Companion.fromHsv(hue: Float, saturation: Float, value: Float): Color {
  if (saturation == 0f) return Color(value, value, value) // achromatic (gray)

  val hPrime = hue / 60f
  val c = value * saturation // Chroma
  val x = c * (1f - abs(hPrime % 2f - 1f))
  val m = value - c

  val (red, green, blue) = when (floor(hPrime).toInt() % 6) {
    0 -> Triple(c, x, 0f)
    1 -> Triple(x, c, 0f)
    2 -> Triple(0f, c, x)
    3 -> Triple(0f, x, c)
    4 -> Triple(x, 0f, c)
    5 -> Triple(c, 0f, x)
    else -> Triple(0f, 0f, 0f)
  }

  return Color(
    red = (red + m).coerceIn(0f, 1f),
    green = (green + m).coerceIn(0f, 1f),
    blue = (blue + m).coerceIn(0f, 1f)
  )
}

/**
 * Converts an RGB Color object to its hexadecimal string representation (e.g., "#RRGGBB").
 */
fun Color.toHexString(): String {
  val r = (red * 255).roundToInt().coerceIn(0, 255)
  val g = (green * 255).roundToInt().coerceIn(0, 255)
  val b = (blue * 255).roundToInt().coerceIn(0, 255)
  return String.format("#%02X%02X%02X", r, g, b)
}
