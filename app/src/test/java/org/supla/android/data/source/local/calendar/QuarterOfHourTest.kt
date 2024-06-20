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
class QuarterOfHourTest {

  @Test
  fun shouldCreateFirstQuarterFromMinutes() {
    // when
    val quarter = QuarterOfHour.from(12)

    // then
    assertThat(quarter).isEqualTo(QuarterOfHour.FIRST)
  }

  @Test
  fun shouldCreateSecondQuarterFromMinutes() {
    // when
    val quarter = QuarterOfHour.from(17)

    // then
    assertThat(quarter).isEqualTo(QuarterOfHour.SECOND)
  }

  @Test
  fun shouldCreateThirdQuarterFromMinutes() {
    // when
    val quarter = QuarterOfHour.from(37)

    // then
    assertThat(quarter).isEqualTo(QuarterOfHour.THIRD)
  }

  @Test
  fun shouldCreateFourthQuarterFromMinutes() {
    // when
    val quarter = QuarterOfHour.from(59)

    // then
    assertThat(quarter).isEqualTo(QuarterOfHour.FOURTH)
  }
}
