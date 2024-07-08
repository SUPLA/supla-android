package org.supla.android.usecases.channel.valueprovider

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFunction

@RunWith(MockitoJUnitRunner::class)
class GpmValueProviderTest : BaseDoubleValueProviderTest<GpmValueProvider>() {

  override val unknownValue: Double = GpmValueProvider.UNKNOWN_VALUE

  @InjectMocks
  override lateinit var valueProvider: GpmValueProvider

  @Test
  fun `check if handles meter`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { function } returns SuplaChannelFunction.GENERAL_PURPOSE_METER
    }

    // when
    val result = valueProvider.handle(channel)

    // then
    assertThat(result).isTrue()
  }

  @Test
  fun `check if handles measurement`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { function } returns SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT
    }

    // when
    val result = valueProvider.handle(channel)

    // then
    assertThat(result).isTrue()
  }
}
