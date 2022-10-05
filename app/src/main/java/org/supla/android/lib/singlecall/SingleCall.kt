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

import android.content.Context
import android.os.Looper
import android.os.NetworkOnMainThreadException
import androidx.annotation.WorkerThread
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.profile.AuthInfo
import org.supla.android.profile.NoSuchProfileException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class designed to making request, without the need to change active profile.
 * Each call creates a separate connection to the server that is closed upon receipt of the result.
 * Used mainly in widget's workers. Single calls are supported by the protocol version> = 19
 */

class SingleCall private constructor(
  var context: Context,
  var profileId: Long,
  var profileRepository: ProfileRepository
) {

  private external fun executeAction(
    context: Context,
    authInfo: AuthInfo,
    parameters: ActionParameters
  )

  private external fun getChannelValue(
    context: Context,
    authInfo: AuthInfo,
    channelId: Int
  ): ChannelValue

  @Throws(NoSuchProfileException::class)
  private fun getProfile(): AuthProfileItem {
    if (Thread.currentThread().equals(Looper.getMainLooper().thread)) {
      throw NetworkOnMainThreadException()
    }

    return profileRepository.getProfile(profileId)
      ?: throw NoSuchProfileException(profileId)
  }

  @WorkerThread
  @Throws(NoSuchProfileException::class, ResultException::class)
  fun executeAction(parameters: ActionParameters) {
    executeAction(context, getProfile().authInfo, parameters)
  }

  @WorkerThread
  @Throws(NoSuchProfileException::class, ResultException::class)
  fun getChannelValue(channelId: Int): ChannelValue {
    return getChannelValue(context, getProfile().authInfo, channelId)
  }

  @Singleton
  class Provider @Inject constructor(private val profileRepository: ProfileRepository) {
    fun provide(context: Context, profileId: Long) =
      SingleCall(context, profileId, profileRepository)
  }

  companion object {
    init {
      System.loadLibrary("suplaclient")
    }
  }
}
