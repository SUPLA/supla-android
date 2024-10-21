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
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS
import org.supla.android.usecases.channel.GetChannelValueUseCase
import org.supla.android.usecases.channel.electricitymeter.ElectricityMeasurements
import org.supla.core.shared.data.SuplaChannelFunction

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
    val channel: ChannelDataEntity = mockk {
      every { channelEntity } returns mockk {
        every { function } returns SuplaChannelFunction.POWER_SWITCH
      }
      every { channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
    }
    val state: ElectricityMeterState = mockk()

    // when
    val result = handler.updateState(state, channel)

    // then
    assertThat(result).isSameAs(state)
  }

  @Test
  fun `should update state when there is electricity meter assigned`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { channelEntity } returns mockk {
        every { function } returns SuplaChannelFunction.POWER_SWITCH
      }
      every { channelValueEntity } returns mockk {
        every { subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      }
      every { isOnline() } returns true
    }
    val value = 123.45
    val state = ElectricityMeterState()
    val monthForwardEnergy: EnergyData = mockk()
    val monthReverseEnergy: EnergyData = mockk()
    val measurements: ElectricityMeasurements = mockk {
      every { toForwardEnergy(any()) } returns monthForwardEnergy
      every { toReverseEnergy(any()) } returns monthReverseEnergy
    }

    every { getChannelValueUseCase<Double>(channel) } returns value

    // when
    val result = handler.updateState(state, channel, measurements)

    // then
    assertThat(result).isEqualTo(
      state.copy(
        online = true,
        totalForwardActiveEnergy = EnergyData("123.5 kWh"),
        currentMonthForwardActiveEnergy = monthForwardEnergy,
        currentMonthReversedActiveEnergy = monthReverseEnergy,
        vectorBalancedValues = emptyMap()
      )
    )
  }

  @Test
  fun `should update state for electricity meter`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { channelEntity } returns mockk {
        every { function } returns SuplaChannelFunction.ELECTRICITY_METER
      }
      every { channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { isOnline() } returns true
    }
    every { getChannelValueUseCase<Double>(channel) } returns Double.NaN

    val state = ElectricityMeterState()

    // when
    val result = handler.updateState(state, channel)

    // then
    assertThat(result).isEqualTo(
      state.copy(
        online = true,
        totalForwardActiveEnergy = EnergyData("---"),
        vectorBalancedValues = emptyMap()
      )
    )
  }

  @Test
  fun `should create state for electricity meter`() {
    // given
    val channel: ChannelDataEntity = mockk {
      every { channelEntity } returns mockk {
        every { function } returns SuplaChannelFunction.POWER_SWITCH
      }
      every { channelValueEntity } returns mockk {
        every { subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      }
      every { isOnline() } returns false
    }
    every { getChannelValueUseCase<Double>(channel) } returns Double.NaN

    // when
    val result = handler.updateState(null, channel)

    // then
    assertThat(result).isEqualTo(
      ElectricityMeterState(
        online = false,
        totalForwardActiveEnergy = EnergyData("---"),
        vectorBalancedValues = emptyMap()
      )
    )
  }
}
