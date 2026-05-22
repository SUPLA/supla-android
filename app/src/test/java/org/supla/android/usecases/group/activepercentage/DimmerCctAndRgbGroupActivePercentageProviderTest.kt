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
import org.supla.android.usecases.group.totalvalue.DimmerCctAndRgbGroupValue
import org.supla.core.shared.data.model.general.SuplaFunction

class DimmerCctAndRgbGroupActivePercentageProviderTest {

  @Test
  fun `should handle dimmer cct and rgb function`() {
    assertThat(DimmerCctAndRgbGroupActivePercentageProvider.handleFunction(SuplaFunction.DIMMER_CCT_AND_RGB)).isTrue()
    assertThat(DimmerCctAndRgbGroupActivePercentageProvider.handleFunction(SuplaFunction.DIMMER_AND_RGB_LIGHTING)).isFalse()
  }

  @Test
  fun `should calculate active percentage for all values`() {
    assertThat(DimmerCctAndRgbGroupActivePercentageProvider.getActivePercentage(0, values())).isEqualTo(75)
  }

  @Test
  fun `should calculate active percentage for brightness channel`() {
    assertThat(DimmerCctAndRgbGroupActivePercentageProvider.getActivePercentage(1, values())).isEqualTo(75)
  }

  @Test
  fun `should calculate active percentage for cct channel`() {
    assertThat(DimmerCctAndRgbGroupActivePercentageProvider.getActivePercentage(2, values())).isEqualTo(75)
  }

  private fun values() = listOf(
    DimmerCctAndRgbGroupValue(color = 20, brightnessColor = 100, brightness = 40, cct = 2400),
    DimmerCctAndRgbGroupValue(color = 20, brightnessColor = 0, brightness = 0, cct = 3000),
    DimmerCctAndRgbGroupValue(color = 40, brightnessColor = 100, brightness = 20, cct = 2600),
    DimmerCctAndRgbGroupValue(color = 10, brightnessColor = 100, brightness = 10, cct = 2800)
  )
}
