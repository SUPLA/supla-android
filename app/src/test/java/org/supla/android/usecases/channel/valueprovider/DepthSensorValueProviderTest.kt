package org.supla.android.usecases.channel.valueprovider

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR

@RunWith(MockitoJUnitRunner::class)
class DepthSensorValueProviderTest : BaseDoubleValueProviderTest<DepthSensorValueProvider>() {

  override val unknownValue: Double = DepthSensorValueProvider.UNKNOWN_VALUE

  @InjectMocks
  override lateinit var valueProvider: DepthSensorValueProvider

  @Test
  fun `check if handles`() {
    // given
    val function = SUPLA_CHANNELFNC_DEPTHSENSOR

    // when
    val result = valueProvider.handle(function)

    // then
    assertThat(result).isTrue()
  }
}
