package org.supla.android.usecases.group.activepercentage
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
import org.supla.android.usecases.group.totalvalue.DimmerCctGroupValue
import org.supla.core.shared.data.model.general.SuplaFunction

class DimmerCctGroupActivePercentageProviderTest {

  @Test
  fun `should handle dimmer cct function`() {
    assertThat(DimmerCctGroupActivePercentageProvider.handleFunction(SuplaFunction.DIMMER_CCT)).isTrue()
    assertThat(DimmerCctGroupActivePercentageProvider.handleFunction(SuplaFunction.DIMMER)).isFalse()
  }

  @Test
  fun `should calculate active percentage based on brightness`() {
    val values = listOf(
      DimmerCctGroupValue(brightness = 100, cct = 2400),
      DimmerCctGroupValue(brightness = 0, cct = 3000),
      DimmerCctGroupValue(brightness = 50, cct = 2700),
      DimmerCctGroupValue(brightness = 100, cct = 4000)
    )

    assertThat(DimmerCctGroupActivePercentageProvider.getActivePercentage(0, values)).isEqualTo(75)
  }
}
