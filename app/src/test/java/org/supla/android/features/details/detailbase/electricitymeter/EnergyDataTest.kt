package org.supla.android.features.details.detailbase.electricitymeter
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
import org.assertj.core.api.Assertions
import org.junit.Test
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter

class EnergyDataTest {

  @Test
  fun `should create data with price`() {
    // given
    val energy = 123.45
    val price = 10.0
    val currency = "PLN"
    val energyString = "123,45 kWh"

    val formatter: ChannelValueFormatter = mockk {
      every { format(energy) } returns energyString
    }

    // when
    val result = EnergyData(formatter, energy, price, currency)

    // then
    Assertions.assertThat(result)
      .extracting({ it.energy }, { it.price })
      .containsExactly(energyString, "1234.50 PLN")
  }

  @Test
  fun `should create data without price when it is zero`() {
    // given
    val energy = 123.45
    val price = 0.0
    val currency = "PLN"
    val energyString = "123,45 kWh"

    val formatter: ChannelValueFormatter = mockk {
      every { format(energy) } returns energyString
    }

    // when
    val result = EnergyData(formatter, energy, price, currency)

    // then
    Assertions.assertThat(result)
      .extracting({ it.energy }, { it.price })
      .containsExactly(energyString, null)
  }
}
