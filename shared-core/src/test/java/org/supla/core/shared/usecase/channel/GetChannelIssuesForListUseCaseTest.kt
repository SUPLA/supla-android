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
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.channel.thermostatValue
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.lists.IssueIcon
import org.supla.core.shared.data.model.thermostat.SuplaThermostatFlag
import org.supla.core.shared.data.model.thermostat.ThermostatValue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId

class GetChannelIssuesForListUseCaseTest {
  @MockK
  private lateinit var getChannelLowBatteryIssueUseCase: GetChannelLowBatteryIssueUseCase

  @MockK
  private lateinit var getChannelBatteryIconUseCase: GetChannelBatteryIconUseCase

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

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Battery)
    assertThat(issues.issuesStrings).isEmpty()
  }

  @Test
  fun `should get warning icon and battery icon if there is issue and battery powering`() {
    // given
    mockkStatic(Channel::thermostatValue)
    val thermometerError = SuplaThermostatFlag.THERMOMETER_ERROR.value.toByte()
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
        every { online } returns true
        every { thermostatValue } returns ThermostatValue.from(true, byteArrayOf(0, 2, 120, 0, 80, 0, thermometerError, 0))
      }
    }

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns IssueIcon.Battery

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error, IssueIcon.Battery)
    assertThat(issues.issuesStrings).containsExactly(LocalizedString.WithId(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))
  }

  @Test
  fun `should get only warning icon if there is no battery`() {
    // given
    mockkStatic(Channel::thermostatValue)
    val thermometerError = SuplaThermostatFlag.THERMOMETER_ERROR.value.toByte()
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
        every { online } returns true
        every { thermostatValue } returns ThermostatValue.from(true, byteArrayOf(0, 2, 120, 0, 80, 0, thermometerError, 0))
      }
    }

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns null
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns null

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error)
    assertThat(issues.issuesStrings).containsExactly(LocalizedString.WithId(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))
  }

  @Test
  fun `should get warning icon if there is low battery and thermostat issue`() {
    // given
    mockkStatic(Channel::thermostatValue)
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
        every { online } returns true
        every { thermostatValue } returns ThermostatValue.from(true, byteArrayOf(0, 2, 120, 0, 80, 0, 0, 1))
      }
    }

    every { getChannelLowBatteryIssueUseCase.invoke(channelWithChildren) } returns ChannelIssueItem.LowBattery(emptyList())
    every { getChannelBatteryIconUseCase.invoke(channelWithChildren) } returns null

    // when
    val issues = useCase(channelWithChildren)

    // then
    assertThat(issues.icons).containsExactly(IssueIcon.Error)
    assertThat(issues.issuesStrings).containsExactly(LocalizedString.WithId(LocalizedStringId.THERMOSTAT_CLOCK_ERROR))
  }
}
