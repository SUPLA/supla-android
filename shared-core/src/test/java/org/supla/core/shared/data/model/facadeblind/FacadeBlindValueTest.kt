package org.supla.core.shared.data.model.facadeblind
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
import org.supla.core.shared.data.model.function.facadeblind.FacadeBlindValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag

class FacadeBlindValueTest {

  @Test
  fun `should create value from byte array`() {
    // given
    val online = true
    val bytes = byteArrayOf(55, 25, 0, 1, 1)

    // when
    val value = FacadeBlindValue.from(online, bytes)

    // when
    assertThat(value.online).isTrue()
    assertThat(value.position).isEqualTo(55)
    assertThat(value.tilt).isEqualTo(25)
    assertThat(value.flags).containsExactly(SuplaShadingSystemFlag.TILT_IS_SET)

    assertThat(value.hasValidPosition()).isTrue()
    assertThat(value.hasValidTilt()).isTrue()

    assertThat(value.alwaysValidPosition).isEqualTo(55)
    assertThat(value.alwaysValidTilt).isEqualTo(25)
  }

  @Test
  fun `should get default value when byte array is to short`() {
    // given
    val online = false
    val bytes = byteArrayOf(55, 25, 0)

    // when
    val value = FacadeBlindValue.from(online, bytes)

    // when
    assertThat(value.online).isFalse()
    assertThat(value.position).isEqualTo(-1)
    assertThat(value.tilt).isEqualTo(-1)
    assertThat(value.flags).isEmpty()

    assertThat(value.hasValidPosition()).isFalse()
    assertThat(value.hasValidTilt()).isFalse()

    assertThat(value.alwaysValidPosition).isEqualTo(0)
    assertThat(value.alwaysValidTilt).isEqualTo(0)
  }

  @Test
  fun `should get invalid value when out of range`() {
    // given
    val online = false
    val bytes = byteArrayOf(122, 103, 0, 2, 0)

    // when
    val value = FacadeBlindValue.from(online, bytes)

    // when
    assertThat(value.online).isFalse()
    assertThat(value.position).isEqualTo(-1)
    assertThat(value.tilt).isEqualTo(-1)
    assertThat(value.flags).containsExactly(SuplaShadingSystemFlag.CALIBRATION_FAILED)

    assertThat(value.hasValidPosition()).isFalse()
    assertThat(value.hasValidTilt()).isFalse()

    assertThat(value.alwaysValidPosition).isEqualTo(0)
    assertThat(value.alwaysValidTilt).isEqualTo(0)
  }
}
