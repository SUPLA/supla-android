package org.supla.android.usecases.channel
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

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.ValuesFormatter
import org.supla.android.db.Channel
import org.supla.android.db.ChannelValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

@RunWith(MockitoJUnitRunner::class)
class GetChannelValueUseCaseTest {

  @Mock
  private lateinit var valuesFormatter: ValuesFormatter

  @InjectMocks
  private lateinit var useCase: GetChannelValueUseCase

  @Test
  fun `should get no value text when channel offline`() {
    // given
    val value: ChannelValue = mockk()
    every { value.onLine } returns false

    val channel: Channel = mockk()
    every { channel.value } returns value

    // when
    val valueText = useCase(channel)

    // then
    assertThat(valueText).isEqualTo(ValuesFormatter.NO_VALUE_TEXT)
  }

  @Test
  fun `should get temperature for thermometer`() {
    // given
    val temperature = 23.2
    val temperatureString = "23.2"
    val value: ChannelValue = mockk()
    every { value.onLine } returns true
    every { value.getTemp(SUPLA_CHANNELFNC_THERMOMETER) } returns temperature

    val channel: Channel = mockk()
    every { channel.value } returns value
    every { channel.func } returns SUPLA_CHANNELFNC_THERMOMETER

    whenever(valuesFormatter.getTemperatureString(temperature)).thenReturn(temperatureString)

    // when
    val valueText = useCase(channel)

    // then
    assertThat(valueText).isEqualTo(temperatureString)
  }

  @Test
  fun `should get temperature for humidity and temperature`() {
    // given
    val temperature = 23.2
    val temperatureString = "23.2"
    val value: ChannelValue = mockk()
    every { value.onLine } returns true
    every { value.getTemp(SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) } returns temperature

    val channel: Channel = mockk()
    every { channel.value } returns value
    every { channel.func } returns SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

    whenever(valuesFormatter.getTemperatureString(temperature)).thenReturn(temperatureString)

    // when
    val valueText = useCase(channel)

    // then
    assertThat(valueText).isEqualTo(temperatureString)
  }

  @Test
  fun `should get humidity for humidity and temperature`() {
    // given
    val humidity = 23.2
    val humidityString = "23.2"
    val value: ChannelValue = mockk()
    every { value.onLine } returns true
    every { value.humidity } returns humidity

    val channel: Channel = mockk()
    every { channel.value } returns value
    every { channel.func } returns SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

    whenever(valuesFormatter.getHumidityString(humidity)).thenReturn(humidityString)

    // when
    val valueText = useCase(channel, ValueType.SECOND)

    // then
    assertThat(valueText).isEqualTo(humidityString)
  }
}
