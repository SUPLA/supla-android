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
import org.supla.android.data.source.ProfileRepository

class MultiAccountProfileManager(private val context: Context,
                                 private val repo: ProfileRepository): ProfileManager {
    

    override fun getCurrentProfile(): AuthProfileItem {
        return repo.allProfiles.filter { it.isActive == true }.first()
    }

    override fun updateCurrentProfile(profile: AuthProfileItem) {
        repo.updateProfile(profile)
        DbHelper.getInstance(context).deleteUserIcons()
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
}
