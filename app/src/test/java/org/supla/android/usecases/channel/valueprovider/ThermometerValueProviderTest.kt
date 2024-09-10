package org.supla.android.usecases.channel.valueprovider

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

@RunWith(MockitoJUnitRunner::class)
class ThermometerValueProviderTest : BaseDoubleValueProviderTest<ThermometerValueProvider>() {

  override val unknownValue: Double = ThermometerValueProvider.UNKNOWN_VALUE

  @InjectMocks
  override lateinit var valueProvider: ThermometerValueProvider

  @Test
  fun `check if handles`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { function } returns SUPLA_CHANNELFNC_THERMOMETER
    }

    // when
    val result = valueProvider.handle(channel)

    // then
    assertThat(result).isTrue()
  }
}
