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

import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatSpecification
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.InvalidValue
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

object DistanceValueFormatter : ValueFormatter() {
  override val invalidValue: InvalidValue = InvalidValue.Distance
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Default

  override fun format(value: Any?): String {
    val doubleValue = value.toDouble()
    return when {
      doubleValue >= 1000 -> DistanceKiloValueFormatter.format(doubleValue / 1000.0)
      doubleValue >= 1 -> DistanceDefaultValueFormatter.format(value)
      doubleValue >= 0.01 -> DistanceCentiValueFormatter.format(doubleValue * 100.0)
      else -> DistanceMilliValueFormatter.format(doubleValue * 1000.0)
    }
  }

  override fun format(value: Any?, format: ValueFormat): String {
    val doubleValue = value.toDouble()
    return when {
      doubleValue >= 1000 -> DistanceKiloValueFormatter.format(doubleValue / 1000.0, format)
      doubleValue >= 1 -> DistanceDefaultValueFormatter.format(value, format)
      doubleValue >= 0.01 -> DistanceCentiValueFormatter.format(doubleValue * 100.0, format)
      else -> DistanceMilliValueFormatter.format(doubleValue * 1000.0, format)
    }
  }
}

private object DistanceMilliValueFormatter : ValueFormatter() {
  override val invalidValue: InvalidValue = InvalidValue.Distance
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Companion.DistanceMilli
}

private object DistanceCentiValueFormatter : ValueFormatter() {
  override val invalidValue: InvalidValue = InvalidValue.Distance
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Companion.DistanceCenti
}

private object DistanceDefaultValueFormatter : ValueFormatter() {
  override val invalidValue: InvalidValue = InvalidValue.Distance
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Companion.DistanceDefault
}

private object DistanceKiloValueFormatter : ValueFormatter() {
  override val invalidValue: InvalidValue = InvalidValue.Distance
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Companion.DistanceKilo
}
