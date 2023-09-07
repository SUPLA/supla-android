package org.supla.android.usecases.list.eventmappers
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
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.local.entity.ThermostatValue
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.db.Channel
import org.supla.android.db.ChannelValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ChannelChild
import org.supla.android.usecases.channel.ChannelWithChildren

@RunWith(MockitoJUnitRunner::class)
class ChannelWithChildrenToThermostatUpdateEventMapperTest {

  @Mock
  lateinit var valuesFormatter: ValuesFormatter

  @InjectMocks
  lateinit var mapper: ChannelWithChildrenToThermostatUpdateEventMapper

  @Test
  fun `should handle channel with children`() {
    // given
    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL

    val channelWithChildren = ChannelWithChildren(channel, emptyList())

    // when
    val result = mapper.handle(channelWithChildren)

    // then
    assertThat(result).isTrue
  }

  @Test
  fun `should not handle channel`() {
    // given
    val channel = mockk<Channel>()

    // when
    val result = mapper.handle(channel)

    // then
    assertThat(result).isFalse
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (cool mode)`() {
    // given
    val setpointTemperatureMax = 23f
    val thermostatValue = mockThermostatValue(setpointTemperatureMax = setpointTemperatureMax)

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT_COOL
    every { channel.value } returns channelValue
    every { channel.onLine } returns true

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    val setpointTemperatureMaxString = "23.0"
    whenever(valuesFormatter.getTemperatureString(setpointTemperatureMax, true)).thenReturn(setpointTemperatureMaxString)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.value).isEqualTo(temperatureString)
    assertThat(result.subValue).isEqualTo(setpointTemperatureMaxString)
    assertThat(result.indicatorIcon).isEqualTo(R.drawable.ic_standby)
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (heat mode)`() {
    // given
    val setpointTemperatureMin = 23f
    val thermostatValue = mockThermostatValue(setpointTemperatureMin = setpointTemperatureMin, mode = SuplaHvacMode.HEAT)

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT
    every { channel.value } returns channelValue
    every { channel.onLine } returns true

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    val setpointTemperatureMinString = "23.0"
    whenever(valuesFormatter.getTemperatureString(setpointTemperatureMin, true)).thenReturn(setpointTemperatureMinString)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.value).isEqualTo(temperatureString)
    assertThat(result.subValue).isEqualTo(setpointTemperatureMinString)
    assertThat(result.indicatorIcon).isEqualTo(R.drawable.ic_standby)
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (off mode)`() {
    // given
    val thermostatValue = mockThermostatValue(mode = SuplaHvacMode.OFF)

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
    every { channel.value } returns channelValue
    every { channel.onLine } returns true

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.value).isEqualTo(temperatureString)
    assertThat(result.subValue).isEqualTo("Off")
    assertThat(result.indicatorIcon).isNull()
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (offline)`() {
    // given
    val thermostatValue = mockThermostatValue(mode = SuplaHvacMode.OFF)

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
    every { channel.value } returns channelValue
    every { channel.onLine } returns false

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isFalse
    assertThat(result.value).isEqualTo(temperatureString)
    assertThat(result.subValue).isEqualTo("")
    assertThat(result.indicatorIcon).isNull()
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (auto mode)`() {
    testMappingChannelWithThermometer_autoFunction(emptyList(), R.drawable.ic_standby)
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (heating)`() {
    testMappingChannelWithThermometer_autoFunction(listOf(SuplaThermostatFlags.HEATING), R.drawable.ic_heating)
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (cooling)`() {
    testMappingChannelWithThermometer_autoFunction(listOf(SuplaThermostatFlags.COOLING), R.drawable.ic_cooling)
  }

  private fun testMappingChannelWithThermometer_autoFunction(flags: List<SuplaThermostatFlags>, indicatorIcon: Int) {
    // given
    val setpointTemperatureMin = 23f
    val setpointTemperatureMax = 26f
    val thermostatValue = mockThermostatValue(
      setpointTemperatureMin = setpointTemperatureMin,
      setpointTemperatureMax = setpointTemperatureMax,
      mode = SuplaHvacMode.AUTO,
      flags = flags
    )

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
    every { channel.value } returns channelValue
    every { channel.onLine } returns true

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    whenever(valuesFormatter.getTemperatureString(setpointTemperatureMin, true)).thenReturn("23.0")
    whenever(valuesFormatter.getTemperatureString(setpointTemperatureMax, true)).thenReturn("26.0")

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.value).isEqualTo(temperatureString)
    assertThat(result.subValue).isEqualTo("23.0 - 26.0")
    assertThat(result.indicatorIcon).isEqualTo(indicatorIcon)
  }

  private fun mockThermostatValue(
    mode: SuplaHvacMode = SuplaHvacMode.COOL,
    setpointTemperatureMin: Float = 0f,
    setpointTemperatureMax: Float = 0f,
    flags: List<SuplaThermostatFlags> = emptyList()
  ) = mockk<ThermostatValue>().also {
    every { it.mode } returns mode
    every { it.setpointTemperatureMax } returns setpointTemperatureMax
    every { it.setpointTemperatureMin } returns setpointTemperatureMin
    every { it.flags } returns flags
  }
}