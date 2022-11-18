package org.supla.android.db
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

import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import org.supla.android.ValuesFormatterProvider
import org.supla.android.cfg.*
import org.supla.android.data.ValuesFormatter
import org.supla.android.lib.SuplaConst
import java.nio.ByteBuffer

@RunWith(MockitoJUnitRunner::class)
class ChannelTest : TestCase() {

  private val celsiusCfg = CfgData(
    TemperatureUnit.CELSIUS,
    true,
    ChannelHeight.HEIGHT_100,
    true,
    false
  )
  private val fahrenheitCfg = CfgData(
    TemperatureUnit.FAHRENHEIT,
    true,
    ChannelHeight.HEIGHT_100,
    true,
    false
  )

  @Before
  fun setup() {
  }

  @Test
  fun testThermometerTemperatureCelsius() {
    val cfgRepository: AppConfigurationProvider = mock {
      on { getConfiguration() } doReturn celsiusCfg
    }
    val valuesFormatterFactory = object : ValuesFormatterProvider {
      override fun getValuesFormatter(): ValuesFormatter =
        ValuesFormatter(cfgRepository)
    }

    val ref = 23.0
    val ch = Channel(valuesFormatterFactory)
    val chval = ChannelValue()
    chval.channelValue = ByteBuffer.allocate(8).putDouble(ref).array().reversedArray()
    chval.onLine = true
    ch.func = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    ch.value = chval

    val expectedReadable =
      String.format("%.1f", ref) + "°C" // Depends from locale it may be 23.0 or 23,0
    assertEquals(expectedReadable, ch.humanReadableValue)
  }

  @Test
  fun testThermostatTemeratureHumidityCelsius() {
    val cfgRepository: AppConfigurationProvider = mock {
      on { getConfiguration() } doReturn celsiusCfg
    }
    val valuesFormatterFactory = object : ValuesFormatterProvider {
      override fun getValuesFormatter(): ValuesFormatter =
        ValuesFormatter(cfgRepository)
    }

    val ref = 13
    val ch = Channel(valuesFormatterFactory)
    val chval = ChannelValue()
    chval.channelValue = ByteBuffer.allocate(8)
      .putInt(0).putInt(ref * 1000)
      .array().reversedArray()
    chval.onLine = true
    ch.func = SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
    ch.value = chval

    val expectedReadable =
      String.format("%.1f", ref.toDouble()) + "°C" // Depends from locale it may be 13.0 or 13,0
    assertEquals(expectedReadable, ch.humanReadableValue)
  }

  @Test
  fun testThermometerTemperatureFahrenheit() {
    val cfgRepository: AppConfigurationProvider = mock {
      on { getConfiguration() } doReturn fahrenheitCfg
    }
    val valuesFormatterFactory = object : ValuesFormatterProvider {
      override fun getValuesFormatter(): ValuesFormatter =
        ValuesFormatter(cfgRepository)
    }

    val ref = 23.0 // measured value in Celsius
    val ch = Channel(valuesFormatterFactory)
    val chval = ChannelValue()
    chval.channelValue = ByteBuffer.allocate(8).putDouble(ref).array().reversedArray()
    chval.onLine = true
    ch.func = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
    ch.value = chval

    val expectedReadable =
      String.format("%.1f", 73.4) + "°F" // Depends from locale it may be 73.4 or 73,4
    assertEquals(expectedReadable, ch.humanReadableValue)
  }
}
