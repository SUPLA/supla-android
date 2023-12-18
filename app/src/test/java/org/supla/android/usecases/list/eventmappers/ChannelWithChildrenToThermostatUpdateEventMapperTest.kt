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
import org.supla.android.db.ChannelExtendedValue
import org.supla.android.db.ChannelValue
import org.supla.android.extensions.date
import org.supla.android.lib.SuplaChannelExtendedValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
import org.supla.android.lib.SuplaTimerState
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
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT

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
    val setpointTemperatureCool = 23f
    val thermostatValue = mockThermostatValue(setpointTemperatureCool = setpointTemperatureCool)

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    every { channel.value } returns channelValue
    every { channel.onLine } returns true
    every { channel.extendedValue } returns null

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    val setpointTemperatureCoolString = "23.0"
    whenever(valuesFormatter.getTemperatureString(setpointTemperatureCool, true)).thenReturn(setpointTemperatureCoolString)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.value).isEqualTo(temperatureString)
    assertThat(result.subValue).isEqualTo(setpointTemperatureCoolString)
    assertThat(result.indicatorIcon).isEqualTo(R.drawable.ic_standby)
  }

  @Test
  fun `should map channel with thermometer to thermostat slideable item (heat mode)`() {
    // given
    val timerEndDate = date(2023, 10, 11, 12, 33, 15)
    val setpointTemperatureHeat = 23f
    val thermostatValue = mockThermostatValue(setpointTemperatureHeat = setpointTemperatureHeat, mode = SuplaHvacMode.HEAT)

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val suplaExtendedValue = SuplaChannelExtendedValue()
    suplaExtendedValue.TimerStateValue = SuplaTimerState(timerEndDate.time.div(1000), null, 1, "")
    val extendedValue: ChannelExtendedValue = mockk()
    every { extendedValue.extendedValue } returns suplaExtendedValue

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT
    every { channel.value } returns channelValue
    every { channel.onLine } returns true
    every { channel.extendedValue } returns extendedValue

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    val setpointTemperatureHeatString = "23.0"
    whenever(valuesFormatter.getTemperatureString(setpointTemperatureHeat, true)).thenReturn(setpointTemperatureHeatString)

    // when
    val result = mapper.map(channelWithChildren) as SlideableListItemData.Thermostat

    // then
    assertThat(result.online).isTrue
    assertThat(result.value).isEqualTo(temperatureString)
    assertThat(result.subValue).isEqualTo(setpointTemperatureHeatString)
    assertThat(result.indicatorIcon).isEqualTo(R.drawable.ic_standby)
    assertThat(result.estimatedTimerEndDate).isEqualTo(timerEndDate)
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
    every { channel.extendedValue } returns null

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
    every { channel.extendedValue } returns null

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
    val setpointTemperatureHeat = 23f
    val setpointTemperatureCool = 26f
    val thermostatValue = mockThermostatValue(
      setpointTemperatureHeat = setpointTemperatureHeat,
      setpointTemperatureCool = setpointTemperatureCool,
      mode = SuplaHvacMode.HEAT_COOL,
      flags = flags
    )

    val channelValue = mockk<ChannelValue>().also { every { it.asThermostatValue() } returns thermostatValue }

    val channel = mockk<Channel>()
    every { channel.func } returns SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO
    every { channel.value } returns channelValue
    every { channel.onLine } returns true
    every { channel.extendedValue } returns null

    val temperatureString = "21.0"
    val thermometerChannel = mockk<Channel>()
    every { thermometerChannel.humanReadableValue } returns temperatureString
    val channelChild = ChannelChild(ChannelRelationType.MAIN_THERMOMETER, thermometerChannel)

    val channelWithChildren = ChannelWithChildren(channel, listOf(channelChild))

    whenever(valuesFormatter.getTemperatureString(setpointTemperatureHeat, true)).thenReturn("23.0")
    whenever(valuesFormatter.getTemperatureString(setpointTemperatureCool, true)).thenReturn("26.0")

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
    setpointTemperatureHeat: Float = 0f,
    setpointTemperatureCool: Float = 0f,
    flags: List<SuplaThermostatFlags> = emptyList()
  ) = mockk<ThermostatValue>().also {
    every { it.mode } returns mode
    every { it.setpointTemperatureCool } returns setpointTemperatureCool
    every { it.setpointTemperatureHeat } returns setpointTemperatureHeat
    every { it.flags } returns flags
  }
}
