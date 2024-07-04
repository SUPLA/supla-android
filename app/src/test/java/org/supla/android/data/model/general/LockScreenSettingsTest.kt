package org.supla.android.data.model.general
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

class LockScreenSettingsTest {

  @Test
  fun `should convert to string and back`() {
    // given
    val settings = LockScreenSettings(LockScreenScope.APPLICATION, "sum", true, 10, 15)

    // when
    val settingsString = settings.asString()
    val result = LockScreenSettings.from(settingsString)

    // then
    assertThat(result).isEqualTo(settings)
    assertThat(result.pinForAppRequired).isTrue()
  }

  @Test
  fun `should convert to string and back - with nulls`() {
    // given
    val settings = LockScreenSettings(LockScreenScope.ACCOUNTS, null, false, 0, null)

    // when
    val settingsString = settings.asString()
    val result = LockScreenSettings.from(settingsString)

    // then
    assertThat(result).isEqualTo(settings)
    assertThat(result.pinForAppRequired).isFalse()
  }

  @Test
  fun `should get default settings when could not parse`() {
    assertThat(LockScreenSettings.from(null)).isSameAs(LockScreenSettings.DEFAULT)
    assertThat(LockScreenSettings.from("0:2")).isSameAs(LockScreenSettings.DEFAULT)
    assertThat(LockScreenSettings.from("0:2:2:pp:")).isSameAs(LockScreenSettings.DEFAULT)
  }
}
