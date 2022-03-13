package org.supla.android.profile
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


import android.content.Context
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.DbHelper
import org.supla.android.SuplaApp
import org.supla.android.data.source.ProfileRepository

class MultiAccountProfileManager(private val context: Context,
                                 private val repo: ProfileRepository): ProfileManager {
    

    override fun getCurrentProfile(): AuthProfileItem {
        val rv = repo.allProfiles.filter { it.isActive == true }.first()
        return rv
    }

    override fun updateCurrentProfile(profile: AuthProfileItem) {
        if(profile.id == ProfileIdNew) {
            profile.id = repo.createNamedProfile(profile.name)
        }
        repo.updateProfile(profile)
        DbHelper.getInstance(context).deleteUserIcons()
        if(profile.isActive) {
            activateProfile(profile.id)
        }
    } 

    override fun getCurrentAuthInfo(): AuthInfo {
        return getCurrentProfile().authInfo
    }

    override fun updateCurrentAuthInfo(info: AuthInfo) {
	      val cp = getCurrentProfile()
        var np = cp.copy(authInfo = info)
	      np.id = cp.id
        updateCurrentProfile(np)
    }

    override fun getAllProfiles(): List<AuthProfileItem> {
        return repo.allProfiles
    }

    override fun getProfile(id: Long): AuthProfileItem? {
        if(id == ProfileIdNew) {
            val authInfo = AuthInfo(emailAuth=true,
                                    serverAutoDetect=true)
            var rv = AuthProfileItem(authInfo=authInfo,
                                     advancedAuthSetup=true,
                                     isActive=false)
            rv.id = id
            return rv
        } else {
            return repo.getProfile(id)
        }
    }

    override fun activateProfile(id: Long): Boolean {
        val current = getCurrentProfile()
        if(current.id == id) return false
        if(repo.setProfileActive(id)) {
            initiateReconnect()
            return true
        } else {
            return false
        }
    }

    override fun removeProfile(id: Long) {
        repo.deleteProfile(id)
    }

    private fun initiateReconnect() {
        SuplaApp.getApp().getSuplaClient().reconnect()
    }
}
