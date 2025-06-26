package org.supla.android.features.addwizard.model
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

import io.mockk.MockKAnnotations
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class EspTest {
  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun checkSuplaPrefix() {
    performTest(
      shortSsid = "SUPLA-AB12",
      longSsid = "SUPLA-123456789EFC",
      invalidSsids = arrayOf(
        "SUPLA-ZYCD",
        "SUPLA-ABCDEF",
        "SUPLA-123456789EFX"
      )
    )
  }

  @Test
  fun checkZamelPrefix() {
    performTest(
      shortSsid = "ZAMEL-AB12",
      longSsid = "ZAMEL-123456789EFC",
      invalidSsids = arrayOf(
        "ZAMEL-ZYCD",
        "ZAMEL-ABCDEF",
        "ZAMEL-123456789EFX"
      )
    )
  }

  @Test
  fun checkNicePrefix() {
    performTest(
      shortSsid = "NICE-AB12",
      longSsid = "NICE-123456789EFC",
      invalidSsids = arrayOf(
        "NICE-ZYCD",
        "NICE-ABCDEF",
        "NICE-123456789EFX"
      )
    )
  }

  @Test
  fun checkHeatpolPrefix() {
    performTest(
      shortSsid = "HEATPOL-AB12",
      longSsid = "HEATPOL-123456789EFC",
      invalidSsids = arrayOf(
        "HEATPOL-ZYCD",
        "HEATPOL-ABCDEF",
        "HEATPOL-123456789EFX"
      )
    )
  }

  @Test
  fun checkComelitPrefix() {
    performTest(
      shortSsid = "COMELIT-AB12",
      longSsid = "COMELIT-123456789EFC",
      invalidSsids = arrayOf(
        "COMELIT-ZYCD",
        "COMELIT-ABCDEF",
        "COMELIT-123456789EFX"
      )
    )
  }

  @Test
  fun checkPolierPrefix() {
    performTest(
      shortSsid = "POLIER-AB12",
      longSsid = "POLIER-123456789EFC",
      invalidSsids = arrayOf(
        "POLIER-ZYCD",
        "POLIER-ABCDEF",
        "POLIER-123456789EFX"
      )
    )
  }

  @Test
  fun checkErgoPrefix() {
    performTest(
      shortSsid = "ERGO-AB12",
      longSsid = "ERGO-123456789EFC",
      invalidSsids = arrayOf(
        "ERGO-ZYCD",
        "ERGO-ABCDEF",
        "ERGO-123456789EFX"
      )
    )
  }

  @Test
  fun checkSomefPrefix() {
    performTest(
      shortSsid = "SOMEF-AB12",
      longSsid = "SOMEF-123456789EFC",
      invalidSsids = arrayOf(
        "SOMEF-ZYCD",
        "SOMEF-ABCDEF",
        "SOMEF-123456789EFX"
      )
    )
  }

  @Test
  fun checkAuratonPrefix() {
    performTest(
      shortSsid = "AURATON-AB12",
      longSsid = "AURATON-123456789EFC",
      invalidSsids = arrayOf(
        "AURATON-ZYCD",
        "AURATON-ABCDEF",
        "AURATON-123456789EFX"
      )
    )
  }

  @Test
  fun checkHpdPrefix() {
    performTest(
      shortSsid = "HPD-AB12",
      longSsid = "HPD-123456789EFC",
      invalidSsids = arrayOf(
        "HPD-ZYCD",
        "HPD-ABCDEF",
        "HPD-123456789EFX"
      )
    )
  }

  private fun performTest(shortSsid: String, longSsid: String, vararg invalidSsids: String) {
    assertThat(Esp.isKnownNetworkName(shortSsid)).isTrue()
    assertThat(Esp.isKnownNetworkName(longSsid)).isTrue()
    invalidSsids.forEach {
      assertThat(Esp.isKnownNetworkName(it)).isFalse()
    }
  }
}
