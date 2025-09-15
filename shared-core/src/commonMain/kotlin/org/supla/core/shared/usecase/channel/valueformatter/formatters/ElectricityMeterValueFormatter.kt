package org.supla.core.shared.usecase.channel.valueformatter.formatters
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
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatSpecification
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.InvalidValue
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision
import kotlin.math.abs

class ElectricityMeterValueFormatter(
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Companion.ElectricityMeter
) : ValueFormatter() {
  override val invalidValue: InvalidValue = InvalidValue.Companion.ElectricityMeter

  override fun format(value: Any?): String =
    format(
      value = value.toDouble(),
      precision = getPrecision(value),
      unit = defaultFormatSpecification.withUnit.ifTrue { defaultFormatSpecification.unit },
      predecessor = defaultFormatSpecification.predecessor,
      showNoValueText = defaultFormatSpecification.showNoValueText
    )

  override fun format(value: Any?, format: ValueFormat): String =
    format(
      value = value.toDouble(),
      precision = format.precision(getPrecision(value)),
      unit = extractUnit(format),
      predecessor = format.predecessor ?: defaultFormatSpecification.predecessor,
      showNoValueText = format.showNoValueText ?: defaultFormatSpecification.showNoValueText
    )

  private fun getPrecision(value: Any?): ValuePrecision {
    val doubleValue = when (value) {
      is Double -> value
      is Float -> value.toDouble()
      is Int -> value.toDouble()
      else -> 0.0
    }

    return if (abs(doubleValue) < 100) {
      ValuePrecision.Companion.exact(2)
    } else if (abs(doubleValue) < 1000) {
      ValuePrecision.Companion.exact(1)
    } else {
      ValuePrecision.Companion.exact(0)
    }
  }
}
