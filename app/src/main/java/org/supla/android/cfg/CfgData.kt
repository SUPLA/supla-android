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

data class CfgData(private var _serverAddr: String,
                   private var _accessID: Int,
                   private var _accessIDpwd: String,
                   private var _email: String,
                   private var _isAdvanced: Boolean,
                   private var _temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS) {
    val serverAddr = MutableLiveData<String>(_serverAddr)
    val accessID = MutableLiveData<Int>(_accessID)
    val accessIDpwd = MutableLiveData<String>(_accessIDpwd)
    val email = MutableLiveData<String>(_email)
    val temperatureUnit = MutableLiveData<TemperatureUnit>(_temperatureUnit)
    val isAdvanced = MutableLiveData<Boolean>(_isAdvanced)
}
