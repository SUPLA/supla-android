package org.supla.android.features.details.thermostatdetail.general.data
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
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.thermostat.SuplaThermostatFlag
import org.supla.core.shared.data.model.function.thermostat.ThermostatState
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue

@RunWith(MockitoJUnitRunner::class)
class SensorIssueTest {

  @Test
  fun `should not create issue when no flag set`() {
    // given
    val value: ThermostatValue = mockk {
      every { online } returns true
      every { state } returns ThermostatState(0)
      every { mode } returns SuplaHvacMode.OFF
      every { setpointTemperatureHeat } returns 10f
      every { setpointTemperatureCool } returns 10f
      every { flags } returns emptyList()
    }
    val children = emptyList<ChannelChildEntity>()
    val getChannelIconUseCase: GetChannelIconUseCase = mockk()

    // when
    val sensorIssue = SensorIssue.build(value, children, getChannelIconUseCase)

    // then
    assertThat(sensorIssue).isNull()
  }

  @Test
  fun `should create issue`() {
    // given
    val value: ThermostatValue = mockk {
      every { online } returns true
      every { state } returns ThermostatState(0)
      every { mode } returns SuplaHvacMode.OFF
      every { setpointTemperatureHeat } returns 10f
      every { setpointTemperatureCool } returns 10f
      every { flags } returns listOf(SuplaThermostatFlag.FORCED_OFF_BY_SENSOR)
    }
    val children = listOf<ChannelChildEntity>()
    val getChannelIconUseCase: GetChannelIconUseCase = mockk()

    // when
    val sensorIssue = SensorIssue.build(value, children, getChannelIconUseCase)

    // then
    assertThat(sensorIssue).isNotNull
  }
}
