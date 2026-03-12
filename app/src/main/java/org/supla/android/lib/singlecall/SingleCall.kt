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
import androidx.room.rxjava3.EmptyResultSetException
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.db.AuthProfileItem
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_DISABLED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_INACTIVE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_DISABLED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_CLIENT_NOT_EXISTS
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_SUBJECT_NOT_FOUND
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_CANT_CONNECT_TO_HOST
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND
import org.supla.android.lib.SuplaConst.SUPLA_RESULT_RESPONSE_TIMEOUT
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
  var profileRepository: RoomProfileRepository
) {

  private external fun executeAction(
    context: Context,
    authInfo: AuthInfo,
    parameters: ActionParameters,
    connectionTimeoutMs: Int
  )

  private external fun getChannelValue(
    context: Context,
    authInfo: AuthInfo,
    channelId: Int,
    connectionTimeoutMs: Int
  ): ChannelValue

  private external fun registerPushNotificationClientToken(
    context: Context,
    authInfo: AuthInfo,
    appId: Int,
    token: String,
    profileName: String,
    connectionTimeoutMs: Int
  )

  @Throws(NoSuchProfileException::class)
  private fun getProfile(): ProfileEntity {
    if (Thread.currentThread().equals(Looper.getMainLooper().thread)) {
      throw NetworkOnMainThreadException()
    }

    return try {
      profileRepository.findProfile(profileId).blockingGet()
    } catch (ex: EmptyResultSetException) {
      throw NoSuchProfileException(profileId)
    }
  }

  @WorkerThread
  fun executeAction(parameters: ActionParameters): Result =
    try {
      executeAction(context, getProfile().authInfo, parameters, CONNECTION_NO_TIMEOUT)
      Result.Success
    } catch (ex: ResultException) {
      ex.toResult
    } catch (_: NoSuchProfileException) {
      Result.NoSuchProfile
    } catch (_: Exception) {
      Result.UnknownError
    }

  @WorkerThread
  @Throws(NoSuchProfileException::class, ResultException::class)
  fun getChannelValue(channelId: Int): ChannelValue {
    return getChannelValue(context, getProfile().authInfo, channelId, CONNECTION_NO_TIMEOUT)
  }

  @WorkerThread
  @Throws(NoSuchProfileException::class, ResultException::class)
  fun registerPushNotificationClientToken(appId: Int, token: String, profile: ProfileEntity) {
    registerPushNotificationClientToken(context, profile.authInfo, appId, token, profile.name, CONNECTION_NO_TIMEOUT)
  }

  @WorkerThread
  @Throws(NoSuchProfileException::class, ResultException::class)
  fun registerPushNotificationClientToken(appId: Int, token: String, profile: AuthProfileItem) {
    registerPushNotificationClientToken(context, profile.authInfo, appId, token, profile.name, CONNECTION_NO_TIMEOUT)
  }

  @Singleton
  class Provider @Inject constructor(
    private val profileRepository: RoomProfileRepository,
    @param:ApplicationContext private val context: Context
  ) {
    fun provide(profileId: Long) = SingleCall(context, profileId, profileRepository)
  }

  sealed interface Result {
    data object Success : Result
    data object Offline : Result
    data object NotFound : Result
    data object NoSuchProfile : Result
    data object UnknownError : Result
    data object Inactive : Result

    data class CommandError(val code: Int) : Result
    data class ConnectionError(val code: Int) : Result
    data class AccessError(val code: Int) : Result
  }

  companion object {
    init {
      System.loadLibrary("suplaclient")
    }

    const val CONNECTION_NO_TIMEOUT: Int = 0
  }
}

val ResultException.toResult: SingleCall.Result
  get() = when (result) {
    SUPLA_RESULT_HOST_NOT_FOUND,
    SUPLA_RESULT_CANT_CONNECT_TO_HOST,
    SUPLA_RESULT_RESPONSE_TIMEOUT -> SingleCall.Result.ConnectionError(result)

    SUPLA_RESULTCODE_CLIENT_NOT_EXISTS,
    SUPLA_RESULTCODE_BAD_CREDENTIALS,
    SUPLA_RESULTCODE_CLIENT_DISABLED,
    SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED,
    SUPLA_RESULTCODE_ACCESSID_DISABLED -> SingleCall.Result.AccessError(result)

    SUPLA_RESULTCODE_ACCESSID_INACTIVE -> SingleCall.Result.Inactive

    SUPLA_RESULTCODE_CHANNEL_IS_OFFLINE -> SingleCall.Result.Offline
    SUPLA_RESULTCODE_SUBJECT_NOT_FOUND -> SingleCall.Result.NotFound
    else -> SingleCall.Result.CommandError(result)
  }
