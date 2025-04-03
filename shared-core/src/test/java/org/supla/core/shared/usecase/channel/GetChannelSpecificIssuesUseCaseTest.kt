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
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.channel.ChannelWithChildren
import org.supla.core.shared.data.model.channel.thermostatValue
import org.supla.core.shared.data.model.channel.valveValue
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.data.model.valve.SuplaValveFlag
import org.supla.core.shared.data.model.valve.ValveValue
import org.supla.core.shared.infrastructure.LocalizedStringId

class GetChannelSpecificIssuesUseCaseTest {

  @InjectMockKs
  private lateinit var useCase: GetChannelSpecificIssuesUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get thermostat thermometer error`() {
    // given
    mockkStatic(Channel::thermostatValue)
    val thermometerError = SuplaThermostatFlag.THERMOMETER_ERROR.value.toByte()
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { thermostatValue } returns
          ThermostatValue.from(SuplaChannelAvailabilityStatus.ONLINE, byteArrayOf(0, 2, 120, 0, 80, 0, thermometerError, 0))
      }
    }

    // when
    val issues = useCase.invoke(channelWithChildren)

    // then
    assertThat(issues).containsExactly(ChannelIssueItem.Error(LocalizedStringId.THERMOSTAT_THERMOMETER_ERROR))
  }

  @Test
  fun `should get thermostat clock error`() {
    // given
    mockkStatic(Channel::thermostatValue)
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { function } returns SuplaFunction.HVAC_THERMOSTAT
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { thermostatValue } returns
          ThermostatValue.from(SuplaChannelAvailabilityStatus.ONLINE, byteArrayOf(0, 2, 120, 0, 80, 0, 0, 1))
      }
    }

    // when
    val issues = useCase.invoke(channelWithChildren)

    // then
    assertThat(issues).containsExactly(ChannelIssueItem.Warning(LocalizedStringId.THERMOSTAT_CLOCK_ERROR))
  }

  @Test
  fun `should get valve issues`() {
    // given
    mockkStatic(Channel::valveValue)
    val channelWithChildren: ChannelWithChildren = mockk {
      every { channel } returns mockk {
        every { function } returns SuplaFunction.VALVE_OPEN_CLOSE
        every { status } returns SuplaChannelAvailabilityStatus.ONLINE
        every { valveValue } returns ValveValue(
          status = SuplaChannelAvailabilityStatus.ONLINE,
          closed = 0,
          flags = listOf(SuplaValveFlag.FLOODING, SuplaValveFlag.MANUALLY_CLOSED)
        )
      }
      every { children } returns listOf(
        mockk {
          every { relation } returns mockk {
            every { relationType } returns ChannelRelationType.DEFAULT
          }
          every { channel } returns mockk {
            every { value } returns byteArrayOf(1)
          }
        }
      )
    }

    // when
    val issues = useCase.invoke(channelWithChildren)

    // then
    assertThat(issues).containsExactly(
      ChannelIssueItem.Error(LocalizedStringId.FLOOD_SENSOR_ACTIVE),
      ChannelIssueItem.Error(LocalizedStringId.VALVE_FLOODING),
      ChannelIssueItem.Error(LocalizedStringId.VALVE_MANUALLY_CLOSED)
    )
  }
}
