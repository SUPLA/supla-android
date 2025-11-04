package org.supla.core.shared.usecase.channel
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
import org.supla.core.shared.data.model.channel.ChannelChild
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.infrastructure.storage.ApplicationPreferences
import org.supla.core.shared.usecase.GetCaptionUseCase

class GetChannelLowBatteryIssueUseCaseTest {

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @MockK
  private lateinit var applicationPreferences: ApplicationPreferences

  @InjectMockKs
  private lateinit var useCase: GetChannelLowBatteryIssueUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get issues for channel and children`() {
    // given
    val mainChannel = mockChannelWithBatteryLevel(1, 4)
    val childChannel = mockChannelWithBatteryLevel(2, 5)
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mainChannel
      every { children } returns listOf(
        mockk {
          every { channel } returns childChannel
          every { children } returns emptyList()
        },
        mockk {
          every { channel } returns mockChannelWithBatteryLevel(2, 50)
          every { children } returns emptyList()
        }
      )
    }

    every { getCaptionUseCase.invoke(mainChannel) } returns LocalizedString.Constant("Main")
    every { getCaptionUseCase.invoke(childChannel) } returns LocalizedString.Constant("Child")
    every { applicationPreferences.batteryWarningLevel } returns 10

    // when
    val issue = useCase(channelWithChildren)

    // then
    assertThat(issue?.icon).isEqualTo(IssueIcon.Battery0)
    assertThat(issue?.messages).isEqualTo(
      listOf(
        localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL_WITH_INFO, 1, LocalizedString.Constant("Main"), 4),
        localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL_WITH_INFO, 2, LocalizedString.Constant("Child"), 5)
      )
    )
    assertThat(issue?.priority).isEqualTo(4)
  }

  @Test
  fun `should return null when there is no low battery issue`() {
    // given
    val mainChannel = mockChannelWithBatteryLevel(1, 30)
    val childChannel = mockChannelWithBatteryLevel(2, 40)
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mainChannel
      every { children } returns listOf(
        mockk {
          every { channel } returns childChannel
          every { children } returns emptyList()
        },
        mockk {
          every { channel } returns mockChannelWithBatteryLevel(2, 50)
          every { children } returns emptyList()
        }
      )
    }

    every { applicationPreferences.batteryWarningLevel } returns 10

    // when
    val issue = useCase(channelWithChildren)

    // then
    assertThat(issue).isNull()
  }

  @Test
  fun `should be able to get issues even if there are cycles in children`() {
    // given
    val child1Channel = mockChannelWithBatteryLevel(2, 5)
    val child1: ChannelChild = mockk(name = "C1")
    every { child1.channel } returns child1Channel

    val child2Channel = mockChannelWithBatteryLevel(3, 4)
    val child2: ChannelChild = mockk(name = "C2")
    every { child2.channel } returns child2Channel

    every { child1.children } returns listOf(child2)
    every { child2.children } returns listOf(child1)

    val channelWithChildrenChannel = mockChannelWithBatteryLevel(1, 20)
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns channelWithChildrenChannel
      every { children } returns listOf(child1)
    }

    every { getCaptionUseCase.invoke(child1Channel) } returns LocalizedString.Constant("Child 1")
    every { getCaptionUseCase.invoke(child2Channel) } returns LocalizedString.Constant("Child 2")
    every { applicationPreferences.batteryWarningLevel } returns 10

    // when
    val issue = useCase(channelWithChildren)

    // then
    assertThat(issue?.icon).isEqualTo(IssueIcon.Battery0)
    assertThat(issue?.messages).isEqualTo(
      listOf(
        localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL_WITH_INFO, 2, LocalizedString.Constant("Child 1"), 5),
        localizedString(LocalizedStringId.CHANNEL_BATTERY_LEVEL_WITH_INFO, 3, LocalizedString.Constant("Child 2"), 4)
      )
    )
    assertThat(issue?.priority).isEqualTo(4)
  }

  private fun mockChannelWithBatteryLevel(id: Int, batteryLevel: Int): Channel =
    mockk {
      every { remoteId } returns id
      every { batteryInfo } returns mockk {
        every { level } returns batteryLevel
      }
    }
}
