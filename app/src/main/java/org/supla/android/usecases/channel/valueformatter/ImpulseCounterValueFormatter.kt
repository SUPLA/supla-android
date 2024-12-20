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
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaConst
import java.util.Locale

class ImpulseCounterValueFormatter : ChannelValueFormatter {
  override fun handle(function: Int): Boolean =
    when (function) {
      SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER,
      SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER,
      SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER,
      SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER -> true

      else -> false
    }

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    val (doubleValue) = guardLet(value as? Double) { return ValuesFormatter.NO_VALUE_TEXT }
    val unit = (custom as? Data)?.unit
    val precisionValue = getPrecisionValue(precision)

    return if (withUnit && unit != null) {
      String.format(Locale.getDefault(), "%.${precisionValue}f $unit", doubleValue)
    } else {
      String.format(Locale.getDefault(), "%.${precisionValue}f", doubleValue)
    }
  }

  private fun getPrecisionValue(precision: ChannelValueFormatter.Precision): Int =
    when (precision) {
      is ChannelValueFormatter.Default -> 3
      is ChannelValueFormatter.Custom -> precision.value
    }

  data class Data(val unit: String)
}
