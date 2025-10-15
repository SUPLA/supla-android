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

import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.logging.Logger
import org.supla.core.shared.usecase.channel.valueformatter.types.InvalidValue
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat.Precision
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision

private const val TAG = "SharedCore.ValueFormatter"
const val NO_VALUE_TEXT = "---"

abstract class ValueFormatter {

  protected abstract val invalidValue: InvalidValue
  protected abstract val defaultFormatSpecification: ValueFormatSpecification

  private val formatter = DecimalFormatter()

  open fun format(value: Any?): String =
    format(
      value = value.toDouble(),
      precision = defaultFormatSpecification.precision,
      unit = defaultFormatSpecification.withUnit.ifTrue { defaultFormatSpecification.unit },
      predecessor = defaultFormatSpecification.predecessor,
      showNoValueText = defaultFormatSpecification.showNoValueText
    )

  open fun format(value: Any?, format: ValueFormat): String =
    format(
      value = value.toDouble(),
      precision = format.precision(defaultFormatSpecification.precision),
      unit = extractUnit(format),
      predecessor = format.predecessor ?: defaultFormatSpecification.predecessor,
      showNoValueText = format.showNoValueText ?: defaultFormatSpecification.showNoValueText
    )

  protected fun format(
    value: Double,
    precision: ValuePrecision,
    unit: String? = null,
    predecessor: String? = null,
    showNoValueText: Boolean = true
  ): String {
    if (showNoValueText && invalidValue.isEqualTo(value)) {
      return NO_VALUE_TEXT
    }

    val stringValue = precision.valueToString(value)

    return when {
      predecessor != null && unit != null ->
        "${predecessor}${stringValue}$unit"

      predecessor != null ->
        "${predecessor}$stringValue"

      unit != null ->
        "${stringValue}$unit"

      else -> stringValue
    }
  }

  protected open fun preprocessValue(value: Double): Double = value

  protected fun extractUnit(format: ValueFormat): String? =
    format.withUnit?.let { it.ifTrue { format.customUnit ?: defaultFormatSpecification.unit } }
      ?: defaultFormatSpecification.withUnit.ifTrue { defaultFormatSpecification.unit }

  protected fun ValueFormat.precision(default: ValuePrecision): ValuePrecision =
    when (this.precision) {
      Precision.Default -> default
      is Precision.Custom -> this.precision.valuePrecision
    }

  protected fun Any?.toDouble(): Double =
    when (this) {
      is Double -> this
      is Float -> this.toDouble()
      is Int -> this.toDouble()
      else -> {
        Logger.e(TAG, "Unexpected value `$this`")
        invalidValue.value
      }
    }

  private fun ValuePrecision.valueToString(value: Double): String =
    formatter.format(value, min, max)
}
