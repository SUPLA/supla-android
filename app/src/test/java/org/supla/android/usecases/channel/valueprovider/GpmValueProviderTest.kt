package org.supla.android.usecases.channel.valueprovider

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

@RunWith(MockitoJUnitRunner::class)
class GpmValueProviderTest : BaseDoubleValueProviderTest<GpmValueProvider>() {

  override val unknownValue: Double = GpmValueProvider.UNKNOWN_VALUE

  @InjectMocks
  override lateinit var valueProvider: GpmValueProvider

  @Test
  fun `check if handles meter`() {
    // given
    val function = SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

    // when
    val result = valueProvider.handle(function)

    // then
    assertThat(result).isTrue()
  }

  @Test
  fun `check if handles measurement`() {
    // given
    val function = SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

    // when
    val result = valueProvider.handle(function)

    // then
    assertThat(result).isTrue()
  }
}
