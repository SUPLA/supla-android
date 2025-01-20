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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.ui.views.card.SummaryCardData
import org.supla.android.usecases.channel.GetChannelValueUseCase
import org.supla.android.usecases.channel.measurements.ElectricityMeasurements

class NoExtendedValueStateHandlerTest {
  @MockK
  private lateinit var getChannelValueUseCase: GetChannelValueUseCase

  @InjectMockKs
  private lateinit var handler: NoExtendedValueStateHandler

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should not manipulate state when there is no electricity meter assigned`() {
    // given
    val channelWithChildren: ChannelWithChildren = mockk {
      every { isOrHasElectricityMeter } returns false
    }
    val state: ElectricityMeterState = mockk()

    // when
    val result = handler.updateState(state, channelWithChildren)

    // then
    assertThat(result).isSameAs(state)
  }

  @Test
  fun `should update state when there is electricity meter assigned`() {
    // given
    val channel: ChannelDataEntity = mockk()
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
      every { isOrHasElectricityMeter } returns true
      every { isOnline() } returns true
    }
    val value = 123.45
    val state = ElectricityMeterState()
    val monthForwardEnergy: SummaryCardData = mockk()
    val monthReverseEnergy: SummaryCardData = mockk()
    val measurements: ElectricityMeasurements = mockk {
      every { toForwardEnergy(any()) } returns monthForwardEnergy
      every { toReverseEnergy(any()) } returns monthReverseEnergy
    }

    every { getChannelValueUseCase<Double>(channelWithChildren) } returns value

    // when
    val result = handler.updateState(state, channelWithChildren, measurements)

    // then
    assertThat(result).isEqualTo(
      state.copy(
        online = true,
        totalForwardActiveEnergy = SummaryCardData("123.5 kWh"),
        currentMonthForwardActiveEnergy = monthForwardEnergy,
        currentMonthReversedActiveEnergy = monthReverseEnergy,
        vectorBalancedValues = null
      )
    )
  }

  @Test
  fun `should update state for electricity meter`() {
    // given
    val channel: ChannelDataEntity = mockk()
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
      every { isOrHasElectricityMeter } returns true
      every { isOnline() } returns true
    }
    every { getChannelValueUseCase<Double>(channelWithChildren) } returns Double.NaN

    val state = ElectricityMeterState()

    // when
    val result = handler.updateState(state, channelWithChildren)

    // then
    assertThat(result).isEqualTo(
      state.copy(
        online = true,
        totalForwardActiveEnergy = SummaryCardData("---"),
        vectorBalancedValues = null
      )
    )
  }

  @Test
  fun `should create state for electricity meter`() {
    // given
    val channel: ChannelDataEntity = mockk()
    val channelWithChildren: ChannelWithChildren = mockk {
      every { this@mockk.channel } returns channel
      every { isOrHasElectricityMeter } returns true
      every { isOnline() } returns false
    }
    every { getChannelValueUseCase<Double>(channelWithChildren) } returns Double.NaN

    // when
    val result = handler.updateState(null, channelWithChildren)

    // then
    assertThat(result).isEqualTo(
      ElectricityMeterState(
        online = false,
        totalForwardActiveEnergy = SummaryCardData("---"),
        vectorBalancedValues = null
      )
    )
  }
}
