package org.supla.android.usecases.channel.valueprovider

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Test
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.usecases.channel.ValueType

abstract class BaseDoubleValueProviderTest<T : DefaultDoubleValueProvider> {

  abstract val unknownValue: Double

  abstract var valueProvider: T

  @Test
  fun `should get unknown value when no data`() {
    // given
    val channelData: ChannelDataEntity = mockk {
      every { channelValueEntity } returns mockk {
        every { getValueAsByteArray() } returns ByteArray(0)
      }
    }

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    Assertions.assertThat(value).isEqualTo(unknownValue)
  }

  @Test
  fun `should get value`() {
    // given
    val valueBytes = listOf(
      (-31).toByte(),
      (122).toByte(),
      (20).toByte(),
      (-82).toByte(),
      (71).toByte(),
      (-31).toByte(),
      (-54).toByte(),
      (63).toByte()
    )
    val channelData: ChannelDataEntity = mockk {
      every { channelValueEntity } returns mockk {
        every { getValueAsByteArray() } returns ByteArray(8) { position -> valueBytes[position] }
      }
    }

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    Assertions.assertThat(value).isEqualTo(0.21)
  }

  @Test
  fun `should get unknown value if there are wrong data`() {
    // given
    val valueBytes = listOf(
      (-31).toByte(),
      (122).toByte(),
      (20).toByte(),
      (-82).toByte(),
      (71).toByte(),
      (-31).toByte(),
      (-54).toByte(),
    )
    val channelData: ChannelDataEntity = mockk {
      every { channelValueEntity } returns mockk {
        every { getValueAsByteArray() } returns ByteArray(7) { position -> valueBytes[position] }
      }
    }

    // when
    val value = valueProvider.value(channelData, ValueType.FIRST)

    // then
    Assertions.assertThat(value).isEqualTo(unknownValue)
  }
}
