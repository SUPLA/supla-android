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

import org.supla.core.shared.data.model.thermometer.TemperatureUnit
import org.supla.core.shared.infrastructure.storage.ApplicationPreferences
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatSpecification
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.InvalidValue
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueUnit

class ThermometerValueFormatter(
  private val preferences: ApplicationPreferences,
) : ValueFormatter() {

  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Companion.ThermometerDefault

  override val invalidValue: InvalidValue = InvalidValue.Companion.Thermometer

  override fun format(value: Any?, format: ValueFormat): String {
    val unit = when (format.withUnit) {
      true if format.customUnit != null -> format.customUnit
      true -> preferences.temperatureUnit.valueUnit.toString()
      else -> null
    }

    return format(
      value = value.toDouble(),
      precision = format.precision(ValuePrecision.exact(preferences.temperaturePrecision)),
      unit = unit,
      showNoValueText = format.showNoValueText ?: defaultFormatSpecification.showNoValueText
    )
  }

  override fun preprocessValue(value: Double): Double {
    if (invalidValue.isEqualTo(value)) {
      return value
    }

    return when (preferences.temperatureUnit) {
      TemperatureUnit.CELSIUS -> value
      TemperatureUnit.FAHRENHEIT -> 9.0 / 5.0 * value + 32.0
    }
  }
}
