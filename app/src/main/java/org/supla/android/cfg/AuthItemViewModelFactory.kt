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

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import android.content.Context
import org.supla.android.db.DbHelper
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileManager
import org.supla.android.profile.MultiAccountProfileManager
import org.supla.android.profile.ProfileIdNew

import org.supla.android.data.source.local.LocalProfileRepository

class AuthItemViewModelFactory(private val ctx: Context,
                               private val profileId: Long,
                               private val allowBasicMode: Boolean,
                               private val navCoordinator: NavCoordinator):
    ViewModelProvider.Factory {
        
            
            
        override fun <T: ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(AuthItemViewModel::class.java)) {
                val repo = LocalProfileRepository(DbHelper.getInstance(ctx))
                val pm = MultiAccountProfileManager(ctx, repo)
                val profile = pm.getProfile(profileId)!!
                if(profileId == ProfileIdNew && allowBasicMode) {
                    profile.advancedAuthSetup = false
                }
                return AuthItemViewModel(pm, profile, allowBasicMode,
                                         navCoordinator) as T
            } else {
                throw IllegalArgumentException("unknown view model class")
            }
        }
}
