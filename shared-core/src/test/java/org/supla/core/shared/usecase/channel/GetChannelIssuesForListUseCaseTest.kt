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
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId

class GetChannelIssuesForListUseCaseTest {
  @MockK
  private lateinit var getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase

  @MockK
  private lateinit var getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase

  @MockK
  private lateinit var getChannelSpecificIssuesUseCase: GetChannelSpecificIssuesUseCase

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
      }
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
    val channelWithChildren: ChannelWithChildren = mockk()

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
    val channelWithChildren: ChannelWithChildren = mockk()

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
    val channelWithChildren: ChannelWithChildren = mockk()

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
    val channelWithChildren: ChannelWithChildren = mockk()

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns null
    every { getChannelSpecificIssuesUseCase.invoke(channelWithChildren) } returns listOf(ChannelIssueItem.SoundAlarm())

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Sound)
    assertThat(issues.issuesStrings).isEmpty()
  }
}
