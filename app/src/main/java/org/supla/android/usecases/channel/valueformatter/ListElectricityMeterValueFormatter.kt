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
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.extensions.ifLet
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.channel.valueprovider.ElectricityMeterValueProvider
import java.text.DecimalFormat

class ListElectricityMeterValueFormatter(private val useNoValue: Boolean? = null) : BaseElectricityMeterValueFormatter() {

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    val unit = (custom as? SuplaElectricityMeasurementType)?.unit ?: "kWh"
    val checkNoValue = useNoValue ?: (custom == SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY)
    ifLet(value as? Double) { (value) ->
      return format(value, if (withUnit) unit else "", getPrecision(value, precision), checkNoValue)
    }
    ifLet(value as? Float) { (value) ->
      return format(value.toDouble(), if (withUnit) unit else "", getPrecision(value.toDouble(), precision), checkNoValue)
    }

    return format(0.0, if (withUnit) unit else "", 0, checkNoValue)
  }
}

class ChartAxisElectricityMeterValueFormatter : BaseElectricityMeterValueFormatter() {
  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    ifLet(value as? Double) { (value) ->
      return format(
        value,
        if (withUnit) "kWh" else "",
        precision = if (value == 0.0) 0 else precision.value,
        checkNoValue = false
      )
    }
    ifLet(value as? Float) { (value) ->
      return format(
        value.toDouble(),
        if (withUnit) "kWh" else "",
        precision = if (value == 0f) 0 else precision.value,
        checkNoValue = false
      )
    }

    return format(0.0, if (withUnit) "kWh" else "", precision = 0, checkNoValue = false)
  }
}

abstract class BaseElectricityMeterValueFormatter : ChannelValueFormatter {

  val formatter: DecimalFormat = DecimalFormat()

  override fun handle(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER

  protected fun format(
    value: Double,
    unit: String,
    precision: Int,
    checkNoValue: Boolean = true
  ): String {
    if (value.isNaN()) {
      // NaN is possible when user selected other type than default (ex voltage) and currently there is no data
      return NO_VALUE_TEXT
    }
    if (checkNoValue && value == ElectricityMeterValueProvider.UNKNOWN_VALUE) {
      return NO_VALUE_TEXT
    }

    formatter.minimumFractionDigits = precision
    formatter.maximumFractionDigits = precision
    return "${formatter.format(value)} $unit"
  }

  protected fun getPrecision(value: Double, precision: ChannelValueFormatter.Precision): Int =
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
