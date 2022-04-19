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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.supla.android.profile.ProfileManager
import org.supla.android.profile.ProfileIdNew
import org.supla.android.db.AuthProfileItem

class ProfilesViewModel(private val profileManager: ProfileManager)
    : ViewModel(), EditableProfileItemViewModel.EditActionHandler {

    private val _uiState: MutableLiveData<ProfilesUiState> = 
        MutableLiveData(ProfilesUiState.ListProfiles(emptyList()))
    val uiState: LiveData<ProfilesUiState> = _uiState
    val profilesAdapter = ProfilesAdapter(this)

    init {
        reload()
    }

    fun onNewProfile() {
        _uiState.value = ProfilesUiState.EditProfile(ProfileIdNew)
    }

    override fun onEditProfile(profileId: Long) {
        _uiState.value = ProfilesUiState.EditProfile(profileId)
    }

    fun onActivateProfile(profileId: Long) {
        if(profileManager.activateProfile(profileId)) {
            _uiState.value = ProfilesUiState.ProfileActivation(profileId)
        }
    }

    fun reload() {
        val profiles = profileManager.getAllProfiles()
        _uiState.value = ProfilesUiState.ListProfiles(profiles)
    }
}

sealed class ProfilesUiState {
    data class ListProfiles(val profiles: List<AuthProfileItem>): ProfilesUiState()
    data class EditProfile(val profileId: Long): ProfilesUiState()
    data class ProfileActivation(val profileId: Long): ProfilesUiState()
}

