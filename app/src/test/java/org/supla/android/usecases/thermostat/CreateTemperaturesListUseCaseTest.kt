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
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.db.Channel
import org.supla.android.db.ChannelValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.usecases.channel.ChannelChild
import org.supla.android.usecases.channel.ChannelWithChildren

@RunWith(MockitoJUnitRunner::class)
class CreateTemperaturesListUseCaseTest {

  @InjectMocks
  lateinit var useCase: CreateTemperaturesListUseCase

  @Test
  fun `should create list of temperatures`() {
    // given
    val child1 = createChild(ChannelRelationType.MAIN_THERMOMETER, 111, "11.0", SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE)
    val child2 = createChild(ChannelRelationType.AUX_THERMOMETER_FLOOR, 222, "22.0")
    val channelWithChildren: ChannelWithChildren = mockk()
    every { channelWithChildren.children } returns listOf(child1, child2)

    every { child1.channel.value } returns mockk<ChannelValue>().also {
      every { it.onLine } returns false
    }
    every { child2.channel.value } returns mockk<ChannelValue>().also {
      every { it.onLine } returns false
    }

    // when
    val temperatures = useCase(channelWithChildren)

    // then
    assertThat(temperatures).hasSize(3)
    assertThat(temperatures)
      .extracting({ it.remoteId }, { it.valueStringProvider.invoke(mockk()) })
      .containsExactly(tuple(111, "---"), tuple(111, "---"), tuple(222, "---"))
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
      .extracting({ it.remoteId }, { it.valueStringProvider.invoke(mockk()) })
      .containsExactly(tuple(-1, "---"))
  }

  private fun createChild(
    relationType: ChannelRelationType,
    remoteId: Int,
    text: String,
    function: Int = SUPLA_CHANNELFNC_THERMOMETER
  ): ChannelChild {
    val channel = mockk<Channel>()
    every { channel.remoteId } returns remoteId
    every { channel.imageIdx } returns mockk()
    every { channel.humanReadableValue } returns text
    every { channel.func } returns function

    return ChannelChild(relationType, channel)
  }
}
