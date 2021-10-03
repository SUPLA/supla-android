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

    enum class NavigationFlow { CREATE_ACCOUNT, STATUS, MAIN, 
                                OPEN_PROFILES }
    val currentProfile = MutableLiveData<String>()
    val cfgData: CfgData = repository.getCfg()
    private val _isDirty = MutableLiveData<Boolean>(false)
    /**
     indicates that configuration is changed and should be saved.
     */
    val isDirty: LiveData<Boolean> = _isDirty

    /**
     indicates that auth settings are changed.
     */
    private var _authSettingsChanged = false

    /**
     server address auto discovery flag.
     */
    private val _serverAutoDiscovery = MutableLiveData<Boolean>()
    val serverAutoDiscovery: LiveData<Boolean> = _serverAutoDiscovery

    private val _didSaveConfig = MutableLiveData<Boolean>(false)
    val didSaveConfig: LiveData<Boolean> get() = _didSaveConfig
    val saveEnabled = MutableLiveData<Boolean>(true)

    val nextAction = MutableLiveData<NavigationFlow?>()

    private val _emailObserver: Observer<String>
    private val _serverAddrObserver: Observer<String>
    private val _accessIDObserver: Observer<Int>
    private val _accessIDPwdObserver: Observer<String>
    private val _advancedObserver: Observer<Boolean>


    init {
        val email = cfgData.email.value
        _emailObserver = Observer { if(it != email) setNeedsReauth() }
        val serverAddr = cfgData.serverAddr.value
        _serverAddrObserver = Observer<String> { if(it != serverAddr) setNeedsReauth() }
        val accessID = cfgData.accessID.value ?: 0
        _accessIDObserver = Observer<Int> { if(it != accessID) setNeedsReauth() }
        val accessIDPwd = cfgData.accessIDpwd.value
        _accessIDPwdObserver = Observer<String>  { if(it != accessIDPwd) setNeedsReauth() }

        val isAdvanced = cfgData.isAdvanced.value
        _advancedObserver = Observer { if(it != isAdvanced) setConfigDirty() }

        cfgData.email.observeForever(_emailObserver)
        cfgData.serverAddr.observeForever(_serverAddrObserver)
        cfgData.accessID.observeForever(_accessIDObserver)
        cfgData.accessIDpwd.observeForever(_accessIDPwdObserver)
        cfgData.isAdvanced.observeForever(_advancedObserver)

        _serverAutoDiscovery.value = !(email?.isEmpty() ?: true) && (serverAddr?.isEmpty() ?: true)
    }

    override fun onCleared() {
        cfgData.email.removeObserver(_emailObserver)
        cfgData.serverAddr.removeObserver(_serverAddrObserver)
        cfgData.accessID.removeObserver(_accessIDObserver)
        cfgData.accessIDpwd.removeObserver(_accessIDPwdObserver)
        cfgData.isAdvanced.removeObserver(_advancedObserver)

        super.onCleared()
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        if(cfgData.temperatureUnit.value != unit) {
            cfgData.temperatureUnit.value = unit
            setConfigDirty()
        }
    }

    fun selectEmailAuth(useEmailAuth: Boolean) {
        if(cfgData.authByEmail.value != useEmailAuth) {
	          cfgData.authByEmail.value = useEmailAuth
            setNeedsReauth()
        }
    }

    fun setButtonAutohide(autohideEnabled: Boolean) {
        if(cfgData.buttonAutohide.value != autohideEnabled) {
            cfgData.buttonAutohide.value = autohideEnabled
            setConfigDirty()
        }
    }

    fun setChannelHeight(height: ChannelHeight) {
        if(cfgData.channelHeight.value != height) {
            cfgData.channelHeight.value = height
            setConfigDirty()
        }
    }


    fun onCreateAccount() {
        nextAction.value = NavigationFlow.CREATE_ACCOUNT
    }

    fun openProfiles() {
        nextAction.value = NavigationFlow.OPEN_PROFILES
    }

    fun saveConfig() {
        if(isDirty.value == true) {
            repository.storeCfg(cfgData)
            _didSaveConfig.value = true
        }
    }

    fun onSaveConfig() {
        saveEnabled.value = false
        saveConfig()
        if(_authSettingsChanged == true) {
            nextAction.value = NavigationFlow.STATUS
        } else {
            nextAction.value = NavigationFlow.MAIN
        }
    }

    fun onEmailChange(s: CharSequence, start: Int, before: Int, count: Int) {
        cfgData.accessIDpwd.value = ""
        cfgData.serverAddr.value = ""
        cfgData.accessID.value = 0
    }

    fun toggleServerAutoDiscovery() {
        if(_serverAutoDiscovery.value == true) {
            _serverAutoDiscovery.value = false
        } else {
            cfgData.serverAddr.value = ""
            _serverAutoDiscovery.value = true
        }
    }

    /**
     sets reauth flag, used to indicate that authentication
     settings are changed.
     */
    private fun setNeedsReauth() {
        _authSettingsChanged = true
        setConfigDirty()
    }

    /**
     sets config dirty flag, to indicate that configuration has
     been updated.
     */
    private fun setConfigDirty() {
        _isDirty.value = true
    }
}
