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
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_ELECTRICITY_MEASUREMENTS
import org.supla.android.lib.SuplaChannelValue.SUBV_TYPE_IC_MEASUREMENTS
import org.supla.core.shared.data.model.general.SuplaFunction

@RunWith(MockitoJUnitRunner::class)
class ProvideChannelDetailTypeUseCaseTest {

  @InjectMocks
  private lateinit var useCase: ProvideChannelDetailTypeUseCase

  @Test
  fun `should provide detail for dimmer`() {
    testDetailType(SuplaFunction.DIMMER, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for dimmer and RGB`() {
    testDetailType(SuplaFunction.DIMMER_AND_RGB_LIGHTING, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for RGB`() {
    testDetailType(SuplaFunction.RGB_LIGHTING, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for roller shutter`() {
    testDetailType(SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER, WindowDetailType(listOf(DetailPage.ROLLER_SHUTTER)))
  }

  @Test
  fun `should provide detail for roof window`() {
    testDetailType(SuplaFunction.CONTROLLING_THE_ROOF_WINDOW, WindowDetailType(listOf(DetailPage.ROOF_WINDOW)))
  }

  @Test
  fun `should provide detail for facade blinds`() {
    testDetailType(SuplaFunction.CONTROLLING_THE_FACADE_BLIND, WindowDetailType(listOf(DetailPage.FACADE_BLINDS)))
  }

  @Test
  fun `should provide detail for light switch with impulse counter`() {
    testDetailType(
      SuplaFunction.LIGHTSWITCH,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.IC_HISTORY))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_IC_MEASUREMENTS.toShort()
      every { channel.flags } returns 0
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for power switch with impulse counter`() {
    testDetailType(
      SuplaFunction.POWER_SWITCH,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.IC_HISTORY))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_IC_MEASUREMENTS.toShort()
      every { channel.flags } returns 0
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for stair case timer with impulse counter`() {
    testDetailType(
      SuplaFunction.STAIRCASE_TIMER,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.IC_HISTORY))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_IC_MEASUREMENTS.toShort()
      every { channel.flags } returns 0
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for light switch with measurement`() {
    testDetailType(
      SuplaFunction.LIGHTSWITCH,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.flags } returns 0
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for power switch with measurement`() {
    testDetailType(
      SuplaFunction.POWER_SWITCH,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.flags } returns 0
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for stair case timer with measurement and without timer even if supported`() {
    testDetailType(
      SuplaFunction.STAIRCASE_TIMER,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.flags } returns SuplaChannelFlag.COUNTDOWN_TIMER_SUPPORTED.rawValue
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for light switch with measurement and timer support`() {
    testDetailType(
      SuplaFunction.LIGHTSWITCH,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.SWITCH_TIMER, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.flags } returns SuplaChannelFlag.COUNTDOWN_TIMER_SUPPORTED.rawValue
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for power switch with measurement and timer support`() {
    testDetailType(
      SuplaFunction.POWER_SWITCH,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.SWITCH_TIMER, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.flags } returns SuplaChannelFlag.COUNTDOWN_TIMER_SUPPORTED.rawValue
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for stair case timer with measurement`() {
    testDetailType(
      SuplaFunction.STAIRCASE_TIMER,
      SwitchDetailType(listOf(DetailPage.SWITCH, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    ) { channel ->
      val channelValue: ChannelValueEntity = mockk()
      every { channelValue.subValueType } returns SUBV_TYPE_ELECTRICITY_MEASUREMENTS.toShort()
      every { channel.flags } returns 0
      every { channel.channelValueEntity } returns channelValue
    }
  }

  @Test
  fun `should provide detail for light switch`() {
    testDetailType(SuplaFunction.LIGHTSWITCH, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for power switch`() {
    testDetailType(SuplaFunction.POWER_SWITCH, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for stair case timer`() {
    testDetailType(SuplaFunction.STAIRCASE_TIMER, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for electricity meter`() {
    testDetailType(
      SuplaFunction.ELECTRICITY_METER,
      EmDetailType(listOf(DetailPage.EM_GENERAL, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    )
  }

  @Test
  fun `should provide detail for electricity IC`() {
    testDetailType(
      SuplaFunction.IC_ELECTRICITY_METER,
      IcDetailType(listOf(DetailPage.IC_GENERAL, DetailPage.IC_HISTORY))
    ) { channel ->
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for gas IC`() {
    testDetailType(
      SuplaFunction.IC_GAS_METER,
      IcDetailType(listOf(DetailPage.IC_GENERAL, DetailPage.IC_HISTORY, DetailPage.IC_OCR))
    ) { channel ->
      every { channel.flags } returns SuplaChannelFlag.OCR.rawValue
    }
  }

  @Test
  fun `should provide detail for water IC`() {
    testDetailType(
      SuplaFunction.IC_WATER_METER,
      IcDetailType(listOf(DetailPage.IC_GENERAL, DetailPage.IC_HISTORY))
    ) { channel ->
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for heat IC`() {
    testDetailType(
      SuplaFunction.IC_HEAT_METER,
      IcDetailType(listOf(DetailPage.IC_GENERAL, DetailPage.IC_HISTORY))
    ) { channel ->
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for thermometer`() {
    testDetailType(SuplaFunction.THERMOMETER, ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY)))
  }

  @Test
  fun `should provide detail for thermometer with humidity`() {
    testDetailType(SuplaFunction.HUMIDITY_AND_TEMPERATURE, ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY)))
  }

  @Test
  fun `should provide detail for HP thermostat`() {
    testDetailType(
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      ThermostatDetailType(listOf(DetailPage.THERMOSTAT_HEATPOL_GENERAL, DetailPage.THERMOSTAT_HEATPOL_HISTORY))
    )
  }

  @Test
  fun `should provide detail for digiglass`() {
    testDetailType(SuplaFunction.DIGIGLASS_VERTICAL, LegacyDetailType.DIGIGLASS)
    testDetailType(SuplaFunction.DIGIGLASS_HORIZONTAL, LegacyDetailType.DIGIGLASS)
  }

  @Test
  fun `should not provide detail for unsupported channel function`() {
    testDetailType(SuplaFunction.CONTROLLING_THE_GATE, null)
  }

  @Test
  fun `should provide detail for hvac thermostat heat`() {
    testDetailType(
      SuplaFunction.HVAC_THERMOSTAT,
      ThermostatDetailType(listOf(DetailPage.THERMOSTAT, DetailPage.SCHEDULE, DetailPage.THERMOSTAT_TIMER, DetailPage.THERMOSTAT_HISTORY))
    )
  }

  @Test
  fun `should not provide detail for hvac thermostat auto`() {
    testDetailType(SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL, null)
  }

  @Test
  fun `should provide detail for general purpose measurement`() {
    testDetailType(SuplaFunction.GENERAL_PURPOSE_MEASUREMENT, GpmDetailType(listOf(DetailPage.GPM_HISTORY)))
  }

  @Test
  fun `should provide detail for general purpose meter`() {
    testDetailType(SuplaFunction.GENERAL_PURPOSE_METER, GpmDetailType(listOf(DetailPage.GPM_HISTORY)))
  }

  @Test
  fun `should provide detail for terrace awning`() {
    testDetailType(SuplaFunction.TERRACE_AWNING, WindowDetailType(listOf(DetailPage.TERRACE_AWNING)))
  }

  @Test
  fun `should provide detail for projector screen`() {
    testDetailType(SuplaFunction.PROJECTOR_SCREEN, WindowDetailType(listOf(DetailPage.PROJECTOR_SCREEN)))
  }

  @Test
  fun `should provide detail for curtain`() {
    testDetailType(SuplaFunction.CURTAIN, WindowDetailType(listOf(DetailPage.CURTAIN)))
  }

  @Test
  fun `should provide detail for vertical blind`() {
    testDetailType(SuplaFunction.VERTICAL_BLIND, WindowDetailType(listOf(DetailPage.VERTICAL_BLIND)))
  }

  private fun testDetailType(function: SuplaFunction, result: DetailType?, extraMocks: ((ChannelDataEntity) -> Unit) = { }) {
    // given
    val channel: ChannelDataEntity = mockk()
    every { channel.function } returns function
    extraMocks(channel)

    // when
    val detailType = useCase(ChannelWithChildren(channel, emptyList()))

    // then
    if (result == null) {
      assertThat(detailType).isNull()
    } else {
      assertThat(detailType).isEqualTo(result)
    }
  }
}
