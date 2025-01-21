package org.supla.android.usecases.channel.valueformatter
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

import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.ifLet
import java.text.DecimalFormat

object CurrentValueFormatter : ChannelValueFormatter {

  val formatter: DecimalFormat = DecimalFormat().apply {
    maximumFractionDigits = 1
    minimumFractionDigits = 1
  }

  override fun handle(function: Int): Boolean {
    throw IllegalStateException("Not expected to be called")
  }

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    formatter.maximumFractionDigits = precision.value
    formatter.minimumFractionDigits = precision.value

    ifLet(value as? Double) { (doubleValue) ->
      return format(doubleValue, withUnit)
    }
    ifLet(value as? Float) { (floatValue) ->
      return format(floatValue.toDouble(), withUnit)
    }

    return ValuesFormatter.NO_VALUE_TEXT
  }

  private fun format(value: Double, withUnit: Boolean): String =
    if (withUnit) {
      "${formatter.format(value)} A"
    } else {
      formatter.format(value)
    }
}
