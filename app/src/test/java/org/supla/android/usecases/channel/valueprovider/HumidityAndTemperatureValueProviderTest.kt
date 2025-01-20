package org.supla.android.usecases.channel.valueprovider
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
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.ValueType
import org.supla.core.shared.data.model.general.SuplaFunction

@RunWith(MockitoJUnitRunner::class)
class HumidityAndTemperatureValueProviderTest {

  @InjectMocks
  private lateinit var valueProvider: HumidityAndTemperatureValueProvider

  private val emptyArray = emptyList<Int>()

  private val defaultArray = listOf(34, 90, 0, 0, 36, -71, 0, 0)

  @Test
  fun `should handle value`() {
    // given
    val channel: ChannelWithChildren = mockk {
      every { function } returns SuplaFunction.HUMIDITY_AND_TEMPERATURE
    }

    // when
    val result = valueProvider.handle(channel)

    // then
    assertThat(result).isTrue()
  }

  @Test
  fun `should get unknown temperature for empty byte array`() {
    // given
    val channelData = mockChannelData(emptyArray)

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    assertThat(value).isEqualTo(ThermometerValueProvider.UNKNOWN_VALUE)
  }

  @Test
  fun `should get unknown humidity for empty byte array`() {
    // given
    val channelData = mockChannelData(emptyArray)

    // when
    val value = valueProvider.value(channelData, ValueType.SECOND)

    // then
    assertThat(value).isEqualTo(HumidityAndTemperatureValueProvider.UNKNOWN_HUMIDITY_VALUE)
  }

  @Test
  fun `should get temperature`() {
    // given
    val channelData = mockChannelData(defaultArray)

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    assertThat(value).isEqualTo(23.074)
  }

  @Test
  fun `should unknown humidity`() {
    // given
    val channelData = mockChannelData(defaultArray)

    // when
    val value = valueProvider.value(channelData, ValueType.SECOND)

    // then
    assertThat(value).isEqualTo(47.396)
  }

  @Test
  fun `should get unknown temperature when data is to short`() {
    // given
    val channelData = mockChannelData(defaultArray.subList(0, 2))

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    assertThat(value).isEqualTo(ThermometerValueProvider.UNKNOWN_VALUE)
  }

  @Test
  fun `should get unknown humidity when data is to short`() {
    // given
    val channelData = mockChannelData(defaultArray.subList(0, 6))

    // when
    val value = valueProvider.value(channelData, ValueType.SECOND)

    // then
    assertThat(value).isEqualTo(HumidityAndTemperatureValueProvider.UNKNOWN_HUMIDITY_VALUE)
  }

  private fun mockChannelData(bytes: List<Int>): ChannelWithChildren = mockk {
    every { channel } returns mockk {
      every { channelValueEntity } returns mockk {
        every { getValueAsByteArray() } returns ByteArray(bytes.size) { position -> bytes[position].toByte() }
      }
    }
  }
}
