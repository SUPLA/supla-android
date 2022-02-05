package org.supla.android.cfg
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

import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.properties.Delegates

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class ChannelHeight(val percent: Int) { 
                                               HEIGHT_60(60), 
                                               HEIGHT_100(100), 
                                               HEIGHT_150(150)
}

data class CfgData(private var _temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
                   private var _buttonAutohide: Boolean = true,
                   private var _channelHeight: ChannelHeight = ChannelHeight.HEIGHT_100,
                   private var _showChannelInfo: Boolean,
                   private var _showOpeningPercent: Boolean) {
    val temperatureUnit = MutableLiveData<TemperatureUnit>(_temperatureUnit)
    val buttonAutohide = MutableLiveData<Boolean>(_buttonAutohide)
    val channelHeight = MutableLiveData<ChannelHeight>(_channelHeight)
    val showChannelInfo = MutableLiveData<Boolean>(_showChannelInfo)
    val showOpeningPercent = MutableLiveData<Boolean>(_showOpeningPercent)
}
