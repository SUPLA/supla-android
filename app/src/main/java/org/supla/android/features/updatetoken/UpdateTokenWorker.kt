package org.supla.android.features.updatetoken

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

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.notifications.NotificationsHelper.Companion.areNotificationsEnabled
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaClient
import org.supla.android.lib.singlecall.SingleCall
import java.util.Date
import java.util.concurrent.TimeUnit

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000

@HiltWorker
class UpdateTokenWorker @AssistedInject constructor(
  private val singleCallProvider: SingleCall.Provider,
  private val profileRepository: RoomProfileRepository,
  private val encryptedPreferences: EncryptedPreferences,
  private val channelRepository: RoomChannelRepository,
  private val dateProvider: DateProvider,
  private val notificationManager: NotificationManager,
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {

  override fun doWork(): Result {
    Trace.d(TAG, "Token update worker started")

    val token = inputData.getString(TOKEN_URI)
    if (token == null) {
      Trace.w(TAG, "Token update worker canceled - token is null!")
      return Result.failure()
    }

    val allProfiles = profileRepository.findAllProfiles().blockingFirst()
    val notificationsEnabled = areNotificationsEnabled(notificationManager)
    val allProfilesUpdated = updateTokenInAllProfiles(token, allProfiles, notificationsEnabled)
    encryptedPreferences.notificationsLastEnabled = notificationsEnabled
    if (allProfilesUpdated) {
      // Worker updated all profiles, last update time can be saved
      encryptedPreferences.fcmTokenLastUpdate = Date()
    }

    Trace.i(TAG, "Token update worker finished (all profiles successfully updated: `$allProfilesUpdated`)")
    return Result.success()
  }

  private fun updateTokenInAllProfiles(
    token: String,
    allProfiles: List<ProfileEntity>,
    notificationsEnabled: Boolean
  ): Boolean {
    var allProfilesUpdated = true
    allProfiles.forEach { profile ->

      val previousEnabled = encryptedPreferences.notificationsLastEnabled
      val previousToken = encryptedPreferences.getFcmProfileToken(profile.id!!)

      if (token == previousToken && tokenUpdateNotNeeded() && notificationsEnabled == previousEnabled) {
        Trace.d(TAG, "Profile `${profile.name}` has active token set - skipping")
        return@forEach
      }
      if (profile.authInfo.emailAuth && profile.authInfo.serverForEmail.isEmpty()) {
        Trace.w(TAG, "Profile `${profile.name}` has server address not set - skipping")
        return@forEach
      }
      if (!profile.authInfo.emailAuth && profile.authInfo.serverForAccessID.isEmpty()) {
        Trace.w(TAG, "Profile `${profile.name}` has server address not set - skipping")
        return@forEach
      }
      if (channelRepository.findChannelsCount(profile.id).blockingGet() == 0) {
        Trace.w(TAG, "Profile `${profile.name}` has no channels - skipping")
        return@forEach
      }

      Trace.i(TAG, "Updating token for profile `${profile.name}` (id: `${profile.id}`)")
      try {
        val singleCall = singleCallProvider.provide(profile.id)
        singleCall.registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, token, profile)
        encryptedPreferences.setFcmProfileToken(profile.id, token)
      } catch (ex: Exception) {
        Trace.w(TAG, "Token update for profile `${profile.name}` (id: `${profile.id}`) failed!", ex)
        allProfilesUpdated = false
      }
    }

    return allProfilesUpdated
  }

  private fun tokenUpdateNotNeeded(): Boolean {
    return encryptedPreferences.fcmTokenLastUpdate?.let {
      val pauseTimeInMillis = UPDATE_PAUSE_IN_DAYS.times(ONE_DAY_MILLIS)
      it.time.plus(pauseTimeInMillis) > dateProvider.currentTimestamp()
    } ?: false
  }

  companion object {
    private const val TOKEN_URI = "UpdateTokenWorker.Token"

    private const val RETRY_TIME_IN_SEC = 30L
    const val UPDATE_PAUSE_IN_DAYS = 7
    val WORK_ID: String = UpdateTokenWorker::class.java.simpleName

    fun build(token: String): OneTimeWorkRequest {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
      val tokenData = workDataOf(TOKEN_URI to token)
      return OneTimeWorkRequestBuilder<UpdateTokenWorker>()
        .setInputData(tokenData)
        .setConstraints(constraints)
        .setBackoffCriteria(
          BackoffPolicy.LINEAR,
          RETRY_TIME_IN_SEC,
          TimeUnit.SECONDS
        )
        .build()
    }
  }
}
