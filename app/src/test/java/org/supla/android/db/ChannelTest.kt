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
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.TemperaturePresenterFactory
import org.supla.android.db.Channel
import org.supla.android.cfg.CfgData
import org.supla.android.cfg.TemperatureUnit
import org.supla.android.data.presenter.TemperaturePresenterImpl
import org.supla.android.lib.SuplaConst
import java.nio.ByteBuffer

@RunWith(MockitoJUnitRunner::class)
class ChannelTest: TestCase() {

    private val celsiusCfg = CfgData("whatever", 0, "xxxx",
        "none@nowhere", false, TemperatureUnit.CELSIUS)
    private val fahrenheitCfg = CfgData("whatever", 0, "xxxx",
    "none@nowhere", false, TemperatureUnit.FAHRENHEIT)


    @Test
    fun testThermometerTemperatureCelsius() {
        val temperaturePresenterFactory: TemperaturePresenterFactory = mock {
            on { getTemperaturePresenter() } doReturn TemperaturePresenterImpl(celsiusCfg)
        }

        val ref: Double = 23.0
        val ch = Channel(temperaturePresenterFactory)
        val chval = ChannelValue()
        chval.channelValue = ByteBuffer.allocate(8).putDouble(ref).array().reversedArray()
        chval.onLine = true
        ch.func = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
        ch.value = chval

        assertEquals(ref, ch.temp)
        assertEquals("23.0°C", ch.humanReadableValue)
    }

    @Test
    fun testThermostatTemeratureHumidityCelsius() {
        val temperaturePresenterFactory: TemperaturePresenterFactory = mock {
            on { getTemperaturePresenter() } doReturn TemperaturePresenterImpl(celsiusCfg)
        }

        val ref: Int = 13
        val ch = Channel(temperaturePresenterFactory)
        val chval = ChannelValue()
        chval.channelValue = ByteBuffer.allocate(8)
            .putInt(0).putInt(ref * 1000)
            .array().reversedArray()
        chval.onLine = true
        ch.func = SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
        ch.value = chval

        assertEquals(ref.toDouble(), ch.temp)
        assertEquals("13.0°C", ch.humanReadableValue)
    }

    @Test
    fun testThermometerTemperatureFahrenheit() {
        val temperaturePresenterFactory: TemperaturePresenterFactory = mock {
            on { getTemperaturePresenter() } doReturn TemperaturePresenterImpl(fahrenheitCfg)
        }

        val ref: Double = 23.0 // measured value in Celsius
        val ch = Channel(temperaturePresenterFactory)
        val chval = ChannelValue()
        chval.channelValue = ByteBuffer.allocate(8).putDouble(ref).array().reversedArray()
        chval.onLine = true
        ch.func = SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
        ch.value = chval

        assertEquals(73.4, ch.temp)
        assertEquals("73.4°F", ch.humanReadableValue)
    }
}
