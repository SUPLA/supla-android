package org.supla.android.usecases.channel
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

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.lib.SuplaConst

@RunWith(MockitoJUnitRunner::class)
class GetChannelCaptionUseCaseTest {

  @Mock
  private lateinit var getChannelDefaultCaptionUseCase: GetChannelDefaultCaptionUseCase

  @InjectMocks
  private lateinit var useCase: GetChannelCaptionUseCase

  @Test
  fun `should get default caption provider when caption empty`() {
    // given
    val function = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    val channelEntity: ChannelEntity = mockk {
      every { this@mockk.function } returns function
      every { caption } returns ""
    }
    whenever(getChannelDefaultCaptionUseCase.invoke(function)).thenReturn { _ -> "" }

    // when
    useCase.invoke(channelEntity)

    // then
    verify(getChannelDefaultCaptionUseCase).invoke(function)
  }

  @Test
  fun `should get caption provider when caption not empty`() {
    // given
    val function = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    val channelEntity: ChannelEntity = mockk {
      every { this@mockk.function } returns function
      every { caption } returns "Test"
    }

    // when
    val result = useCase.invoke(channelEntity)

    // then
    val context: Context = mockk()
    assertThat(result(context)).isEqualTo("Test")
    verifyNoInteractions(getChannelDefaultCaptionUseCase)
  }

  @Test
  fun `should get default caption when caption empty`() {
    // given
    val function = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    val channelEntity: ChannelEntity = mockk {
      every { this@mockk.function } returns function
      every { caption } returns ""
    }
    val context: Context = mockk()
    whenever(getChannelDefaultCaptionUseCase.invoke(function)).thenReturn { _ -> "" }

    // when
    useCase.invoke(channelEntity, context)

    // then
    verify(getChannelDefaultCaptionUseCase).invoke(function)
  }

  @Test
  fun `should get caption when caption not empty`() {
    // given
    val function = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    val channelEntity: ChannelEntity = mockk {
      every { this@mockk.function } returns function
      every { caption } returns "Test"
    }
    val context: Context = mockk()

    // when
    val result = useCase.invoke(channelEntity, context)

    // then
    assertThat(result).isEqualTo("Test")
    verifyNoInteractions(getChannelDefaultCaptionUseCase)
  }
}
