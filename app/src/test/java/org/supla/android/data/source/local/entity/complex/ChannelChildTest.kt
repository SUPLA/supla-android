package org.supla.android.data.source.local.entity.complex
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
import org.junit.Test
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.ThermostatIndicatorIcon
import org.supla.android.ui.lists.ListOnlineState
import org.supla.core.shared.data.model.channel.ChannelRelationType
import org.supla.core.shared.data.model.thermostat.SuplaThermostatFlag

class ChannelChildTest {

  @Test
  fun `check if the indicator icon of children is calculated properly - standby and heating`() {
    // given
    val firstStandby = mockChild(SuplaHvacMode.HEAT)
    val secondStandby = mockChild(SuplaHvacMode.HEAT)
    val thirdHeating = mockChild(SuplaHvacMode.HEAT, SuplaThermostatFlag.HEATING)

    // when
    val indicatorIcon = listOf(firstStandby, secondStandby, thirdHeating).indicatorIcon

    // then
    assertThat(indicatorIcon).isEqualTo(ThermostatIndicatorIcon.HEATING)
  }

  @Test
  fun `check if the indicator icon of children is calculated properly - standby, off by sensor, heating`() {
    // given
    val firstStandby = mockChild(SuplaHvacMode.HEAT)
    val secondOffBySensor = mockChild(SuplaHvacMode.HEAT, SuplaThermostatFlag.FORCED_OFF_BY_SENSOR)
    val thirdHeating = mockChild(SuplaHvacMode.HEAT, SuplaThermostatFlag.HEATING)

    // when
    val indicatorIcon = listOf(firstStandby, secondOffBySensor, thirdHeating).indicatorIcon

    // then
    assertThat(indicatorIcon).isEqualTo(ThermostatIndicatorIcon.HEATING)
  }

  @Test
  fun `check if the indicator icon of children is calculated properly - standby, off by sensor, off`() {
    // given
    val firstStandby = mockChild(SuplaHvacMode.HEAT)
    val secondOffBySensor = mockChild(SuplaHvacMode.HEAT, SuplaThermostatFlag.FORCED_OFF_BY_SENSOR)
    val thirdOff = mockChild(SuplaHvacMode.OFF)

    // when
    val indicatorIcon = listOf(firstStandby, secondOffBySensor, thirdOff).indicatorIcon

    // then
    assertThat(indicatorIcon).isEqualTo(ThermostatIndicatorIcon.FORCED_OFF_BY_SENSOR)
  }

  @Test
  fun `check if online state is calculated properly - all online`() {
    // given
    val first = mockChild(true)
    val second = mockChild(true)
    val third = mockChild(true)

    // when
    val state = listOf(first, second, third).onlineState

    // then
    assertThat(state).isEqualTo(ListOnlineState.ONLINE)
  }

  @Test
  fun `check if online state is calculated properly - all offline`() {
    // given
    val first = mockChild(false)
    val second = mockChild(false)
    val third = mockChild(false)

    // when
    val state = listOf(first, second, third).onlineState

    // then
    assertThat(state).isEqualTo(ListOnlineState.OFFLINE)
  }

  @Test
  fun `check if online state is calculated properly - first online then offline`() {
    // given
    val first = mockChild(true)
    val second = mockChild(false)
    val third = mockChild(false)

    // when
    val state = listOf(first, second, third).onlineState

    // then
    assertThat(state).isEqualTo(ListOnlineState.PARTIALLY_ONLINE)
  }

  @Test
  fun `check if online state is calculated properly - offline, online, offline`() {
    // given
    val first = mockChild(false)
    val second = mockChild(true)
    val third = mockChild(false)

    // when
    val state = listOf(first, second, third).onlineState

    // then
    assertThat(state).isEqualTo(ListOnlineState.PARTIALLY_ONLINE)
  }

  private fun mockChild(hvacMode: SuplaHvacMode, vararg flag: SuplaThermostatFlag): ChannelChildEntity =
    mockk {
      every { relationType } returns ChannelRelationType.MASTER_THERMOSTAT
      every { channelDataEntity } returns mockk {
        every { channelValueEntity } returns mockk {
          every { asThermostatValue() } returns mockk {
            every { online } returns true
            every { flags } returns flag.toList()
            every { mode } returns hvacMode
          }
        }
      }
    }

  private fun mockChild(online: Boolean): ChannelChildEntity {
    val value: ChannelValueEntity = mockk {
      every { this@mockk.online } returns online
    }

    return mockk {
      every { relationType } returns ChannelRelationType.MASTER_THERMOSTAT
      every { channelDataEntity } returns mockk {
        every { channelValueEntity } returns value
      }
    }
  }
}
