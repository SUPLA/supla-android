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
class DepthSensorValueProviderTest : BaseDoubleValueProviderTest<DepthSensorValueProvider>() {

  override val unknownValue: Double = DepthSensorValueProvider.UNKNOWN_VALUE

  @InjectMocks
  override lateinit var valueProvider: DepthSensorValueProvider

  @Test
  fun `check if handles`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { function } returns SuplaChannelFunction.DEPTH_SENSOR
    }

    // when
    val result = valueProvider.handle(channel)

    // then
    assertThat(result).isTrue()
  }
}
