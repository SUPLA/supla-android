package org.supla.android.usecases.channel.measurementsprovider
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

class AggregationResultTest {

  @Test
  fun `should get available sums`() {
    // given
    val result = AggregationResult(
      list = listOf(),
      sum = listOf(1f, 2f, 3f)
    )

    // when
    val sum1 = result.nextSum()
    val sum2 = result.nextSum()
    val sum3 = result.nextSum()
    val sum4 = result.nextSum()

    // then
    assertThat(sum1).isEqualTo(1f)
    assertThat(sum2).isEqualTo(2f)
    assertThat(sum3).isEqualTo(3f)
    assertThat(sum4).isEqualTo(0f)
  }
}
