package org.supla.android.usecases.list.eventmappers
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
import org.mockito.kotlin.whenever
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.db.Channel
import org.supla.android.images.ImageId
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase

@RunWith(MockitoJUnitRunner::class)
class ChannelWithChildrenToMeasurementUpdateEventMapperTest {

  @Mock
  private lateinit var getChannelCaptionUseCase: GetChannelCaptionUseCase

  @Mock
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @InjectMocks
  private lateinit var mapper: ChannelWithChildrenToMeasurementUpdateEventMapper

  @Test
  fun `should handle channel with children`() {
    // given
    val channel = mockk<ChannelDataEntity>()
    every { channel.channelEntity } returns mockk { every { function } returns SUPLA_CHANNELFNC_DEPTHSENSOR }

    val channelWithChildren = ChannelWithChildren(channel, emptyList())

    // when
    val result = mapper.handle(channelWithChildren)

    // then
    assertThat(result).isTrue
  }

  @Test
  fun `should not handle channel`() {
    // given
    val channel = mockk<Channel>()

    // when
    val result = mapper.handle(channel)

    // then
    assertThat(result).isFalse
  }

  @Test
  fun `should map channel data to item`() {
    // given
    val online = true
    val caption = "caption"
    val value = "value"

    val channel = mockk<ChannelDataEntity>()
    every { channel.function } returns SUPLA_CHANNELFNC_DEPTHSENSOR
    every { channel.channelEntity } returns mockk { every { function } returns SUPLA_CHANNELFNC_DEPTHSENSOR }
    every { channel.channelValueEntity } returns mockk { every { this@mockk.online } returns online }
    every { channel.flags } returns 0

    val imageId = ImageId(123)
    val channelWithChildren = ChannelWithChildren(channel, emptyList())
    whenever(getChannelValueStringUseCase.invoke(channel)).thenReturn(value)
    whenever(getChannelCaptionUseCase.invoke(channel)).thenReturn { caption }
    whenever(getChannelIconUseCase.invoke(channel)).thenReturn(imageId)

    val context: Context = mockk()

    // when
    val result = mapper.map(channelWithChildren)

    // then
    val defaultItem = result as SlideableListItemData.Default

    assertThat(defaultItem.online).isTrue
    assertThat(defaultItem.titleProvider(context)).isEqualTo(caption)
    assertThat(defaultItem.icon).isEqualTo(imageId)
    assertThat(defaultItem.value).isEqualTo(value)
    assertThat(defaultItem.infoSupported).isEqualTo(false)
  }
}
