package org.supla.android.data.source.remote.thermostat
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

import org.assertj.core.api.Assertions
import org.assertj.core.data.Offset
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.function.thermostat.HomePlusThermostatValue
import org.supla.core.shared.data.model.function.thermostat.SuplaHeatpolThermostatFlag

@RunWith(MockitoJUnitRunner::class)
class HeatpolThermostatValueTest {

  @Test
  fun `should create thermostat value from byte array`() {
    // given
    val status = SuplaChannelAvailabilityStatus.ONLINE
    val bytes = byteArrayOf(1, 4, 120, 0, 80, 0)

    // when
    val values = HomePlusThermostatValue.from(status, bytes)

    // then
    Assertions.assertThat(values.status).isEqualTo(status)
    Assertions.assertThat(values.on).isTrue
    Assertions.assertThat(values.flags).containsExactly(SuplaHeatpolThermostatFlag.COOL_MODE)
    Assertions.assertThat(values.measuredTemperature).isEqualTo(1.2f, Offset.offset(0.001f))
    Assertions.assertThat(values.presetTemperature).isEqualTo(0.8f, Offset.offset(0.001f))
  }

  @Test
  fun `should create default value when array to short`() {
    // given
    val status = SuplaChannelAvailabilityStatus.OFFLINE
    val bytes = byteArrayOf(0, 0, 120, 0, 80)

    // when
    val values = HomePlusThermostatValue.from(status, bytes)

    // then
    Assertions.assertThat(values.status).isEqualTo(status)
    Assertions.assertThat(values.on).isFalse()
    Assertions.assertThat(values.flags).isEmpty()
    Assertions.assertThat(values.measuredTemperature).isEqualTo(0f, Offset.offset(0.001f))
    Assertions.assertThat(values.presetTemperature).isEqualTo(0f, Offset.offset(0.001f))
  }
}
