package org.supla.android.data.source.local.calendar
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
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class HourTest {

  @Test
  fun `should parse hour with 00 00 format`() {
    // given
    val hourString = "12:33"

    // when
    val hour = Hour.from(hourString)

    // then
    assertThat(hour!!.minute).isEqualTo(33)
    assertThat(hour.hour).isEqualTo(12)
  }

  @Test
  fun `should parse hour with 0 00 format`() {
    // given
    val hourString = "1:33"

    // when
    val hour = Hour.from(hourString)

    // then
    assertThat(hour!!.minute).isEqualTo(33)
    assertThat(hour.hour).isEqualTo(1)
  }

  @Test
  fun `should parse hour with 00 0 format`() {
    // given
    val hourString = "12:3"

    // when
    val hour = Hour.from(hourString)

    // then
    assertThat(hour!!.minute).isEqualTo(3)
    assertThat(hour.hour).isEqualTo(12)
  }

  @Test
  fun `should not parse hour when has no minutes`() {
    // given
    val hourString = "12:"

    // when
    val hour = Hour.from(hourString)

    // then
    assertThat(hour).isNull()
  }

  @Test
  fun `should not parse hour when has no hours`() {
    // given
    val hourString = ":33"

    // when
    val hour = Hour.from(hourString)

    // then
    assertThat(hour).isNull()
  }

  @Test
  fun `should not parse hour when there is only number`() {
    // given
    val hourString = "33"

    // when
    val hour = Hour.from(hourString)

    // then
    assertThat(hour).isNull()
  }
}
