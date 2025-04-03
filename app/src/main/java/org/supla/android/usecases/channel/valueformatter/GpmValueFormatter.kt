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
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaConst
import java.text.DecimalFormat

class GpmValueFormatter(
  config: SuplaChannelGeneralPurposeBaseConfig?
) : ChannelValueFormatter {

  private val beforeValue: String = config?.unitBeforeValue?.let { if (config.noSpaceBeforeValue) it else "$it " } ?: ""
  private val afterValue: String = config?.unitAfterValue?.let { if (config.noSpaceAfterValue) it else " $it" } ?: ""
  private val valueFormatter: DecimalFormat = DecimalFormat().apply {
    minimumFractionDigits = config?.valuePrecision ?: 2
    maximumFractionDigits = config?.valuePrecision ?: 2
  }
  private val labelFormatter = DecimalFormat()

  override fun handle(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT ||
      function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    val (doubleValue) = guardLet(value as? Double) { return ValuesFormatter.NO_VALUE_TEXT }
    if (doubleValue.isNaN()) {
      return ValuesFormatter.NO_VALUE_TEXT
    }

    return if (withUnit) {
      "$beforeValue${valueFormatter.format(doubleValue)}$afterValue"
    } else {
      valueFormatter.format(doubleValue)
    }
  }

  override fun formatChartLabel(value: Any, precision: Int, withUnit: Boolean): String {
    val (doubleValue) = guardLet(value as? Double) { return ValuesFormatter.NO_VALUE_TEXT }
    if (doubleValue.isNaN()) {
      return ValuesFormatter.NO_VALUE_TEXT
    }
    labelFormatter.minimumFractionDigits = precision
    labelFormatter.maximumFractionDigits = precision

    return if (withUnit) {
      "$beforeValue${labelFormatter.format(doubleValue)}$afterValue"
    } else {
      labelFormatter.format(doubleValue)
    }
  }
}
