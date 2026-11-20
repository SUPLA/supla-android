package org.supla.android.usecases.group
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

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.InjectMockKs
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType

class CalculateGroupTiltingDetailsUseCaseTest {

  @InjectMockKs
  private lateinit var useCase: CalculateGroupTiltingDetailsUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get unknown when group is empty`() {
    // when
    val result = useCase.invoke(emptyList())

    // then
    Assertions.assertThat(result).isEqualTo(TiltingDetails.Unknown)
  }

  @Test
  fun `should get unknown when config not supported`() {
    // when
    val result = useCase.invoke(listOf(SuplaChannelConfig(1, 1, 1)))

    // then
    Assertions.assertThat(result).isEqualTo(TiltingDetails.Unknown)
  }

  @Test
  fun `should get unknown when one config is missing`() {
    // given
    val config = mockConfig(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)

    // when
    val result = useCase.invoke(listOf(config, null, config))

    // then
    Assertions.assertThat(result).isEqualTo(TiltingDetails.Unknown)
  }

  @Test
  fun `should get similar when both configs are similar`() {
    // given
    val config = mockConfig(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)

    // when
    val result = useCase.invoke(listOf(config, config))

    // then
    Assertions.assertThat(result).isEqualTo(TiltingDetails.Similar(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING))
  }

  @Test
  fun `should get different when configs are different`() {
    // given
    val config1 = mockConfig(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)
    val config2 = mockConfig(0, 180, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)

    // when
    val result = useCase.invoke(listOf(config1, config2))

    // then
    Assertions.assertThat(result).isEqualTo(TiltingDetails.Different)
  }

  @Test
  fun `should get different when first configs are different and then comes third one - similar`() {
    // given
    val config1 = mockConfig(0, 90, SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING)
    val config2 = mockConfig(1, 180, SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED)

    // when
    val result = useCase.invoke(listOf(config1, config2, config2))

    // then
    Assertions.assertThat(result).isEqualTo(TiltingDetails.Different)
  }

  private fun mockConfig(tilt0: Int, tilt100: Int, type: SuplaTiltControlType) =
    SuplaChannelFacadeBlindConfig(
      remoteId = 0,
      func = 0,
      crc32 = 0L,
      closingTimeMs = 0,
      openingTimeMs = 0,
      tiltingTimeMs = 0,
      motorUpsideDown = false,
      buttonsUpsideDown = false,
      timeMargin = 0,
      tilt0Angle = tilt0,
      tilt100Angle = tilt100,
      type = type
    )
}
