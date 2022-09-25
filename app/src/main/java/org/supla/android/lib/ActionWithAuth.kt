package org.supla.android.lib

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

import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import org.supla.android.data.source.ProfileRepository
import org.supla.android.profile.AuthInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class designed to making fast requests, without the need to change active profile. Used mainly in widget's workers.
 */
class ActionWithAuth private constructor(
        private val profileId: Long,
        private val profileRepository: ProfileRepository
) : SuplaNativeActions {

    @WorkerThread
    override fun open(subjectId: Int, subjectType: IdType): RequestResponse = call { open(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun close(subjectId: Int, subjectType: IdType): RequestResponse = call { close(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun shut(subjectId: Int, subjectType: IdType, percentage: Float): RequestResponse =
            call { shut(it, subjectId, subjectType.value, percentage) }

    @WorkerThread
    override fun reveal(subjectId: Int, subjectType: IdType): RequestResponse = call { reveal(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun turnOn(subjectId: Int, subjectType: IdType): RequestResponse = call { turnOn(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun turnOff(subjectId: Int, subjectType: IdType): RequestResponse = call { turnOff(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun setRgbw(
            subjectId: Int,
            subjectType: IdType,
            color: Int,
            colorBrightness: Int,
            brightness: Int,
            onOff: Boolean
    ): RequestResponse =
            call { setRgbw(it, subjectId, subjectType.value, color, colorBrightness, brightness, onOff) }

    @WorkerThread
    override fun stop(subjectId: Int, subjectType: IdType): RequestResponse = call { stop(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun toggle(subjectId: Int, subjectType: IdType): RequestResponse = call { toggle(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun upOrStop(subjectId: Int, subjectType: IdType): RequestResponse = call { upOrStop(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun downOrStop(subjectId: Int, subjectType: IdType): RequestResponse = call { downOrStop(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun stepByStep(subjectId: Int, subjectType: IdType): RequestResponse = call { stepByStep(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun execute(subjectId: Int, subjectType: IdType): RequestResponse = call { execute(it, subjectId, subjectType.value) }

    @WorkerThread
    override fun interrupt(subjectId: Int, subjectType: IdType): RequestResponse = call { interrupt(it, subjectId, subjectType.value) }

    private fun call(action: (AuthInfo) -> Int): RequestResponse {
        val profile = profileRepository.getProfile(profileId)
                ?: return RequestResponse.ERROR_UNKNOWN_PROFILE

        val result = action(profile.authInfo)
        return RequestResponse.values().first { it.code == result }
    }

    @VisibleForTesting
    external fun open(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun close(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun shut(authInfo: AuthInfo, subjectId: Int, subjectType: Int, percentage: Float): Int

    @VisibleForTesting
    external fun reveal(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun turnOn(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun turnOff(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun setRgbw(
            authInfo: AuthInfo,
            subjectId: Int,
            subjectType: Int,
            color: Int,
            colorBrightness: Int,
            brightness: Int,
            onOff: Boolean
    ): Int

    @VisibleForTesting
    external fun stop(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun toggle(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun upOrStop(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun downOrStop(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun stepByStep(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun execute(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @VisibleForTesting
    external fun interrupt(authInfo: AuthInfo, subjectId: Int, subjectType: Int): Int

    @Singleton
    class Provider @Inject constructor(private val profileRepository: ProfileRepository) {
        fun provide(profileId: Long): ActionWithAuth = ActionWithAuth(profileId, profileRepository)
    }
}

enum class RequestResponse(val code: Int) {
    SUCCESS(0),
    ERROR_UNKNOWN(1),
    ERROR_UNKNOWN_PROFILE(2),
    ERROR_NO_CONNECTION(3),
    ERROR_UNKNOWN_DEVICE(4),
    ERROR_LOGGED_OUT(5)
}
