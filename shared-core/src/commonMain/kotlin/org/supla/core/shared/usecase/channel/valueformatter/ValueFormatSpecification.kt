package org.supla.core.shared.usecase.channel.valueformatter
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

import org.supla.core.shared.usecase.channel.valueformatter.types.DefaultPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.DistanceCentiPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.DistanceDefaultPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.DistanceMilliPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.GpmPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.ImpulseCounterPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.PercentageDefaultPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.PressurePrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.RainPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueUnit
import org.supla.core.shared.usecase.channel.valueformatter.types.VoltagePrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.WeightDefaultPrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.WeightKiloPrecision

data class ValueFormatSpecification(
  val precision: ValuePrecision,
  val withUnit: Boolean = false,
  val unit: String? = null,
  val predecessor: String? = null,
  val showNoValueText: Boolean = true
) {
  companion object Companion {
    val Default =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        withUnit = false,
        showNoValueText = false
      )

    val ThermometerDefault =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        unit = ValueUnit.TEMPERATURE_DEGREE.toString(),
      )
    val Gpm =
      ValueFormatSpecification(
        precision = GpmPrecision
      )
    val Humidity =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        unit = ValueUnit.HUMIDITY.toString()
      )
    val ImpulseCounter =
      ValueFormatSpecification(
        precision = ImpulseCounterPrecision
      )
    val ElectricityMeter =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        unit = ValueUnit.ELECTRICITY_METER.toString()
      )
    val ElectricityMeterForGeneral =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        withUnit = true,
        unit = ValueUnit.ELECTRICITY_METER.toString(),
        showNoValueText = false
      )
    val ElectricityMeterForChartSummary =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        showNoValueText = false
      )
    val PowerActive =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        unit = ValueUnit.POWER_ACTIVE.toString()
      )
    val Voltage =
      ValueFormatSpecification(
        precision = VoltagePrecision,
        unit = ValueUnit.VOLTAGE.toString()
      )
    val Pressure =
      ValueFormatSpecification(
        precision = PressurePrecision,
        unit = ValueUnit.PRESSURE.toString()
      )
    val Rain =
      ValueFormatSpecification(
        precision = RainPrecision,
        unit = ValueUnit.RAIN.toString()
      )
    val Wind =
      ValueFormatSpecification(
        precision = DefaultPrecision,
        unit = ValueUnit.WIND.toString()
      )
    val WeightKilo =
      ValueFormatSpecification(
        precision = WeightKiloPrecision,
        unit = ValueUnit.WEIGHT_KILO.toString()
      )
    val WeightDefault =
      ValueFormatSpecification(
        precision = WeightDefaultPrecision,
        unit = ValueUnit.WEIGHT_DEFAULT.toString()
      )
    val DistanceMilli =
      ValueFormatSpecification(
        precision = DistanceMilliPrecision,
        unit = ValueUnit.DISTANCE_MILLI.toString()
      )
    val DistanceCenti =
      ValueFormatSpecification(
        precision = DistanceCentiPrecision,
        unit = ValueUnit.DISTANCE_CENTI.toString()
      )
    val DistanceDefault =
      ValueFormatSpecification(
        precision = DistanceDefaultPrecision,
        unit = ValueUnit.DISTANCE_DEFAULT.toString()
      )
    val DistanceKilo =
      ValueFormatSpecification(
        precision = DistanceDefaultPrecision,
        unit = ValueUnit.DISTANCE_KILO.toString()
      )
    val Percentage =
      ValueFormatSpecification(
        precision = PercentageDefaultPrecision,
        withUnit = true,
        unit = ValueUnit.PERCENTAGE.toString()
      )
  }
}
