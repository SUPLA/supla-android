package org.supla.android.usecases.channel.valueformatter
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

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeBaseConfig

@RunWith(MockitoJUnitRunner::class)
class GpmValueFormatterTest {

  @Test
  fun `should format value according to value`() {
    // given
    val config = mockConfig("$", true, "k", false, 2)
    val formatter = GpmValueFormatter(config)

    // when
    val formatted = formatter.format(2.534, true)

    // then
    assertThat(formatted).isEqualTo("$2.53 k")
  }

  @Test
  fun `should get no value when NaN`() {
    // given
    val config = mockConfig()
    val formatter = GpmValueFormatter(config)

    // when
    val formatted = formatter.format(Double.NaN, true)

    // then
    assertThat(formatted).isEqualTo("---")
  }

  @Test
  fun `should get no value when not double`() {
    // given
    val config = mockConfig()
    val formatter = GpmValueFormatter(config)

    // when
    val formatted = formatter.format(123, true)

    // then
    assertThat(formatted).isEqualTo("---")
  }

  @Test
  fun `should format value according to precision`() {
    // given
    val config = mockConfig("$", false, "k", true, 4)
    val formatter = GpmValueFormatter(config)

    // when
    val formatted = formatter.format(2.534, true)

    // then
    assertThat(formatted).isEqualTo("$ 2.5340k")
  }

  private fun mockConfig(
    unitBefore: String = "",
    noSpaceBefore: Boolean = false,
    unitAfter: String = "",
    noSpaceAfter: Boolean = false,
    precision: Int = 2
  ): SuplaChannelGeneralPurposeBaseConfig {
    return mockk {
      every { unitBeforeValue } returns unitBefore
      every { noSpaceBeforeValue } returns noSpaceBefore
      every { unitAfterValue } returns unitAfter
      every { noSpaceAfterValue } returns noSpaceAfter
      every { valuePrecision } returns precision
    }
  }
}
