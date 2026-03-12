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
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.GetCaptionUseCase

class GetChannelIssuesForListUseCaseTest {
  @MockK
  private lateinit var getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase

  @MockK
  private lateinit var getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase

  @MockK
  private lateinit var getChannelSpecificIssuesUseCase: GetChannelSpecificIssuesUseCase

  @MockK
  private lateinit var getCaptionUseCase: GetCaptionUseCase

  @InjectMockKs
  private lateinit var useCase: GetChannelIssuesForListUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get battery icon if there are no issues`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { function } returns SuplaFunction.POWER_SWITCH
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
      }
      every { allChildrenFlat } returns emptyList()
    }

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns IssueIcon.Battery
    every { getChannelSpecificIssuesUseCase.invoke(channelWithChildren) } returns emptyList()

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Battery)
    assertThat(issues.issuesStrings).isEmpty()
  }

  @Test
  fun `should get warning icon and battery icon if there is issue and battery powering`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockChannelWithChildren()

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns IssueIcon.Battery
    every { getChannelSpecificIssuesUseCase.invoke(channelWithChildren) } returns
      listOf(ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error, IssueIcon.Battery)
    assertThat(issues.issuesStrings).containsExactly(LocalizedString.WithId(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))
  }

  @Test
  fun `should get only warning icon if there is no battery`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockChannelWithChildren()

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns null
    every { getChannelSpecificIssuesUseCase.invoke(channelWithChildren) } returns
      listOf(ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error)
    assertThat(issues.issuesStrings).containsExactly(LocalizedString.WithId(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))
  }

  @Test
  fun `should get warning icon if there is low battery and thermostat issue`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockChannelWithChildren()

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns ChannelIssueItem.LowBattery(emptyList())
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns null
    every { getChannelSpecificIssuesUseCase.invoke(channelWithChildren) } returns
      listOf(ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_CLOCK_ERROR))

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error)
    assertThat(issues.issuesStrings).containsExactly(LocalizedString.WithId(LocalizedStringId.THERMOSTAT_CLOCK_ERROR))
  }

  @Test
  fun `should get only sound icon if only sound flag active`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockChannelWithChildren()

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns null
    every { getChannelSpecificIssuesUseCase.invoke(channelWithChildren) } returns listOf(ChannelIssueItem.SoundAlarm())

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Sound)
    assertThat(issues.issuesStrings).isEmpty()
  }

  @Test
  fun `should get update icon if channel is in update status`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockChannelWithChildren(SuplaChannelAvailabilityStatus.FIRMWARE_UPDATE_ONGOING)

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Update)
    assertThat(issues.issuesStrings).containsExactly(localizedString(LocalizedStringId.CHANNEL_STATUS_UPDATING))
  }

  @Test
  fun `should notify with error and message when channel is not available`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockChannelWithChildren(SuplaChannelAvailabilityStatus.ONLINE_BUT_NOT_AVAILABLE)

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error)
    assertThat(issues.issuesStrings).containsExactly(localizedString(LocalizedStringId.CHANNEL_STATUS_NOT_AVAILABLE))
  }

  @Test
  fun `should get channel issues without extension if there are no children issues`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockChannelWithChildren()
    val channelIssue: ChannelIssueItem = ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_CALIBRATION_ERROR)

    every { getChannelLowBatteryIssueUseCase(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase(channelWithChildren) } returns null
    every { getChannelSpecificIssuesUseCase(channelWithChildren) } returns listOf(channelIssue)

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error)
    assertThat(issues.issuesStrings).containsExactly(localizedString(LocalizedStringId.THERMOSTAT_CALIBRATION_ERROR))
  }

  @Test
  fun `should get channel issues with extension if there are children issues`() {
    // given
    val childCaption = LocalizedString.Constant("caption child")
    val childChannel: Channel = mockk {
      every { remoteId } returns 2
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }
    val child: ChannelWithChildren = mockk {
      every { channel } returns childChannel
      every { allChildrenFlat } returns emptyList()
    }

    val mainCaption = LocalizedString.Constant("caption main")
    val mainChannel: Channel = mockk {
      every { remoteId } returns 1
      every { status } returns SuplaChannelAvailabilityStatus.ONLINE
    }
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mainChannel
      every { allChildrenFlat } returns listOf(child)
    }
    val channelIssue: ChannelIssueItem = ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_CALIBRATION_ERROR)

    every { getChannelLowBatteryIssueUseCase(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase(channelWithChildren) } returns null
    every { getChannelSpecificIssuesUseCase(channelWithChildren) } returns listOf(channelIssue)
    every { getChannelSpecificIssuesUseCase(child) } returns listOf(channelIssue)
    every { getCaptionUseCase(mainChannel) } returns mainCaption
    every { getCaptionUseCase(childChannel) } returns childCaption

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error)
    assertThat(issues.issuesStrings).containsExactly(
      localizedString("%s (%d - %s)", localizedString(LocalizedStringId.THERMOSTAT_CALIBRATION_ERROR), 1, mainCaption),
      localizedString("%s (%d - %s)", localizedString(LocalizedStringId.THERMOSTAT_CALIBRATION_ERROR), 2, childCaption)
    )
  }

  private fun mockChannelWithChildren(status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.ONLINE): ChannelWithChildren {
    val channel: Channel = mockk {
      every { this@mockk.status } returns status
    }
    return mockk {
      every { this@mockk.channel } returns channel
      every { this@mockk.allChildrenFlat } returns emptyList()
    }
  }
}
