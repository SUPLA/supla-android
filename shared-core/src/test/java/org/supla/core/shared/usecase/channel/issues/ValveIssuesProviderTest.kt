package org.supla.core.shared.usecase.channel.issues
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
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.channel.ChannelChild
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.valve.SuplaValveFlag
import org.supla.core.shared.infrastructure.LocalizedStringId

class ValveIssuesProviderTest {

  @InjectMockKs
  private lateinit var provider: ValveIssuesProvider

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should not provide issues when channel is offline`() {
    // given
    val channelWithChildren = ChannelWithChildren(
      channel = mockk {
        every { function } returns SuplaFunction.VALVE_OPEN_CLOSE
        every { status } returns SuplaChannelAvailabilityStatus.OFFLINE
      },
      children = emptyList()
    )

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).isEmpty()
  }

  @Test
  fun `should not provide issues when channel has no children`() {
    // given
    val channelWithChildren = ChannelWithChildren(
      channel = mockk {
        every { function } returns SuplaFunction.VALVE_OPEN_CLOSE
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { value } returns byteArrayOf(0, 0)
      },
      children = emptyList()
    )

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).isEmpty()
  }

  @Test
  fun `should not provide issues when there is no child with active sensor`() {
    // given
    val child: ChannelChild = mockk {
      every { relation } returns mockk {
        every { relationType } returns ChannelRelationType.DEFAULT
      }
      every { channel } returns mockk {
        every { value } returns byteArrayOf(0)
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      }
    }
    val channelWithChildren = ChannelWithChildren(
      channel = mockk {
        every { function } returns SuplaFunction.VALVE_OPEN_CLOSE
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { value } returns byteArrayOf(0, 0)
      },
      children = listOf(child)
    )

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).isEmpty()
  }

  @Test
  fun `should provide issues when there is child with active sensor`() {
    // given
    val child: ChannelChild = mockk {
      every { relation } returns mockk {
        every { relationType } returns ChannelRelationType.DEFAULT
      }
      every { channel } returns mockk {
        every { value } returns byteArrayOf(1)
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      }
    }
    val channelWithChildren = ChannelWithChildren(
      channel = mockk {
        every { function } returns SuplaFunction.VALVE_OPEN_CLOSE
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { value } returns byteArrayOf(0, 0)
      },
      children = listOf(child)
    )

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(ChannelIssueItem.Error(LocalizedStringId.FLOOD_SENSOR_ACTIVE))
  }

  @Test
  fun `should provide four types of issues`() {
    // given
    val child: ChannelChild = mockk {
      every { relation } returns mockk {
        every { relationType } returns ChannelRelationType.DEFAULT
      }
      every { channel } returns mockk {
        every { value } returns byteArrayOf(1)
        every { status } returns SuplaChannelAvailabilityStatus.OFFLINE
      }
    }
    val channelWithChildren = ChannelWithChildren(
      channel = mockk {
        every { function } returns SuplaFunction.VALVE_OPEN_CLOSE
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { value } returns byteArrayOf(0, (SuplaValveFlag.FLOODING.value or SuplaValveFlag.MANUALLY_CLOSED.value).toByte())
      },
      children = listOf(child)
    )

    // when
    val issues = provider.provide(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Error(LocalizedStringId.FLOOD_SENSOR_ACTIVE),
      ChannelIssueItem.Error(LocalizedStringId.VALVE_SENSOR_OFFLINE),
      ChannelIssueItem.Error(LocalizedStringId.VALVE_FLOODING),
      ChannelIssueItem.Error(LocalizedStringId.VALVE_MANUALLY_CLOSED)
    )
  }
}
