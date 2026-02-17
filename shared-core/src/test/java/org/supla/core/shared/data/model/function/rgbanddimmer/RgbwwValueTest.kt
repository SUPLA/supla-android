package org.supla.core.shared.data.model.function.rgbanddimmer

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus

class RgbwwValueTest {

  @Test
  fun `should create red color`() {
    // given
    val value = RgbwwValue(SuplaChannelAvailabilityStatus.ONLINE, true, 100, 100, 255, 0, 0, 0)

    // when
    val color = value.rgb

    // then
    assertThat(color).isEqualTo(0xFF0000)
  }

  @Test
  fun `should create green color`() {
    // given
    val value = RgbwwValue(SuplaChannelAvailabilityStatus.ONLINE, true, 100, 100, 0, 255, 0, 0)

    // when
    val color = value.rgb

    // then
    assertThat(color).isEqualTo(0x00FF00)
  }

  @Test
  fun `should create blue color`() {
    // given
    val value = RgbwwValue(SuplaChannelAvailabilityStatus.ONLINE, true, 100, 100, 0, 0, 255, 0)

    // when
    val color = value.rgb

    // then
    assertThat(color).isEqualTo(0x0000FF)
  }
}
