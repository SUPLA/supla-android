package org.supla.android.usecases.channel.valueprovider

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.usecases.channel.ValueType

@RunWith(MockitoJUnitRunner::class)
class HumidityAndTemperatureValueProviderTest {

  @InjectMocks
  private lateinit var valueProvider: HumidityAndTemperatureValueProvider

  private val emptyArray = emptyList<Int>()

  private val defaultArray = listOf(34, 90, 0, 0, 36, -71, 0, 0)

  @Test
  fun `should handle value`() {
    // given
    val function = SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

    // when
    val result = valueProvider.handle(function)

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

  private fun mockChannelData(bytes: List<Int>): ChannelDataEntity = mockk {
    every { channelValueEntity } returns mockk {
      every { getValueAsByteArray() } returns ByteArray(bytes.size) { position -> bytes[position].toByte() }
    }
  }
}
