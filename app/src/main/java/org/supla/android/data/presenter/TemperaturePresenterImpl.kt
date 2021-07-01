package org.supla.android.data.presenter
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

import org.supla.android.db.ChannelValue
import org.supla.android.cfg.CfgData
import org.supla.android.cfg.TemperatureUnit
import org.supla.android.db.ChannelBase

class TemperaturePresenterImpl(private val config: CfgData): TemperaturePresenter {

    override fun getTemp(value: ChannelValue, channel: ChannelBase): Double {
        return getConvertedValue(value.getTemp(channel.func))
    }

    override fun formattedWithUnit(value: ChannelValue, channel: ChannelBase): String {
        return String.format("%.1f%s", getTemp(value, channel), getUnitString())
    }

    override fun getConvertedValue(rawValue: Double): Double {
        if(rawValue <= ChannelBase.TEMPERATURE_NA_VALUE) {
            return rawValue // Pass-through special value without conversion.
        }
        return if(config.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            toFahrenheit(rawValue)
        } else {
            rawValue
        }
    }

    override fun getUnitString(): String {
        return if (config.temperatureUnit==TemperatureUnit.FAHRENHEIT) "\u00B0F" else "\u00B0C"
    }

    private fun toFahrenheit(celsiusValue: Double): Double {
        return 9.0/5.0 * celsiusValue + 32.0
    }
}