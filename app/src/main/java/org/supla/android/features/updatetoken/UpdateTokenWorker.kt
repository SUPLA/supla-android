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

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.hilt.android.EntryPointAccessors.fromApplication
import org.supla.android.Trace
import org.supla.android.di.entrypoints.EncryptedPreferencesEntryPoint
import org.supla.android.di.entrypoints.ProfileManagerEntryPoint
import org.supla.android.di.entrypoints.SingleCallProviderEntryPoint
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaClient
import java.util.*
import java.util.concurrent.TimeUnit

class UpdateTokenWorker(appContext: Context, workerParameters: WorkerParameters) : Worker(appContext, workerParameters) {

  private val profileManager = fromApplication(appContext, ProfileManagerEntryPoint::class.java).provideProfileManager()
  private val singleCallProvider = fromApplication(appContext, SingleCallProviderEntryPoint::class.java).provideSingleCallProvider()
  private val encryptedPreferences = fromApplication(appContext, EncryptedPreferencesEntryPoint::class.java).provideEncryptedPreferences()

  override fun doWork(): Result {
    Trace.d(TAG, "Token update worker started")

    val token = inputData.getString(TOKEN_URI)
    if (token == null) {
      Trace.w(TAG, "Token update worker canceled - token is null!")
      return Result.failure()
    }
    val updateSelf = inputData.getBoolean(UPDATE_SELF_URI, true)

    val currentProfile = profileManager.getCurrentProfile().blockingGet()
    if (currentProfile == null) {
      Trace.i(TAG, "Token update worker canceled - no profile found")
      return Result.failure()
    }

    var allProfilesUpdated = true
    profileManager.getAllProfiles().blockingFirst().forEach { profile ->
      if (profile.id == currentProfile.id && updateSelf.not()) {
        Trace.d(TAG, "Active profile skipped because of updateSelf: `$updateSelf`")
        return@forEach
      }

      Trace.i(TAG, "Updating token for profile `${profile.name}` (id: `${profile.id}`)")
      try {
        val singleCall = singleCallProvider.provide(profile.id)
        singleCall.registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, token)
      } catch (ex: Exception) {
        Trace.w(TAG, "Token update for profile `${profile.name}` (id: `${profile.id}`) failed!", ex)
        allProfilesUpdated = false
      }
    }

    if (allProfilesUpdated) {
      // Worker updated all profiles, last update time can be saved
      encryptedPreferences.fcmTokenLastUpdate = Date()
    }

    Trace.d(TAG, "Token update worker finished (all profiles successfully updated: `$allProfilesUpdated`)")
    return Result.success()
  }

  companion object {
    private const val TOKEN_URI = "UpdateTokenWorker.Token"
    private const val UPDATE_SELF_URI = "UpdateTokenWorker.UpdateSelf"

    private const val RETRY_TIME_IN_SEC = 30L
    const val UPDATE_PAUSE_IN_DAYS = 7

    fun build(token: String, updateSelf: Boolean): OneTimeWorkRequest {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
      val tokenData = workDataOf(
        TOKEN_URI to token,
        UPDATE_SELF_URI to updateSelf
      )
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
