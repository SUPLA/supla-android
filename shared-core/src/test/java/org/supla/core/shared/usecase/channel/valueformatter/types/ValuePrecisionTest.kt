package org.supla.core.shared.usecase.channel.valueformatter.types
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

class ValuePrecisionTest {
  @Test
  fun `should create exact precision`() {
    // given
    val precisionValue = 5

    // when
    val precision = ValuePrecision.exact(precisionValue)

    // then
    assertThat(precision.min).isEqualTo(precisionValue)
    assertThat(precision.max).isEqualTo(precisionValue)
  }

  @Test
  fun `should create at most precision`() {
    // given
    val precisionValue = 5

    // when
    val precision = ValuePrecision.atMost(precisionValue)

    // then
    assertThat(precision.min).isEqualTo(0)
    assertThat(precision.max).isEqualTo(precisionValue)
  }

  @Test
  fun `should create between precision`() {
    // given
    val min = 1
    val max = 5

    // when
    val precision = ValuePrecision.between(min, max)

    // then
    assertThat(precision.min).isEqualTo(min)
    assertThat(precision.max).isEqualTo(max)
  }
}
