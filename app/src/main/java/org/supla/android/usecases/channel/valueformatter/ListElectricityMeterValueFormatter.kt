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

import org.supla.android.data.ValuesFormatter.Companion.NO_VALUE_TEXT
import org.supla.android.extensions.ifLet
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.channel.valueprovider.ElectricityMeterValueProvider
import java.text.DecimalFormat

class ListElectricityMeterValueFormatter : BaseElectricityMeterValueFormatter() {

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision): String {
    ifLet(value as? Double) { (value) -> return formatDouble(value, withUnit, precision) }
    ifLet(value as? Float) { (value) -> return formatFloat(value, withUnit, precision) }

    return NO_VALUE_TEXT
  }
}

class ChartElectricityMeterValueFormatter : BaseElectricityMeterValueFormatter() {
  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision): String {
    ifLet(value as? Double) { (value) -> return formatDouble(value, withUnit, precision, checkNoValue = false) }
    ifLet(value as? Float) { (value) -> return formatFloat(value, withUnit, precision, checkNoValue = false) }

    return formatFloat(0f, withUnit, precision, checkNoValue = false)
  }
}

abstract class BaseElectricityMeterValueFormatter : ChannelValueFormatter {

  val formatter: DecimalFormat = DecimalFormat()

  override fun handle(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER

  protected fun formatDouble(
    value: Double,
    withUnit: Boolean,
    precision: ChannelValueFormatter.Precision,
    checkNoValue: Boolean = true
  ): String {
    if (checkNoValue && value == ElectricityMeterValueProvider.UNKNOWN_VALUE) {
      return NO_VALUE_TEXT
    }

    val precisionValue = getPrecision(value.toFloat(), precision)
    formatter.minimumFractionDigits = precisionValue
    formatter.maximumFractionDigits = precisionValue
    return if (withUnit) {
      "${formatter.format(value)} kWh"
    } else {
      formatter.format(value)
    }
  }

  protected fun formatFloat(
    value: Float,
    withUnit: Boolean,
    precision: ChannelValueFormatter.Precision,
    checkNoValue: Boolean = true
  ): String {
    if (checkNoValue && value == ElectricityMeterValueProvider.UNKNOWN_VALUE.toFloat()) {
      return NO_VALUE_TEXT
    }

    val precisionValue = getPrecision(value, precision)
    formatter.minimumFractionDigits = precisionValue
    formatter.maximumFractionDigits = precisionValue
    return if (withUnit) {
      "${formatter.format(value)} kWh"
    } else {
      formatter.format(value)
    }
  }

  private fun getPrecision(value: Float, precision: ChannelValueFormatter.Precision): Int =
    when (precision) {
      is ChannelValueFormatter.Default ->
        if (value < 100) {
          2
        } else if (value < 1000) {
          1
        } else {
          0
        }

      is ChannelValueFormatter.Custom -> precision.value
    }
}
