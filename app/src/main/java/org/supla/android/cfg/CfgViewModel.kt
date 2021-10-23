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
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

import org.supla.android.SuplaApp

class CfgViewModel(private val repository: CfgRepository): ViewModel() {

    enum class NavigationFlow { CREATE_ACCOUNT, STATUS, MAIN, 
                                BASIC_MODE_ALERT,
                                OPEN_PROFILES }
    val currentProfile = MutableLiveData<String>()
    val cfgData: CfgData = repository.getCfg()
    private val _isDirty = MutableLiveData<Boolean>(false)
    /**
     indicates that configuration is changed and should be saved.
     */
    val isDirty: LiveData<Boolean> = _isDirty

    val accessID : MutableLiveData<String>


    /**
     indicates that auth settings are changed.
     */
    private var _authSettingsChanged = false

    /**
     server address auto discovery flag.
     */
    private val _serverAutoDiscovery: MutableLiveData<Boolean>
    val serverAutoDiscovery: LiveData<Boolean>
    
    private val _authByEmail: MutableLiveData<Boolean>
    val authByEmail: LiveData<Boolean>
    val emailAddress: MutableLiveData<String>
    val serverAddrEmail: MutableLiveData<String>
    val serverAddrAccessID: MutableLiveData<String>
    val accessIDpwd: MutableLiveData<String>


    private val _didSaveConfig = MutableLiveData<Boolean>(false)
    val didSaveConfig: LiveData<Boolean> get() = _didSaveConfig
    val saveEnabled = MutableLiveData<Boolean>(true)

    val isAdvancedMode: MutableLiveData<Boolean>

    val nextAction = MutableLiveData<NavigationFlow?>()

    private val _emailObserver: Observer<String>
    private val _serverAddrEmailObserver: Observer<String>
    private val _serverAddrAccessIDObserver: Observer<String>
    private val _accessIDObserver: Observer<String>
    private val _accessIDPwdObserver: Observer<String>
    private val _advancedObserver: Observer<Boolean>


    init {
        val app = SuplaApp.getApp()
        val pm = app.getProfileManager(app)
        val profile = pm.getCurrentProfile()
        val authInfo = profile.authInfo

        _authByEmail = MutableLiveData(authInfo.emailAuth)
        authByEmail = _authByEmail

        val accessIDstr = if(authInfo.accessID > 0) authInfo.accessID.toString() else ""
        accessID = MutableLiveData<String>(accessIDstr)
        _accessIDObserver = Observer<String> {  if(it != accessIDstr) setNeedsReauth() }                                   
        accessID.observeForever(_accessIDObserver)

        _serverAutoDiscovery = MutableLiveData<Boolean>(authInfo.serverAutoDetect)
        serverAutoDiscovery = _serverAutoDiscovery

        emailAddress = MutableLiveData(authInfo.emailAddress)
        _emailObserver = Observer { if(it != authInfo.emailAddress) setNeedsReauth() }
        emailAddress.observeForever(_emailObserver)

        serverAddrEmail = MutableLiveData(authInfo.serverForEmail)
        _serverAddrEmailObserver = Observer { if(it != authInfo.serverForEmail) setNeedsReauth() }
        serverAddrEmail.observeForever(_serverAddrEmailObserver)

        serverAddrAccessID = MutableLiveData(authInfo.serverForAccessID)
        _serverAddrAccessIDObserver = Observer { if(it != authInfo.serverForAccessID) setNeedsReauth() }
        serverAddrAccessID.observeForever(_serverAddrAccessIDObserver)


        accessIDpwd = MutableLiveData(authInfo.accessIDpwd)
        _accessIDPwdObserver = Observer { if(it != authInfo.accessIDpwd) setNeedsReauth() }
        accessIDpwd.observeForever(_accessIDPwdObserver)

        isAdvancedMode = MutableLiveData<Boolean>(profile.advancedAuthSetup)
        _advancedObserver = Observer {
            if(it != profile.advancedAuthSetup) {
                setConfigDirty()
            }
            if(it == false) {
                // Do some sanity checks before going into
                // basic mode
                if(!((authByEmail.value == true) &&
                      (serverAutoDiscovery.value == true))) {
                    nextAction.value = NavigationFlow.BASIC_MODE_ALERT
                    isAdvancedMode.value = true
                }
            }            
        }
        isAdvancedMode.observeForever(_advancedObserver)

    }

    override fun onCleared() {
        emailAddress.removeObserver(_emailObserver)
        serverAddrEmail.removeObserver(_serverAddrEmailObserver)
        serverAddrAccessID.removeObserver(_serverAddrAccessIDObserver)
        accessID.removeObserver(_accessIDObserver)
        accessIDpwd.removeObserver(_accessIDPwdObserver)
        isAdvancedMode.removeObserver(_advancedObserver)

        super.onCleared()
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        if(cfgData.temperatureUnit.value != unit) {
            cfgData.temperatureUnit.value = unit
            setConfigDirty()
        }
    }

    fun selectEmailAuth(useEmailAuth: Boolean) {
        if(_authByEmail.value != useEmailAuth) {
	          _authByEmail.value = useEmailAuth
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

    fun setShowChannelInfo(show: Boolean) {
        if(cfgData.showChannelInfo.value != show) {
            cfgData.showChannelInfo.value = show
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

        if(_authSettingsChanged) {
            val app = SuplaApp.getApp()
            val pm = app.getProfileManager(app)
            val profile = pm.getCurrentProfile()

            profile.authInfo.emailAuth = _authByEmail.value!!
            try {
                profile.authInfo.accessID = accessID.value?.toInt() ?: 0
            } catch(_: NumberFormatException) {
                profile.authInfo.accessID = 0
            }
            profile.authInfo.serverAutoDetect = serverAutoDiscovery.value ?: true
            profile.authInfo.emailAddress = emailAddress.value ?: ""
            profile.authInfo.serverForEmail = serverAddrEmail.value ?: ""
            profile.authInfo.serverForAccessID = serverAddrAccessID.value ?: ""
            profile.authInfo.accessIDpwd = accessIDpwd.value ?: ""
            profile.advancedAuthSetup = isAdvancedMode.value ?: false
            
            pm.updateCurrentProfile(profile)
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
        if (_serverAutoDiscovery.value == true) {
            serverAddrEmail.value = ""
        }
    }

    fun toggleServerAutoDiscovery() {
        if(_serverAutoDiscovery.value == true) {
            _serverAutoDiscovery.value = false
        } else {
            serverAddrEmail.value = ""
            _serverAutoDiscovery.value = true
        }
        setNeedsReauth()
    }

    private fun clearEmail() {
        emailAddress.value = ""
    }

    private fun clearAccessID() {
        accessIDpwd.value = ""
        accessID.value = ""
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
