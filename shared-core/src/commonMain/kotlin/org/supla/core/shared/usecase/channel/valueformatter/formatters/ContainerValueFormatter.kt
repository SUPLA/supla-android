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

import org.supla.android.lib.singlecall.ContainerLevel
import org.supla.core.shared.data.model.function.container.ContainerValue
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatSpecification
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.InvalidValue
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

object ContainerValueFormatter : ValueFormatter() {
  override val invalidValue: InvalidValue = InvalidValue.Equals(0.0)
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Percentage

  override fun format(value: Any?): String {
    return format(
      value = value,
      format = ValueFormat()
    )
  }

  override fun format(value: Any?, format: ValueFormat): String {
    val unit =
      if (format.withUnit == true) {
        format.customUnit ?: defaultFormatSpecification.unit
      } else if (defaultFormatSpecification.withUnit) {
        format.customUnit ?: defaultFormatSpecification.unit
      } else {
        null
      }

    (value as? ContainerValue)?.let {
      return if (value.levelKnown) {
        format(value.level, unit)
      } else {
        NO_VALUE_TEXT
      }
    }
    (value as? ContainerLevel)?.let {
      return if (value.levelKnown) {
        format(value.level, unit)
      } else {
        NO_VALUE_TEXT
      }
    }

    (value as? Double)?.let {
      return super.format(value, format)
    }

    (value as? Int)?.let {
      return if (value > 0) {
        format(value - 1, unit)
      } else {
        NO_VALUE_TEXT
      }
    }

    return NO_VALUE_TEXT
  }

  private fun format(
    value: Int,
    unit: String? = null,
  ): String {
    return if (unit != null) {
      "${value}$unit"
    } else {
      "$value"
    }
  }
}
