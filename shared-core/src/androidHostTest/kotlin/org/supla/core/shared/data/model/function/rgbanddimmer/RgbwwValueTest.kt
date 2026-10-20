package org.supla.core.shared.data.model.function.rgbanddimmer
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
