package org.supla.android.data.source.local.entity.complex
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

import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.core.shared.data.model.function.facadeblind.FacadeBlindValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag

class ChannelDataEntityExtensionsTest {

  private val gson = mockk<Gson>()

  @Test
  fun `should omit marker when position is invalid`() {
    val result = listOf(channel(position = -1, tilt = 30)).toShadingBlindMarkers(gson)

    assertThat(result).isEmpty()
  }

  @Test
  fun `should keep valid position and mark invalid tilt as unknown`() {
    val result = listOf(channel(position = 40, tilt = -1)).toShadingBlindMarkers(gson)

    assertThat(result).containsExactly(ShadingBlindMarker(position = 40f, tiltValue = null))
  }

  @Test
  fun `should mark tilt as unknown when tilt flag is not set`() {
    val result = listOf(
      channel(position = 40, tilt = 30, flags = emptyList())
    ).toShadingBlindMarkers(gson)

    assertThat(result).containsExactly(ShadingBlindMarker(position = 40f, tiltValue = null))
  }

  @Test
  fun `should keep valid tilt when tilt flag is set`() {
    val result = listOf(
      channel(position = 40, tilt = 30, flags = listOf(SuplaShadingSystemFlag.TILT_IS_SET))
    ).toShadingBlindMarkers(gson)

    assertThat(result).containsExactly(ShadingBlindMarker(position = 40f, tiltValue = 30f))
  }

  private fun channel(
    position: Int,
    tilt: Int,
    flags: List<SuplaShadingSystemFlag> = listOf(SuplaShadingSystemFlag.TILT_IS_SET)
  ): ChannelDataEntity {
    val facadeBlindValue = FacadeBlindValue(
      status = SuplaChannelAvailabilityStatus.ONLINE,
      position = position,
      tilt = tilt,
      flags = flags
    )
    val channelValue = mockk<ChannelValueEntity> {
      every { asFacadeBlindValue() } returns facadeBlindValue
    }

    return mockk {
      every { channelValueEntity } returns channelValue
      every { configEntity } returns null
    }
  }
}
