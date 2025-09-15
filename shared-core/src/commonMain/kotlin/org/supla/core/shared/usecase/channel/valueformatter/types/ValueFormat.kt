package org.supla.core.shared.usecase.channel.valueformatter.types
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

import org.supla.core.shared.extensions.ifTrue

data class ValueFormat(
  val withUnit: Boolean? = null,
  val precision: Precision = Precision.Default,
  val customUnit: String? = null,
  val predecessor: String? = null,
  val showNoValueText: Boolean? = null
) {

  sealed interface Precision {
    data object Default : Precision
    data class Custom(val valuePrecision: ValuePrecision) : Precision
  }

  companion object {
    val WithUnit = ValueFormat(withUnit = true)
    val WithoutUnit = ValueFormat(withUnit = false)
    val ChartDefault = ValueFormat(withUnit = true, showNoValueText = false)
    val TemperatureWithDegree = ValueFormat(withUnit = true, customUnit = ValueUnit.TEMPERATURE_DEGREE.toString())
  }
}

fun custom(precision: ValuePrecision) = ValueFormat.Precision.Custom(precision)

fun customPrecision(precision: Int) =
  ValueFormat(
    precision = ValueFormat.Precision.Custom(ValuePrecision.exact(precision))
  )

fun customUnit(unit: String) = ValueFormat(withUnit = true, customUnit = unit)

fun withUnit(withUnit: Boolean) = if (withUnit) ValueFormat.WithUnit else ValueFormat.WithoutUnit

fun withUnit(withUnit: Boolean, unit: String?, leadingSpace: Boolean = true) =
  ValueFormat(
    withUnit = withUnit && unit != null,
    customUnit = unit?.let { leadingSpace.ifTrue { " $unit" } ?: unit }
  )

fun forChartMarker(unit: String? = null) =
  unit?.let {
    ValueFormat(
      withUnit = true,
      customUnit = " $unit",
      showNoValueText = false
    )
  } ?: ValueFormat.ChartDefault
