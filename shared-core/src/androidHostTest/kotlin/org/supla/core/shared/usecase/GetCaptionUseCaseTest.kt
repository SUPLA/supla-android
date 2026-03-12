package org.supla.core.shared.usecase
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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.GetChannelDefaultCaptionUseCase

class GetCaptionUseCaseTest {

  @MockK
  private lateinit var getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase

  @InjectMockKs
  private lateinit var useCase: GetCaptionUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get default caption provider when caption empty`() {
    // given
    val function = SuplaFunction.THERMOMETER
    val channelEntity: Channel = mockk {
      every { this@mockk.function } returns function
      every { caption } returns ""
    }
    every { getChannelDefaultCaptionUseCase.invoke(function) } returns LocalizedString.Empty

    // when
    useCase.invoke(channelEntity)

    // then
    verify {
      getChannelDefaultCaptionUseCase.invoke(function)
    }
  }

  @Test
  fun `should get caption provider when caption not empty`() {
    // given
    val function = SuplaFunction.THERMOMETER
    val channelEntity: Channel = mockk {
      every { this@mockk.function } returns function
      every { caption } returns "Test"
    }

    // when
    val result = useCase.invoke(channelEntity)

    // then
    assertThat(result).isEqualTo(LocalizedString.Constant("Test"))
    confirmVerified(getChannelDefaultCaptionUseCase)
  }
}
