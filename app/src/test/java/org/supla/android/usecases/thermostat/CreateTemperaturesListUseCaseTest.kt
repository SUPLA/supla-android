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

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.model.general.IconType
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.channel.ValueType
import org.supla.android.usecases.icon.GetChannelIconUseCase

@RunWith(MockitoJUnitRunner::class)
class CreateTemperaturesListUseCaseTest {

  @Mock
  private lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Mock
  private lateinit var getChannelValueStringUseCase: GetChannelValueStringUseCase

  @InjectMocks
  lateinit var useCase: CreateTemperaturesListUseCase

  @Test
  fun `should create list of temperatures`() {
    // given
    val child1 = createChild(ChannelRelationType.MAIN_THERMOMETER, 111, "11.0", SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE, "12.0")
    val child2 = createChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 222, "22.0")

    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.children } returns listOf(child1, child2)

    // when
    val temperatures = useCase(channelWithChildren)

    // then
    assertThat(temperatures).hasSize(3)
    assertThat(temperatures)
      .extracting({ it.remoteId }, { it.value })
      .containsExactly(tuple(111, "11.0"), tuple(111, "12.0"), tuple(222, "22.0"))
  }

  @Test
  fun `should make list with one entry even if there is no thermometer`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.children } returns emptyList()

    // when
    val temperatures = useCase(channelWithChildren)

    // then
    assertThat(temperatures).hasSize(1)
    assertThat(temperatures)
      .extracting({ it.remoteId }, { it.value })
      .containsExactly(tuple(-1, "---"))
  }

  @Test
  fun `should add main thermometer even if there is no main thermometer`() {
    // given
    val child2 = createChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 222, "22.0")

    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.children } returns listOf(child2)

    // when
    val temperatures = useCase(channelWithChildren)

    // then
    assertThat(temperatures).hasSize(2)
    assertThat(temperatures)
      .extracting({ it.remoteId }, { it.value })
      .containsExactly(tuple(-1, "---"), tuple(222, "22.0"))
  }

  private fun createChild(
    relationType: ChannelRelationType,
    remoteId: Int,
    text: String,
    function: Int = SUPLA_CHANNELFNC_THERMOMETER,
    secondValue: String? = null
  ): ChannelChildEntity {
    val relationEntity = mockk<ChannelRelationEntity> {
      every { this@mockk.relationType } returns relationType
    }
    val channel = mockk<ChannelDataEntity>()
    every { channel.remoteId } returns remoteId
    every { channel.function } returns function

    whenever(getChannelValueStringUseCase(channel, withUnit = false)).thenReturn(text)
    secondValue?.let { whenever(getChannelValueStringUseCase(channel, ValueType.SECOND, withUnit = false)).thenReturn(secondValue) }
    whenever(getChannelIconUseCase.getIconProvider(channel)).thenReturn { null }
    secondValue?.let { whenever(getChannelIconUseCase.getIconProvider(channel, IconType.SECOND)).thenReturn { null } }

    return ChannelChildEntity(relationEntity, channel)
  }
}
