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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileManager
import org.supla.android.profile.ProfileIdNew

/**
A view model responsible for user credential input views. Handles both
initial authentication screen and profile editing.

@param profileManager profile manager to use for accessing account database
@param item account currently being edited
@param allowsBasicMode whether to allow basic mode (initial screen usually allows
       while profile editing mode does not.
*/
class AuthItemViewModel(private val profileManager: ProfileManager,
                        private val item: AuthProfileItem,
                        val allowsBasicMode: Boolean,
                        private val navCoordinator: NavCoordinator): ViewModel() {
    private val _isDirty = MutableLiveData<Boolean>(false)
    /**
     indicates that configuration is changed and should be saved.
     */
    val isDirty: LiveData<Boolean> = _isDirty


    private val _editAction: MutableStateFlow<AuthItemEditAction?> =
        MutableStateFlow(null)
    val editAction: StateFlow<AuthItemEditAction?> = _editAction

    
    /**
     indicates that auth settings are changed.
     */
    private var _authSettingsChanged = false

    /**
     server address auto discovery flag.
     */
    private val _serverAutoDiscovery: MutableLiveData<Boolean>
    val serverAutoDiscovery: LiveData<Boolean>

    val profileName: MutableLiveData<String>
    
    private val _authByEmail: MutableLiveData<Boolean>
    val authByEmail: LiveData<Boolean>
    val emailAddress: MutableLiveData<String>
    val accessID: MutableLiveData<String>
    val serverAddrEmail: MutableLiveData<String>
    val serverAddrAccessID: MutableLiveData<String>
    val accessIDpwd: MutableLiveData<String>
    val isAdvancedMode: MutableLiveData<Boolean>
    val saveEnabled = MutableLiveData<Boolean>(true)
    val isActive: MutableLiveData<Boolean>
    val isActiveVisible: Boolean get() {
        return (profileManager.getAllProfiles().size > 1) || (item.id == ProfileIdNew)
    }

    val isDeleteAvailable: Boolean  get() {
        return !item.isActive && (item.id != ProfileIdNew)
    }

    private val _emailObserver: Observer<String>
    private val _serverAddrEmailObserver: Observer<String>
    private val _serverAddrAccessIDObserver: Observer<String>
    private val _accessIDObserver: Observer<String>
    private val _accessIDPwdObserver: Observer<String>
    private val _advancedObserver: Observer<Boolean>



    init {
        
        val info = item.authInfo
        _authByEmail = MutableLiveData(info.emailAuth)
        authByEmail = _authByEmail

        profileName = MutableLiveData(item.name)

        val accessIDstr = if(info.accessID > 0) info.accessID.toString() else ""
        accessID = MutableLiveData<String>(accessIDstr)
        _accessIDObserver = Observer<String> {  if(it != accessIDstr) setNeedsReauth() }                                   
        accessID.observeForever(_accessIDObserver)

        _serverAutoDiscovery = MutableLiveData<Boolean>(info.serverAutoDetect)
        serverAutoDiscovery = _serverAutoDiscovery

        emailAddress = MutableLiveData(info.emailAddress)
        _emailObserver = Observer { if(it != info.emailAddress) setNeedsReauth() }
        emailAddress.observeForever(_emailObserver)

        serverAddrEmail = MutableLiveData(info.serverForEmail)
        _serverAddrEmailObserver = Observer { if(it != info.serverForEmail) setNeedsReauth() }
        serverAddrEmail.observeForever(_serverAddrEmailObserver)
       

        serverAddrAccessID = MutableLiveData(info.serverForAccessID)
        _serverAddrAccessIDObserver = Observer { if(it != info.serverForAccessID) setNeedsReauth() }
        serverAddrAccessID.observeForever(_serverAddrAccessIDObserver)


        accessIDpwd = MutableLiveData(info.accessIDpwd)
        _accessIDPwdObserver = Observer { if(it != info.accessIDpwd) setNeedsReauth() }
        accessIDpwd.observeForever(_accessIDPwdObserver)

        isAdvancedMode = MutableLiveData<Boolean>(if(allowsBasicMode) item.advancedAuthSetup else true)
        _advancedObserver = Observer {
            if(it != item.advancedAuthSetup) {
                setConfigDirty()
            }
            if(it == false) {
                // Do some sanity checks before going into
                // basic mode
                if(!((authByEmail.value == true) &&
                      (serverAutoDiscovery.value == true))) {
                    _editAction.value = AuthItemEditAction.Alert(R.string.basic_profile_warning,
                                                                R.string.basic_config_unavailable)
                    isAdvancedMode.value = true
                }
            }            
        }
        isAdvancedMode.observeForever(_advancedObserver)

        isActive = MutableLiveData<Boolean>(item.isActive)
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

    fun onProfileNameChange(s: CharSequence, start: Int, before: Int, count: Int) {
        profileName.value = s.toString()
    }


    fun selectEmailAuth(useEmailAuth: Boolean) {
        if(_authByEmail.value != useEmailAuth) {
	          _authByEmail.value = useEmailAuth
            setNeedsReauth()
        }
    }

    private fun saveConfig(): Boolean {
        val pm = profileManager
        val profile = item
        val pn = profileName.value

        if(pn != null) {
            profile.name = pn
        }

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

        if(profile.authInfo.isAuthDataComplete) {
            pm.updateCurrentProfile(profile)
            return true
        } else {
            return false
        }
    }

    private fun checkForDuplicateName(profile: String): Boolean {
        val match = profileManager.getAllProfiles().filter { it.name == profile }.firstOrNull()
        return (match != null && match.id != item.id)
    }

    fun onSaveConfig() {
        val pname = profileName.value
        if(pname.isNullOrBlank()) {
            _editAction.value = AuthItemEditAction.Alert(R.string.form_error,
                                                         R.string.form_profile_name_missing)
            return
        }

        if(checkForDuplicateName(pname)) {
            _editAction.value = AuthItemEditAction.Alert(R.string.form_error,
                                                         R.string.form_profile_duplicate)
            return
        }

        if(saveConfig()) {
            saveEnabled.value = false
            
            navCoordinator.returnFromAuth(_authSettingsChanged)
        } else {
            _editAction.value = AuthItemEditAction.Alert(R.string.form_error,
                                                         R.string.form_profile_required_data_missing)
        }
    }

    fun onEmailChange(s: CharSequence, start: Int, before: Int, count: Int) {
        if (_serverAutoDiscovery.value == true) {
            serverAddrEmail.value = ""
        }
    }

    fun onCreateAccount() {
        navCoordinator.navigate(NavigationFlow.CREATE_ACCOUNT)
    }

    fun onDeleteProfile() {
        // TODO: implementation missing
    }

    fun toggleServerAutoDiscovery() {
        if(_serverAutoDiscovery.value == true) {
            _serverAutoDiscovery.value = false

	          val email = emailAddress.value
	          if(serverAddrEmail.value == "" && email != null) {		
		            serverAddrEmail.value = email.substringAfter("@")
	          }
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
    }

    private fun setConfigDirty() {
        _isDirty.value = true
    }

    val hasValidAccount : Boolean
        get() =  profileManager.getCurrentProfile().authInfo.isAuthDataComplete
}

sealed class AuthItemEditAction {
    data class EditingCommited(val item: AuthProfileItem, val needsReauth: Boolean): 
        AuthItemEditAction()
    data class EditingCanceled(val item: AuthProfileItem): AuthItemEditAction()
    data class Alert(val titleResId: Int, val messageResId: Int): AuthItemEditAction()
}
