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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.PROFILE_ID_NEW
import org.supla.android.profile.ProfileManager
import org.supla.android.widget.WidgetManager
import javax.inject.Inject

/**
A view model responsible for user credential input views. Handles both
initial authentication screen and profile editing.

@param profileManager profile manager to use for accessing account database
@param item account currently being edited
@param allowsBasicMode whether to allow basic mode (initial screen usually allows
       while profile editing mode does not.
*/
@HiltViewModel
class AuthItemViewModel @Inject constructor(
        private val profileManager: ProfileManager,
        private val widgetManager: WidgetManager
) : ViewModel() {
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
    private val _serverAutoDiscovery: MutableLiveData<Boolean> = MutableLiveData()
    val serverAutoDiscovery: LiveData<Boolean> = _serverAutoDiscovery

    val profileName: MutableLiveData<String> = MutableLiveData()

    private val _authByEmail: MutableLiveData<Boolean> = MutableLiveData()
    val authByEmail: LiveData<Boolean> = _authByEmail
    val emailAddress: MutableLiveData<String> = MutableLiveData()
    val accessID: MutableLiveData<String> = MutableLiveData()
    val serverAddrEmail: MutableLiveData<String> = MutableLiveData()
    val serverAddrAccessID: MutableLiveData<String> = MutableLiveData()
    val accessIDpwd: MutableLiveData<String> = MutableLiveData()
    val isAdvancedMode: MutableLiveData<Boolean> = MutableLiveData()
    val saveEnabled = MutableLiveData(true)
    val isActive: MutableLiveData<Boolean> = MutableLiveData()

    val isDeleteAvailable: Boolean  get() {
        return !item.isActive && (item.id != PROFILE_ID_NEW)
    }

    val allowsEditingProfileName: Boolean get() {
        return profileManager.getAllProfiles().size > 1 || item.authInfo.isAuthDataComplete || item.id == PROFILE_ID_NEW
    }


    private lateinit var _emailObserver: Observer<String>
    private lateinit var _serverAddrEmailObserver: Observer<String>
    private lateinit var _serverAddrAccessIDObserver: Observer<String>
    private lateinit var _accessIDObserver: Observer<String>
    private lateinit var _accessIDPwdObserver: Observer<String>
    private lateinit var _advancedObserver: Observer<Boolean>
    private lateinit var _activeObserver: Observer<Boolean>

    private lateinit var item: AuthProfileItem


    fun onCreated(profileId: Long) {
        item = profileManager.getProfile(profileId)!!
        val info = item.authInfo
        _authByEmail.value = info.emailAuth
        profileName.value = item.name

        val accessIDstr = if(info.accessID > 0) info.accessID.toString() else ""
        accessID.value = accessIDstr
        _accessIDObserver = Observer<String> {  if(it != accessIDstr) setNeedsReauth() }
        accessID.observeForever(_accessIDObserver)

        _serverAutoDiscovery.value = info.serverAutoDetect

        emailAddress.value = info.emailAddress
        _emailObserver = Observer { if(it != info.emailAddress) setNeedsReauth() }
        emailAddress.observeForever(_emailObserver)

        serverAddrEmail.value = info.serverForEmail
        _serverAddrEmailObserver = Observer { if(it != info.serverForEmail) setNeedsReauth() }
        serverAddrEmail.observeForever(_serverAddrEmailObserver)


        serverAddrAccessID.value = info.serverForAccessID
        _serverAddrAccessIDObserver = Observer { if(it != info.serverForAccessID) setNeedsReauth() }
        serverAddrAccessID.observeForever(_serverAddrAccessIDObserver)


        accessIDpwd.value = info.accessIDpwd
        _accessIDPwdObserver = Observer { if(it != info.accessIDpwd) setNeedsReauth() }
        accessIDpwd.observeForever(_accessIDPwdObserver)

        isAdvancedMode.value = item.advancedAuthSetup
        _advancedObserver = Observer {
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

        isActive.value = item.isActive
        _activeObserver = Observer {
            if(it == false && item.isActive) {
                _editAction.value = AuthItemEditAction.Alert(R.string.profile_set_as_default,
                                                             R.string.profile_activate_not_available)
                isActive.value = true
            }
        }
        isActive.observeForever(_activeObserver)
    }

    override fun onCleared() {
        emailAddress.removeObserver(_emailObserver)
        serverAddrEmail.removeObserver(_serverAddrEmailObserver)
        serverAddrAccessID.removeObserver(_serverAddrAccessIDObserver)
        accessID.removeObserver(_accessIDObserver)
        accessIDpwd.removeObserver(_accessIDPwdObserver)
        isAdvancedMode.removeObserver(_advancedObserver)
        isActive.removeObserver(_activeObserver)

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
        profile.isActive = isActive.value ?: false

        return if(profile.authInfo.isAuthDataComplete) {
            profileManager.updateCurrentProfile(profile)
            true
        } else {
            false
        }
    }

    private fun checkForDuplicateName(profile: String): Boolean {
        val match = profileManager.getAllProfiles().firstOrNull { it.name == profile }
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
            _editAction.value = AuthItemEditAction.ReturnFromAuth(_authSettingsChanged)
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
        _editAction.value = AuthItemEditAction.NavigateToCreateAccount
    }

    fun onDeleteProfile(force: Boolean = false) {
        when {
            item.isActive -> _editAction.value = AuthItemEditAction.Alert(
                    R.string.form_error,
                    R.string.form_cannot_delete_active_profile)
            !force -> _editAction.value = AuthItemEditAction.ConfirmDelete(
                    widgetManager.hasProfileWidgets(item.id))
            else -> {
                profileManager.removeProfile(item.id)
                _editAction.value = AuthItemEditAction.ReturnFromAuth(true)
            }
        }
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

    /**
     sets reauth flag, used to indicate that authentication
     settings are changed.
     */
    private fun setNeedsReauth() {
        _authSettingsChanged = true
    }

    val hasValidAccount : Boolean
        get() =  profileManager.getCurrentProfile().authInfo.isAuthDataComplete
}

sealed class AuthItemEditAction {
    data class Alert(val titleResId: Int, val messageResId: Int): AuthItemEditAction()
    data class ReturnFromAuth(val authSettingChanged: Boolean): AuthItemEditAction()
    data class ConfirmDelete(val hasWidgets: Boolean): AuthItemEditAction()
    object NavigateToCreateAccount: AuthItemEditAction()
}
