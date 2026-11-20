package org.supla.android.features.details.windowdetail.base.data
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

import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.extensions.ifLet
import java.util.Locale

enum class WindowGroupedValueFormat {
  OPENING_PERCENTAGE, PERCENTAGE, DEGREE
}

enum class ValueDestination {
  WINDOW, SLIDER
}

sealed class WindowGroupedValue(protected open val value: Float?) {

  abstract fun value(destination: ValueDestination): Float?

  open fun asString(format: WindowGroupedValueFormat, value0: Float? = null, value100: Float? = null): String {
    val (value) = guardLet(value) { return "" }
    return when (format) {
      WindowGroupedValueFormat.OPENING_PERCENTAGE -> String.format(Locale.getDefault(), "%.0f%%", 100f - value)
      WindowGroupedValueFormat.PERCENTAGE -> String.format(Locale.getDefault(), "%.0f%%", value)
      WindowGroupedValueFormat.DEGREE -> {
        ifLet(value0, value100) { (value0, value100) ->
          return String.format(Locale.getDefault(), "%.0f°", value.angleBetween(value0, value100))
        }

        String.format(Locale.getDefault(), "%.0f%%", value)
      }
    }
  }

  fun asAngle(value0: Float, value100: Float): Float? = value?.angleBetween(value0, value100)

  data class Different(
    private val min: Float,
    private val max: Float,
  ) : WindowGroupedValue(null) {

    override fun value(destination: ValueDestination): Float? =
      when (destination) {
        ValueDestination.WINDOW -> max
        ValueDestination.SLIDER -> null
      }

    override fun asString(format: WindowGroupedValueFormat, value0: Float?, value100: Float?): String = when (format) {
      WindowGroupedValueFormat.OPENING_PERCENTAGE -> String.format(Locale.getDefault(), "%.0f%% - %.0f%%", 100f - max, 100f - min)
      WindowGroupedValueFormat.PERCENTAGE -> String.format(Locale.getDefault(), "%.0f%% - %.0f%%", min, max)
      WindowGroupedValueFormat.DEGREE -> {
        ifLet(value0, value100) { (value0, value100) ->
          return String.format(
            Locale.getDefault(),
            "%.0f° - %.0f°",
            min.angleBetween(value0, value100),
            max.angleBetween(value0, value100)
          )
        }

        String.format(Locale.getDefault(), "%.0f%% - %.0f%%", min, max)
      }
    }
  }

  object Invalid : WindowGroupedValue(null) {
    override fun value(destination: ValueDestination): Float? = null
  }

  data class Similar(override val value: Float) : WindowGroupedValue(value) {
    override fun value(destination: ValueDestination): Float = value
  }
}

fun Float.angleBetween(angle0: Float, angle100: Float): Float =
  angle0.plus(angle100.minus(angle0).times(this).div(100f))
