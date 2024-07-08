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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ThermostatIndicatorIconTest {

  @Test
  fun `should merge and get more important indicator`() {
    // when
    val result = ThermostatIndicatorIcon.COOLING mergeWith ThermostatIndicatorIcon.STANDBY

    // then
    assertThat(result).isEqualTo(ThermostatIndicatorIcon.COOLING)
  }

  @Test
  fun `should merge with null`() {
    // when
    val result = ThermostatIndicatorIcon.OFF mergeWith null

    // then
    assertThat(result).isEqualTo(ThermostatIndicatorIcon.OFF)
  }
}
