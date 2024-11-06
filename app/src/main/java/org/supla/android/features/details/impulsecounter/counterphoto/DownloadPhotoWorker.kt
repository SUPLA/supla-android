package org.supla.android.features.details.impulsecounter.counterphoto
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
import androidx.hilt.work.HiltWorker
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
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.events.UpdateEventsManager
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import org.supla.core.shared.usecase.channel.StoreChannelOcrPhotoUseCase

@HiltWorker
class DownloadPhotoWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  private val suplaCloudServiceProvider: SuplaCloudService.Provider,
  private val storeChannelOcrPhotoUseCase: StoreChannelOcrPhotoUseCase,
  private val updateEventsManager: UpdateEventsManager
) : Worker(appContext, workerParameters) {

  private val remoteId: Int?
    get() = inputData.getInt(REMOTE_ID, -1).let {
      return@let if (it < 0) null else it
    }

  private val profileId: Long?
    get() = inputData.getLong(PROFILE_ID, -1).let {
      return@let if (it < 0) null else it
    }

  override fun doWork(): Result {
    val (remoteId) = guardLet(remoteId) {
      Trace.w(TAG, "OCR photo download not possible - missing remote id")
      return Result.failure()
    }

    val (profileId) = guardLet(profileId) {
      Trace.w(TAG, "OCR photo download not possible - missing profile id")
      return Result.failure()
    }

    return try {
      storeChannelOcrPhotoUseCase(
        remoteId = remoteId,
        profileId = profileId,
        photo = suplaCloudServiceProvider.provide().getImpulseCounterPhoto(remoteId).blockingFirst()
      )
      updateEventsManager.emitChannelUpdate(remoteId)
      Result.success()
    } catch (ex: Exception) {
      Trace.e(TAG, "Could not download OCR photo")
      Result.failure()
    }
  }

  companion object {
    val WORK_ID: String = DownloadPhotoWorker::class.java.simpleName

    private const val REMOTE_ID = "REMOTE_ID"
    private const val PROFILE_ID = "PROFILE_ID"

    fun build(remoteId: Int, profileId: Long): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<DownloadPhotoWorker>()
        .setInputData(workDataOf(REMOTE_ID to remoteId, PROFILE_ID to profileId))
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .build()
  }
}
