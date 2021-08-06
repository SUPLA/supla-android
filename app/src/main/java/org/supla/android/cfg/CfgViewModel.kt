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
import androidx.lifecycle.Observer

class CfgViewModel(private val repository: CfgRepository): ViewModel() {

    enum class NavigationFlow { CREATE_ACCOUNT, STATUS, MAIN }
    val cfgData: CfgData = repository.getCfg()
    private val _isDirty = MutableLiveData<Boolean>(false)
    val isDirty: LiveData<Boolean> = _isDirty

    private val _didSaveConfig = MutableLiveData<Boolean>(false)
    val didSaveConfig: LiveData<Boolean> get() = _didSaveConfig
    val advanced = MutableLiveData<Boolean>(false)
    val saveEnabled = MutableLiveData<Boolean>(true)

    val nextAction = MutableLiveData<NavigationFlow?>()

    private val _emailObserver: Observer<String>
    private val _serverAddrObserver: Observer<String>
    private val _accessIDObserver: Observer<Int>
    private val _accessIDPwdObserver: Observer<String>
    private val _temperatureUnitObserver: Observer<TemperatureUnit>

    init {
        val email = cfgData.email.value
        _emailObserver = Observer<String> { if(it != email) _isDirty.value = true }
        val serverAddr = cfgData.serverAddr.value
        _serverAddrObserver = Observer<String> { if(it != serverAddr) _isDirty.value = true }
        val accessID = cfgData.accessID.value ?: 0
        _accessIDObserver = Observer<Int> { if(it != accessID) _isDirty.value = true }
        val accessIDPwd = cfgData.accessIDpwd.value
        _accessIDPwdObserver = Observer<String>  { if(it != accessIDPwd) _isDirty.value = true }
        val temperatureUnit = cfgData.temperatureUnit.value
        _temperatureUnitObserver = Observer<TemperatureUnit> { if(it != temperatureUnit) _isDirty.value = true }

        cfgData.email.observeForever(_emailObserver)
        cfgData.serverAddr.observeForever(_serverAddrObserver)
        cfgData.accessID.observeForever(_accessIDObserver)
        cfgData.accessIDpwd.observeForever(_accessIDPwdObserver)
        cfgData.temperatureUnit.observeForever(_temperatureUnitObserver)
    }

    override fun onCleared() {
        cfgData.email.removeObserver(_emailObserver)
        cfgData.serverAddr.removeObserver(_serverAddrObserver)
        cfgData.accessID.removeObserver(_accessIDObserver)
        cfgData.accessIDpwd.removeObserver(_accessIDPwdObserver)
        cfgData.temperatureUnit.removeObserver(_temperatureUnitObserver)

        super.onCleared()
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        cfgData.temperatureUnit.value = unit
    }


    fun onCreateAccount() {
        nextAction.value = NavigationFlow.CREATE_ACCOUNT
    }

    fun onSaveConfig() {
        saveEnabled.value = false
        if(isDirty.value == true) {
            repository.storeCfg(cfgData)
            _didSaveConfig.value = true
        }
        nextAction.value = NavigationFlow.STATUS
    }

    fun onEmailChange(s: CharSequence, start: Int, before: Int, count: Int) {
        cfgData.accessIDpwd.value = ""
        cfgData.serverAddr.value = ""
        cfgData.accessID.value = 0
    }
}
