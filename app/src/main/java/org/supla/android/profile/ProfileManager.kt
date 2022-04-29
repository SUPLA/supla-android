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

import org.supla.android.db.AuthProfileItem

const val ProfileIdNew: Long = -1

/**
 * Additional holder class is needed because of circular dependency between
 * MultiAccountProfileManager and DbHelper
 */
data class ProfileIdHolder(var profileId: Long?)

interface ProfileManager {

    /**
     * Returns current profile.
     */
    fun getCurrentProfile(): AuthProfileItem

    /**
     * Updates current profile.
     */
    fun updateCurrentProfile(profile: AuthProfileItem)

    /**
     * Get authentication settings of current profile.
     */
    fun getCurrentAuthInfo(): AuthInfo

    /**
     * Update authentication settings of current profile.
     */
    fun updateCurrentAuthInfo(info: AuthInfo)


    /**
     * Return list of all user profiles
     */
    fun getAllProfiles(): List<AuthProfileItem>

    /**
     * Return profile with given id
     */
    fun getProfile(id: Long): AuthProfileItem?

    /**
     * Activate profile with given id
     * returns true if profile has been changed (i.e. reauthentication
     * is required).
     */
    fun activateProfile(id: Long, force: Boolean): Boolean

    /**
     * Remove profile
     */
    fun removeProfile(id: Long)
}
