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


import org.supla.android.Encryption
import org.supla.android.SuplaApp
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.DbHelper
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst
import kotlin.random.Random

class MultiAccountProfileManager(private val dbHelper: DbHelper,
                                 private val deviceId: String,
                                 private val repo: ProfileRepository,
                                 private val profileIdHolder: ProfileIdHolder) : ProfileManager {

    override fun getCurrentProfile(): AuthProfileItem {
        return repo.allProfiles.first { it.isActive }
    }

    override fun updateCurrentProfile(profile: AuthProfileItem) {
        val forceActivate: Boolean
        if(profile.id == ProfileIdNew) {
            profile.id = repo.createNamedProfile(profile.name)
            forceActivate = profile.isActive
        } else if(profile.isActive) {
            val prev = getProfile(profile.id)
            forceActivate = profile.isActive && !(prev?.isActive?:false)
        } else {
            forceActivate = false
        }

        repo.updateProfile(profile)
        dbHelper.deleteUserIcons()
        if(profile.isActive) {
            activateProfile(profile.id, forceActivate)
        }
    }

    override fun getCurrentAuthInfo(): AuthInfo {
        return getCurrentProfile().authInfo
    }

    override fun updateCurrentAuthInfo(info: AuthInfo) {
        val cp = getCurrentProfile()
        val np = cp.copy(authInfo = info)
        np.id = cp.id
        updateCurrentProfile(np)
    }

    override fun getAllProfiles(): List<AuthProfileItem> {
        return repo.allProfiles
    }

    override fun getProfile(id: Long): AuthProfileItem? {
        return if (id == ProfileIdNew) {
            val authInfo = AuthInfo(emailAuth=true,
                                    serverAutoDetect=true,
                                    guid=encrypted(Random.nextBytes(SuplaConst.SUPLA_GUID_SIZE)),
                                    authKey=encrypted(Random.nextBytes(SuplaConst.SUPLA_AUTHKEY_SIZE)))
            val rv = AuthProfileItem(authInfo=authInfo,
                                     advancedAuthSetup=false,
                                     isActive=if(repo.allProfiles.size > 0) false else true)
            rv.id = id
            rv
        } else {
            repo.getProfile(id)
        }
    }

    override fun activateProfile(id: Long, force: Boolean): Boolean {
        val current = getCurrentProfile()
        if((current.id == id && !force)) {
            return false
        }


        ImageCache.clear()
        DbHelper.getInstance(SuplaApp.getApp()).loadUserIconsIntoCache()
        initiateReconnect()
        if(repo.setProfileActive(id)) {
            profileIdHolder.profileId = id
        }
        return true
    }

    override fun removeProfile(id: Long) {
        repo.deleteProfile(id)
    }

    private fun initiateReconnect() {
        SuplaApp.getApp().suplaClient?.reconnect()
    }

    private fun encrypted(bytes: ByteArray): ByteArray {
        return Encryption.encryptDataWithNullOnException(bytes, deviceId)
    }
}
