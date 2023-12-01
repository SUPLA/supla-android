package org.supla.android.model.thermostat
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

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags

@RunWith(MockitoJUnitRunner::class)
class ThermostatValueTest {

  @Test
  fun `should create thermostat value from byte array`() {
    // given
    val bytes = byteArrayOf(0, 2, 120, 0, 80, 0, 8, 0)

    // when
    val values = ThermostatValue.from(true, bytes)

    // then
    assertThat(values.online).isTrue()
    assertThat(values.state.isOff()).isTrue
    assertThat(values.mode).isEqualTo(SuplaHvacMode.HEAT)
    assertThat(values.setpointTemperatureHeat).isEqualTo(1.2f, Offset.offset(0.001f))
    assertThat(values.setpointTemperatureCool).isEqualTo(0.8f, Offset.offset(0.001f))
    assertThat(values.flags).containsExactly(SuplaThermostatFlags.COOLING)
  }

  @Test
  fun `should create thermostat value from byte array (flag on second byte)`() {
    // given
    val bytes = byteArrayOf(0, 2, 120, 0, 80, 0, 8, 2)

    // when
    val values = ThermostatValue.from(false, bytes)

    // then
    assertThat(values.online).isFalse()
    assertThat(values.state.isOff()).isTrue
    assertThat(values.mode).isEqualTo(SuplaHvacMode.HEAT)
    assertThat(values.setpointTemperatureHeat).isEqualTo(1.2f, Offset.offset(0.001f))
    assertThat(values.setpointTemperatureCool).isEqualTo(0.8f, Offset.offset(0.001f))
    assertThat(values.flags).containsExactly(SuplaThermostatFlags.COOLING, SuplaThermostatFlags.FORCED_OFF_BY_SENSOR)
  }
}
