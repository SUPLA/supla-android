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
import androidx.lifecycle.StateFlow
import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.ViewModel
import org.supla.android.profile.ProfileManager

class ProfilesViewModel(private val profileManager: ProfileManager)
    : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilesUiState.ListProfiles(emptyList()))
    val uiState: StateFlow<ProfilesUiState> = _uiState
    val profilesAdapter = ProfilesAdapter(this)

    init {
        viewModelScope.launch {
            val profiles = profileManager.getAllProfiles()

            _uiState.value = ProfilesUiState.ListProfiles(profiles)
        }
    }

    fun onNewProfile() {

    }
}

sealed class ProfilesUiState {
    data class ListProfiles(profiles: List<Profile>): ProfilesUiState()
    data class NewProfile(): ProfilesUiState()
}

