package org.supla.android.features.temperaturesdownload
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
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.reactivex.rxjava3.kotlin.blockingSubscribeBy
import org.supla.android.Trace
import org.supla.android.data.source.BaseMeasurementRepository
import org.supla.android.data.source.remote.rest.channel.Measurement
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet

abstract class BaseDownloadLogWorker<T : Measurement, U> constructor(
  appContext: Context,
  workerParameters: WorkerParameters,
  private val downloadEventsManager: DownloadEventsManager,
  private val baseMeasurementRepository: BaseMeasurementRepository<T, U>
) : Worker(appContext, workerParameters) {

  protected val remoteId: Int?
    get() = inputData.getInt(REMOTE_ID_URI, -1).let {
      return@let if (it < 0) null else it
    }
  protected val profileId: Long?
    get() = inputData.getLong(PROFILE_ID_URI, -1).let {
      return@let if (it < 0) null else it
    }

  override fun doWork(): Result {
    Log.d(TAG, "Worker started with ${baseMeasurementRepository.javaClass.simpleName}")

    val (remoteId) = guardLet(remoteId) {
      Trace.w(TAG, "Download temperatures worker failed - remoteId < 0!")
      return Result.failure()
    }
    val (profileId) = guardLet(profileId) {
      Trace.w(TAG, "Download temperatures worker failed - profileId < 0!")
      return Result.failure()
    }
    Log.d(TAG, "Worker parameters - remoteId: $remoteId, profileId: $profileId")

    var result = Result.failure()
    baseMeasurementRepository.loadMeasurements(remoteId, profileId)
      .doOnSubscribe {
        downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Started)
      }
      .doOnNext {
        downloadEventsManager.emitProgressState(
          remoteId = remoteId,
          state = DownloadEventsManager.State.InProgress(it)
        )
      }
      .blockingSubscribeBy(
        onComplete = {
          downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Finished)
          result = Result.success()
        },
        onError = {
          Trace.e(TAG, it.message, it)
          downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Failed)
        }
      )

    return result
  }

  companion object {
    const val ITEMS_LIMIT_PER_REQUEST = 5000

    private val WORK_ID: String = BaseDownloadLogWorker::class.java.simpleName
    private val REMOTE_ID_URI = "$WORK_ID.REMOTE_ID"
    private val PROFILE_ID_URI = "$WORK_ID.PROFILE_ID"

    val CONSTRAINTS = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    fun data(remoteId: Int, profileId: Long) =
      workDataOf(REMOTE_ID_URI to remoteId, PROFILE_ID_URI to profileId)
  }
}
