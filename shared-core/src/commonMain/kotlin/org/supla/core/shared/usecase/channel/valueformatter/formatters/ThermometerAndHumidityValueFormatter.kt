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

import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.storage.ApplicationPreferences
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatSpecification
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.InvalidValue
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

class ThermometerAndHumidityValueFormatter(
  preferences: ApplicationPreferences,
  override val defaultFormatSpecification: ValueFormatSpecification = ValueFormatSpecification.Companion.Default
) : ValueFormatter() {

  override val invalidValue: InvalidValue = InvalidValue.None

  private val thermometerValueFormatter = ThermometerValueFormatter(preferences)
  private val humidityValueFormatter = HumidityValueFormatter()

  override fun format(value: Any?, format: ValueFormat): String {
    val (temperatureAndHumidity) = guardLet(value as? TemperatureAndHumidity) { return NO_VALUE_TEXT }

    val temperatureString = thermometerValueFormatter.format(temperatureAndHumidity.temperature, format)
    val humidityString = humidityValueFormatter.format(temperatureAndHumidity.humidity, format)

    return "$temperatureString\n$humidityString"
  }
}
