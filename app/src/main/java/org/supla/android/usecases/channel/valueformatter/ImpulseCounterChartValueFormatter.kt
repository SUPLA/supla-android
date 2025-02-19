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

import org.supla.android.Trace
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaConst

class ImpulseCounterChartValueFormatter(
  private val unit: String?
) : ChannelValueFormatter {
  override fun handle(function: Int): Boolean =
    when (function) {
      SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER,
      SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER,
      SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER,
      SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER -> true

      else -> false
    }

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    (value as? Double)?.let {
      return toString(it, withUnit, precision)
    }
    (value as? Float)?.let {
      return toString(it.toDouble(), withUnit, precision)
    }

    Trace.d(TAG, "Unexpected value format: `$value`")
    return ValuesFormatter.NO_VALUE_TEXT
  }

  private fun toString(value: Double, withUnit: Boolean, precision: ChannelValueFormatter.Precision): String {
    return if (unit != null && withUnit) {
      String.format("%.${precision.value}f $unit", value)
    } else {
      String.format("%.${precision.value}f", value)
    }
  }
}
