package org.supla.android.usecases.thermostat
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
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.R
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.features.details.thermostatdetail.general.MeasurementValue
import org.supla.android.images.ImageId
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.general.SuplaFunction

class CreateTemperaturesListUseCaseTest {

  @MockK
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @MockK
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @InjectMockKs
  lateinit var useCase: CreateTemperaturesListUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create list of temperatures`() {
    // given
    val child1ImageId: ImageId = mockk()
    val child2ImageId: ImageId = mockk()
    val child1 =
      createChild(ChannelRelationType.MAIN_THERMOMETER, 111, "11.0", SuplaFunction.HUMIDITY_AND_TEMPERATURE, "12.0", child1ImageId)
    val child2 = createChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 222, "22.0", imageId = child2ImageId)

    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.children } returns listOf(child1, child2)

    // when
    val temperatures = useCase.invoke(channelWithChildren)

    // then
    assertThat(temperatures).hasSize(3)
    assertThat(temperatures)
      .containsExactly(
        MeasurementValue(111, child1ImageId, "11.0"),
        MeasurementValue(111, child1ImageId, "12.0"),
        MeasurementValue(222, child2ImageId, "22.0")
      )
  }

  @Test
  fun `should make list with one entry even if there is no thermometer`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.children } returns emptyList()

    // when
    val temperatures = useCase.invoke(channelWithChildren)

    // then
    assertThat(temperatures).hasSize(1)
    assertThat(temperatures)
      .containsExactly(MeasurementValue(-1, ImageId(R.drawable.ic_unknown_channel), "---"))
  }

  @Test
  fun `should add main thermometer even if there is no main thermometer`() {
    // given
    val imageId: ImageId = mockk()
    val child2 = createChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 222, "22.0", imageId = imageId)

    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.children } returns listOf(child2)

    // when
    val temperatures = useCase.invoke(channelWithChildren)

    // then
    assertThat(temperatures).hasSize(2)
    assertThat(temperatures)
      .containsExactly(
        MeasurementValue(-1, ImageId(R.drawable.ic_unknown_channel), "---"),
        MeasurementValue(222, imageId, "22.0")
      )
  }

  private fun createChild(
    relationType: ChannelRelationType,
    remoteId: Int,
    text: String,
    function: SuplaFunction = SuplaFunction.THERMOMETER,
    secondValue: String? = null,
    imageId: ImageId = mockk()
  ): ChannelChildEntity {
    val relationEntity = mockk<ChannelRelationEntity> {
      every { this@mockk.relationType } returns relationType
    }
    val channel = mockk<ChannelDataEntity>()
    every { channel.remoteId } returns remoteId
    every { channel.function } returns function

    every { getChannelValueStringUseCase.invoke(channel, withUnit = false) } returns text
    secondValue?.let { every { getChannelValueStringUseCase.invoke(channel, ValueType.SECOND, withUnit = false) } returns secondValue }
    every { getChannelIconUseCase.invoke(channel) } returns imageId
    every { getChannelIconUseCase.invoke(channel, IconType.SECOND) } returns imageId

    return ChannelChildEntity(relationEntity, channel)
  }
}
