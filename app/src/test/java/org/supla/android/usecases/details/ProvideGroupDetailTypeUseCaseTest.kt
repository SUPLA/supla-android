package org.supla.android.usecases.details
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
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.features.details.detailbase.standarddetail.DetailPage

@RunWith(MockitoJUnitRunner::class)
class ProvideGroupDetailTypeUseCaseTest {

  @InjectMocks
  private lateinit var useCase: ProvideGroupDetailTypeUseCase

  @Test
  fun `should provide detail for dimmer`() {
    testDetailType(SuplaChannelFunction.DIMMER, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for dimmer and RGB`() {
    testDetailType(SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for RGB`() {
    testDetailType(SuplaChannelFunction.RGB_LIGHTING, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for roller shutter`() {
    testDetailType(SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER, WindowDetailType(listOf(DetailPage.ROLLER_SHUTTER)))
  }

  @Test
  fun `should provide detail for roof window`() {
    testDetailType(SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW, WindowDetailType(listOf(DetailPage.ROOF_WINDOW)))
  }

  @Test
  fun `should provide detail for facade blinds`() {
    testDetailType(SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND, WindowDetailType(listOf(DetailPage.FACADE_BLINDS)))
  }

  @Test
  fun `should provide detail for light switch`() {
    testDetailType(SuplaChannelFunction.LIGHTSWITCH, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for power switch`() {
    testDetailType(SuplaChannelFunction.POWER_SWITCH, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for stair case timer`() {
    testDetailType(SuplaChannelFunction.STAIRCASE_TIMER, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for electricity meter`() {
    testDetailType(
      SuplaChannelFunction.ELECTRICITY_METER,
      EmDetailType(listOf(DetailPage.EM_GENERAL, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    )
  }

  @Test
  fun `should provide detail for electricity IC`() {
    testDetailType(SuplaChannelFunction.IC_ELECTRICITY_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for gas IC`() {
    testDetailType(SuplaChannelFunction.IC_GAS_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for water IC`() {
    testDetailType(SuplaChannelFunction.IC_WATER_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for heat IC`() {
    testDetailType(SuplaChannelFunction.IC_HEAT_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for thermometer`() {
    testDetailType(SuplaChannelFunction.THERMOMETER, ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY)))
  }

  @Test
  fun `should provide detail for thermometer with humidity`() {
    testDetailType(SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE, ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY)))
  }

  @Test
  fun `should provide detail for HP thermostat`() {
    testDetailType(SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS, LegacyDetailType.THERMOSTAT_HP)
  }

  @Test
  fun `should provide detail for digiglass`() {
    testDetailType(SuplaChannelFunction.DIGIGLASS_VERTICAL, LegacyDetailType.DIGIGLASS)
    testDetailType(SuplaChannelFunction.DIGIGLASS_HORIZONTAL, LegacyDetailType.DIGIGLASS)
  }

  @Test
  fun `should not provide detail for unsupported channel function`() {
    testDetailType(SuplaChannelFunction.CONTROLLING_THE_GATE, null)
  }

  @Test
  fun `should provide detail for hvac thermostat heat`() {
    testDetailType(
      SuplaChannelFunction.HVAC_THERMOSTAT,
      ThermostatDetailType(listOf(DetailPage.THERMOSTAT, DetailPage.SCHEDULE, DetailPage.THERMOSTAT_TIMER, DetailPage.THERMOSTAT_HISTORY))
    )
  }

  @Test
  fun `should not provide detail for hvac thermostat auto`() {
    testDetailType(SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL, null)
  }

  @Test
  fun `should provide detail for general purpose measurement`() {
    testDetailType(SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT, GpmDetailType(listOf(DetailPage.GPM_HISTORY)))
  }

  @Test
  fun `should provide detail for general purpose meter`() {
    testDetailType(SuplaChannelFunction.GENERAL_PURPOSE_METER, GpmDetailType(listOf(DetailPage.GPM_HISTORY)))
  }

  @Test
  fun `should provide detail for terrace awning`() {
    testDetailType(SuplaChannelFunction.TERRACE_AWNING, WindowDetailType(listOf(DetailPage.TERRACE_AWNING)))
  }

  @Test
  fun `should provide detail for projector screen`() {
    testDetailType(SuplaChannelFunction.PROJECTOR_SCREEN, WindowDetailType(listOf(DetailPage.PROJECTOR_SCREEN)))
  }

  @Test
  fun `should provide detail for curtain`() {
    testDetailType(SuplaChannelFunction.CURTAIN, WindowDetailType(listOf(DetailPage.CURTAIN)))
  }

  @Test
  fun `should provide detail for vertical blind`() {
    testDetailType(SuplaChannelFunction.VERTICAL_BLIND, WindowDetailType(listOf(DetailPage.VERTICAL_BLIND)))
  }

  private fun testDetailType(function: SuplaChannelFunction, result: DetailType?, extraMocks: ((ChannelDataEntity) -> Unit) = { }) {
    // given
    val channel: ChannelDataEntity = mockk()
    every { channel.function } returns function
    extraMocks(channel)

    // when
    val detailType = useCase(channel)

    // then
    if (result == null) {
      assertThat(detailType).isNull()
    } else {
      assertThat(detailType).isEqualTo(result)
    }
  }
}