package org.supla.android.lib.singlecall
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

import android.os.Looper
import android.os.NetworkOnMainThreadException
import androidx.annotation.WorkerThread
import org.supla.android.data.source.ProfileRepository
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.profile.AuthInfo
import org.supla.android.profile.NoSuchProfileException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class designed to making request, without the need to change active profile.
 * Each call creates a separate connection to the server that is closed upon receipt of the result.
 * Used mainly in widget's workers.
 */

class SingleCall private constructor(
    var profileId: Long,
    var profileRepository: ProfileRepository) {
    private external fun executeAction(authInfo: AuthInfo, parameters: ActionParameters)

    @Throws(NoSuchProfileException::class, ConnectionException::class, ResultException::class)
    @WorkerThread
    fun executeAction(parameters: ActionParameters) {
        if (Looper.getMainLooper().isCurrentThread) {
            throw NetworkOnMainThreadException()
        }

        val profile = profileRepository.getProfile(profileId)
            ?: throw NoSuchProfileException(profileId)

        executeAction(profile.authInfo, parameters)
    }

    @Singleton
    class Provider @Inject constructor(private val profileRepository: ProfileRepository) {
        fun provide(profileId: Long) : SingleCall = SingleCall(profileId, profileRepository)
    }

    companion object {
        init {
            System.loadLibrary("suplaclient")
        }
    }
}