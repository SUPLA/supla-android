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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.core.shared.data.model.thermometer.TemperatureUnit
import org.supla.core.shared.infrastructure.storage.ApplicationPreferences
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision

class ThermometerValueFormatterTest {

  @MockK
  private lateinit var preferences: ApplicationPreferences

  @InjectMockKs
  private lateinit var formatter: ThermometerValueFormatter

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should format celsius temperature`() {
    // given
    val temperature = 20f
    every { preferences.temperatureUnit } returns TemperatureUnit.CELSIUS
    every { preferences.temperaturePrecision } returns 1

    // when
    val temperatureString = formatter.format(temperature, ValueFormat.WithUnit)

    // then
    assertThat(temperatureString).isEqualTo("20.0 °C")
  }

  @Test
  fun `should format fahrenheit temperature`() {
    // given
    val temperature = 20f
    every { preferences.temperatureUnit } returns TemperatureUnit.FAHRENHEIT
    every { preferences.temperaturePrecision } returns 1

    // when
    val temperatureString = formatter.format(temperature, ValueFormat.WithUnit)

    // then
    assertThat(temperatureString).isEqualTo("68.0 °F")
  }

  @Test
  fun `should format without unit`() {
    // given
    val temperature = 20f
    every { preferences.temperatureUnit } returns TemperatureUnit.CELSIUS
    every { preferences.temperaturePrecision } returns 2

    // when
    val temperatureString = formatter.format(temperature, ValueFormat.WithoutUnit)

    // then
    assertThat(temperatureString).isEqualTo("20.00")
  }

  @Test
  fun `should format with degree only`() {
    // given
    val temperature = 20f
    every { preferences.temperatureUnit } returns TemperatureUnit.CELSIUS
    every { preferences.temperaturePrecision } returns 2

    // when
    val temperatureString = formatter.format(temperature, ValueFormat.TemperatureWithDegree)

    // then
    assertThat(temperatureString).isEqualTo("20.00°")
  }

  @Test
  fun `should format with custom unit`() {
    // given
    val temperature = 20f
    every { preferences.temperatureUnit } returns TemperatureUnit.CELSIUS
    every { preferences.temperaturePrecision } returns 2

    // when
    val format = ValueFormat(
      withUnit = true,
      precision = ValueFormat.Precision.Custom(ValuePrecision.exact(5)),
      customUnit = "K"
    )
    val temperatureString = formatter.format(temperature, format)

    // then
    assertThat(temperatureString).isEqualTo("20.00000K")
  }
}
