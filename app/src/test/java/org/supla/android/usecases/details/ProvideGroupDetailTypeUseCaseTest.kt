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
import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.lib.SuplaConst.SUPLA_BIT_FUNC_CONTROLLINGTHEGATE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CURTAIN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND

@RunWith(MockitoJUnitRunner::class)
class ProvideGroupDetailTypeUseCaseTest {

  @InjectMocks
  private lateinit var useCase: ProvideGroupDetailTypeUseCase

  @Test
  fun `should provide detail for dimmer`() {
    testDetailType(SUPLA_CHANNELFNC_DIMMER, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for dimmer and RGB`() {
    testDetailType(SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for RGB`() {
    testDetailType(SUPLA_CHANNELFNC_RGBLIGHTING, LegacyDetailType.RGBW)
  }

  @Test
  fun `should provide detail for roller shutter`() {
    testDetailType(SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER, WindowDetailType(listOf(DetailPage.ROLLER_SHUTTER)))
  }

  @Test
  fun `should provide detail for roof window`() {
    testDetailType(SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW, WindowDetailType(listOf(DetailPage.ROOF_WINDOW)))
  }

  @Test
  fun `should provide detail for facade blinds`() {
    testDetailType(SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND, WindowDetailType(listOf(DetailPage.FACADE_BLINDS)))
  }

  @Test
  fun `should provide detail for light switch`() {
    testDetailType(SUPLA_CHANNELFNC_LIGHTSWITCH, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for power switch`() {
    testDetailType(SUPLA_CHANNELFNC_POWERSWITCH, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for stair case timer`() {
    testDetailType(SUPLA_CHANNELFNC_STAIRCASETIMER, SwitchDetailType(listOf(DetailPage.SWITCH))) { channel ->
      every { channel.channelValueEntity } returns mockk {
        every { subValueType } returns 0
      }
      every { channel.flags } returns 0
    }
  }

  @Test
  fun `should provide detail for electricity meter`() {
    testDetailType(
      SUPLA_CHANNELFNC_ELECTRICITY_METER,
      EmDetailType(listOf(DetailPage.EM_GENERAL, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))
    )
  }

  @Test
  fun `should provide detail for electricity IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_ELECTRICITY_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for gas IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_GAS_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for water IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_WATER_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for heat IC`() {
    testDetailType(SUPLA_CHANNELFNC_IC_HEAT_METER, LegacyDetailType.IC)
  }

  @Test
  fun `should provide detail for thermometer`() {
    testDetailType(SUPLA_CHANNELFNC_THERMOMETER, ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY)))
  }

  @Test
  fun `should provide detail for thermometer with humidity`() {
    testDetailType(SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE, ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY)))
  }

  @Test
  fun `should provide detail for HP thermostat`() {
    testDetailType(SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS, LegacyDetailType.THERMOSTAT_HP)
  }

  @Test
  fun `should provide detail for digiglass`() {
    testDetailType(SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL, LegacyDetailType.DIGIGLASS)
    testDetailType(SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL, LegacyDetailType.DIGIGLASS)
  }

  @Test
  fun `should not provide detail for unsupported channel function`() {
    testDetailType(SUPLA_BIT_FUNC_CONTROLLINGTHEGATE, null)
  }

  @Test
  fun `should provide detail for hvac thermostat heat`() {
    testDetailType(
      SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
      ThermostatDetailType(listOf(DetailPage.THERMOSTAT, DetailPage.SCHEDULE, DetailPage.THERMOSTAT_TIMER, DetailPage.THERMOSTAT_HISTORY))
    )
  }

  @Test
  fun `should not provide detail for hvac thermostat auto`() {
    testDetailType(SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL, null)
  }

  @Test
  fun `should provide detail for general purpose measurement`() {
    testDetailType(SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT, GpmDetailType(listOf(DetailPage.GPM_HISTORY)))
  }

  @Test
  fun `should provide detail for general purpose meter`() {
    testDetailType(SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER, GpmDetailType(listOf(DetailPage.GPM_HISTORY)))
  }

  @Test
  fun `should provide detail for terrace awning`() {
    testDetailType(SUPLA_CHANNELFNC_TERRACE_AWNING, WindowDetailType(listOf(DetailPage.TERRACE_AWNING)))
  }

  @Test
  fun `should provide detail for projector screen`() {
    testDetailType(SUPLA_CHANNELFNC_PROJECTOR_SCREEN, WindowDetailType(listOf(DetailPage.PROJECTOR_SCREEN)))
  }

  @Test
  fun `should provide detail for curtain`() {
    testDetailType(SUPLA_CHANNELFNC_CURTAIN, WindowDetailType(listOf(DetailPage.CURTAIN)))
  }

  @Test
  fun `should provide detail for vertical blind`() {
    testDetailType(SUPLA_CHANNELFNC_VERTICAL_BLIND, WindowDetailType(listOf(DetailPage.VERTICAL_BLIND)))
  }

  private fun testDetailType(function: Int, result: DetailType?, extraMocks: ((ChannelDataEntity) -> Unit) = { }) {
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
