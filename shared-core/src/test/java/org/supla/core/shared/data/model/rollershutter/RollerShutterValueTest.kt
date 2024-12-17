package org.supla.core.shared.data.model.rollershutter
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
import org.supla.core.shared.data.model.function.rollershutter.RollerShutterValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag

class RollerShutterValueTest {

  @Test
  fun shouldCreateFromByteArray() {
    // given
    val online = true
    val bytes = byteArrayOf(0x37, 0x2D, 0x23, 0x9, 0x0) // 55, 45, 35, 9, 0

    // when
    val value = RollerShutterValue.from(online, bytes)

    // then
    assertThat(value.online).isEqualTo(true)
    assertThat(value.position).isEqualTo(55)
    assertThat(value.bottomPosition).isEqualTo(35)
    assertThat(value.flags).containsExactly(
      SuplaShadingSystemFlag.TILT_IS_SET,
      SuplaShadingSystemFlag.MOTOR_PROBLEM
    )
  }

  @Test
  fun shouldCreateDefaultValueWhenByteArrayToShort() {
    // given
    val online = false
    val bytes = byteArrayOf(0x37, 0x2D)

    // when
    val value = RollerShutterValue.from(online, bytes)

    // then
    assertThat(value.online).isEqualTo(false)
    assertThat(value.position).isEqualTo(-1)
    assertThat(value.bottomPosition).isEqualTo(0)
    assertThat(value.flags).isEmpty()
  }

  @Test
  fun `should get invalid value when out of range`() {
    // given
    val online = false
    val bytes = byteArrayOf(-50, 0, 127, 2, 0)

    // when
    val value = RollerShutterValue.from(online, bytes)

    // when
    assertThat(value.online).isFalse()
    assertThat(value.position).isEqualTo(-1)
    assertThat(value.bottomPosition).isEqualTo(100)
    assertThat(value.flags).containsExactly(SuplaShadingSystemFlag.CALIBRATION_FAILED)

    assertThat(value.hasValidPosition()).isFalse()

    assertThat(value.alwaysValidPosition).isEqualTo(0)
  }
}
