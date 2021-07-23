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

import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class CfgViewModel(private val repository: CfgRepository): ViewModel() {

    enum class NavigationFlow { CREATE_ACCOUNT, STATUS, MAIN }
    val cfgData: CfgData = repository.getCfg()

    private val _didSaveConfig = MutableLiveData<Boolean>(false)
    val didSaveConfig: LiveData<Boolean> get() = _didSaveConfig
    val advanced = MutableLiveData<Boolean>(false)
    val saveEnabled = MutableLiveData<Boolean>(true)
    val _temperatureUnit = MutableLiveData<TemperatureUnit>(cfgData.temperatureUnit)
    val temperatureUnit: LiveData<TemperatureUnit> = _temperatureUnit

    val nextAction = MutableLiveData<NavigationFlow?>()

    fun setTemperatureUnit(unit: TemperatureUnit) {
        _temperatureUnit.value = unit
        cfgData.temperatureUnit = unit
    }


    fun onCreateAccount() {
        nextAction.value = NavigationFlow.CREATE_ACCOUNT
    }

    fun onSaveConfig() {
        saveEnabled.value = false
        if(cfgData.isDirty.value == true) {
            repository.storeCfg(cfgData)
            _didSaveConfig.value = true
        }
        nextAction.value = NavigationFlow.STATUS
    }
}
