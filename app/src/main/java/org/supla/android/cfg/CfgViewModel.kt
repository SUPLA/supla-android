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

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class CfgViewModel(private val repository: CfgRepository): ViewModel() {

    enum class NavigationFlow { CREATE_ACCOUNT, STATUS, MAIN }

    private val _didSaveConfig = MutableLiveData<Boolean>(false)
    val didSaveConfig: LiveData<Boolean> get() = _didSaveConfig
    val advanced = MutableLiveData<Boolean>(false)
    val saveEnabled = MutableLiveData<Boolean>(true)
    val temperatureUnit = MutableLiveData<TemperatureUnit>(TemperatureUnit.CELSIUS)
    val nextAction = MutableLiveData<NavigationFlow?>()
    val cfgData: CfgData = repository.getCfg()

    fun onCreateAccount() {
        android.util.Log.i("SUPLA", "will create account")
        nextAction.value = NavigationFlow.CREATE_ACCOUNT
    }

    fun onSaveConfig() {
        android.util.Log.i("SUPLA", "save config")
        saveEnabled.value = false
        if(cfgData.isDirty.value == true) {
            repository.storeCfg(cfgData)
            nextAction.value = NavigationFlow.STATUS
            _didSaveConfig.value = true
        }
    }
}
