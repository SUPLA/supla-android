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
import kotlin.math.abs

class ListElectricityMeterValueFormatter : BaseElectricityMeterValueFormatter() {

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    val unit = (custom as? SuplaElectricityMeasurementType)?.unit ?: "kWh"
    val checkNoValue = custom == SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY
    ifLet(value as? Double) { (value) ->
      return formatDouble(value, if (withUnit) unit else "", { getPrecision(it, precision) }, checkNoValue)
    }
    ifLet(value as? Float) { (value) ->
      return formatFloat(value, if (withUnit) unit else "", { getPrecision(it, precision) }, checkNoValue)
    }

    return NO_VALUE_TEXT
  }
}

class ChartMarkerElectricityMeterValueFormatter : BaseElectricityMeterValueFormatter() {
  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    ifLet(value as? Double) { (value) ->
      return formatDouble(
        value,
        if (withUnit) "kWh" else "",
        precisionProvider = { getPrecision(it, precision) },
        checkNoValue = false
      )
    }
    ifLet(value as? Float) { (value) ->
      return formatFloat(
        value,
        if (withUnit) "kWh" else "",
        precisionProvider = { getPrecision(it, precision) },
        checkNoValue = false
      )
    }

    return formatFloat(0f, if (withUnit) "kWh" else "", precisionProvider = { getPrecision(it, precision) }, checkNoValue = false)
  }
}

class ChartAxisElectricityMeterValueFormatter : BaseElectricityMeterValueFormatter() {
  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    ifLet(value as? Double) { (value) ->
      return formatDouble(
        value,
        if (withUnit) "kWh" else "",
        precisionProvider = { getAxisPrecision(it) },
        checkNoValue = false
      )
    }
    ifLet(value as? Float) { (value) ->
      return formatFloat(
        value,
        if (withUnit) "kWh" else "",
        precisionProvider = { getAxisPrecision(it) },
        checkNoValue = false
      )
    }

    return formatFloat(0f, if (withUnit) "kWh" else "", precisionProvider = { 0 }, checkNoValue = false)
  }

  private fun getAxisPrecision(value: Float): Int =
    when {
      value == 0f -> 0
      abs(value) < 10f -> 1
      else -> 0
    }
}

abstract class BaseElectricityMeterValueFormatter : ChannelValueFormatter {

  val formatter: DecimalFormat = DecimalFormat()

  override fun handle(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER

  protected fun formatDouble(
    value: Double,
    unit: String,
    precisionProvider: (Float) -> Int,
    checkNoValue: Boolean = true
  ): String {
    if (checkNoValue && value == ElectricityMeterValueProvider.UNKNOWN_VALUE) {
      return NO_VALUE_TEXT
    }

    val precisionValue = precisionProvider(value.toFloat())
    formatter.minimumFractionDigits = precisionValue
    formatter.maximumFractionDigits = precisionValue
    return "${formatter.format(value)} $unit"
  }

  protected fun formatFloat(
    value: Float,
    unit: String,
    precisionProvider: (Float) -> Int,
    checkNoValue: Boolean = true
  ): String {
    if (checkNoValue && value == ElectricityMeterValueProvider.UNKNOWN_VALUE.toFloat()) {
      return NO_VALUE_TEXT
    }

    val precisionValue = precisionProvider(value)
    formatter.minimumFractionDigits = precisionValue
    formatter.maximumFractionDigits = precisionValue
    return "${formatter.format(value)} $unit"
  }

  protected fun getPrecision(value: Float, precision: ChannelValueFormatter.Precision): Int =
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
