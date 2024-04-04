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

import org.supla.android.extensions.ifLet

enum class WindowGroupedValueFormat {
  OPENING_PERCENTAGE, PERCENTAGE, DEGREE
}

sealed class WindowGroupedValue private constructor(open val value: Float) {

  open fun asString(format: WindowGroupedValueFormat, value0: Float? = null, value100: Float? = null): String = when (format) {
    WindowGroupedValueFormat.OPENING_PERCENTAGE -> String.format("%.0f%%", 100f - value)
    WindowGroupedValueFormat.PERCENTAGE -> String.format("%.0f%%", value)
    WindowGroupedValueFormat.DEGREE -> {
      ifLet(value0, value100) { (value0, value100) ->
        return String.format("%.0f°", valueToAngle(value, value0, value100))
      }

      String.format("%.0f%%", value)
    }
  }

  fun asAngle(value0: Float, value100: Float) = valueToAngle(value, value0, value100)

  protected fun valueToAngle(value: Float, value0: Float, value100: Float) =
    value0.plus(value100.minus(value0).times(value).div(100f))

  data class Different(
    private val min: Float,
    private val max: Float
  ) : WindowGroupedValue(0f) {

    override fun asString(format: WindowGroupedValueFormat, value0: Float?, value100: Float?): String = when (format) {
      WindowGroupedValueFormat.OPENING_PERCENTAGE -> String.format("%.0f%% - %.0f%%", 100f - min, 100f - max)
      WindowGroupedValueFormat.PERCENTAGE -> String.format("%.0f%% - %.0f%%", min, max)
      WindowGroupedValueFormat.DEGREE -> {
        ifLet(value0, value100) { (value0, value100) ->
          return String.format("%.0f° - %.0f°", valueToAngle(min, value0, value100), valueToAngle(max, value0, value100))
        }

        String.format("%.0f%% - %.0f%%", min, max)
      }
    }
  }

  object Invalid : WindowGroupedValue(0f)

  data class Similar(override val value: Float) : WindowGroupedValue(value)
}
