package org.supla.android.features.measurementsdownload.workers
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
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.supla.android.data.source.local.entity.measurements.BaseLogEntity
import org.supla.android.data.source.remote.rest.channel.Measurement
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.blockingSubscribeBy
import org.supla.android.features.measurementsdownload.BaseDownloadLogUseCase
import org.supla.core.shared.extensions.guardLet
import timber.log.Timber

abstract class BaseDownloadLogWorker<T : Measurement, U : BaseLogEntity>(
  appContext: Context,
  workerParameters: WorkerParameters,
  private val downloadEventsManager: DownloadEventsManager,
  private val baseDownloadLogUseCase: BaseDownloadLogUseCase<T, U>
) : Worker(appContext, workerParameters) {

  protected val remoteId: Int?
    get() = inputData.getInt(REMOTE_ID_URI, -1).let {
      return@let if (it < 0) null else it
    }
  protected val profileId: Long?
    get() = inputData.getLong(PROFILE_ID_URI, -1).let {
      return@let if (it < 0) null else it
    }
  protected val dataType: DownloadEventsManager.DataType
    get() = inputData.getString(DATA_TYPE_ID_URI).let {
      it?.let { DownloadEventsManager.DataType.valueOf(it) } ?: DownloadEventsManager.DataType.DEFAULT_TYPE
    }

  override fun doWork(): Result {
    Timber.d("Worker started with ${baseDownloadLogUseCase.javaClass.simpleName}")

    val (remoteId) = guardLet(remoteId) {
      Timber.w("Download temperatures worker failed - remoteId < 0!")
      return Result.failure()
    }
    val (profileId) = guardLet(profileId) {
      Timber.w("Download temperatures worker failed - profileId < 0!")
      return Result.failure()
    }
    Timber.d("Worker parameters - remoteId: $remoteId, profileId: $profileId")

    var result = Result.failure()
    baseDownloadLogUseCase.loadMeasurements(remoteId, profileId)
      .doOnSubscribe {
        downloadEventsManager.emitProgressState(remoteId, dataType, DownloadEventsManager.State.Started)
      }
      .doOnNext {
        downloadEventsManager.emitProgressState(
          remoteId = remoteId,
          dataType = dataType,
          state = DownloadEventsManager.State.InProgress(it)
        )
      }
      .blockingSubscribeBy(
        onComplete = {
          downloadEventsManager.emitProgressState(remoteId, dataType, DownloadEventsManager.State.Finished)
          result = Result.success()
        },
        onError = {
          Timber.e(it)
          downloadEventsManager.emitProgressState(remoteId, dataType, DownloadEventsManager.State.Failed)
        }
      )

    return result
  }

  companion object {
    const val ITEMS_LIMIT_PER_REQUEST = 5000

    private val WORK_ID: String = BaseDownloadLogWorker::class.java.simpleName
    private val REMOTE_ID_URI = "$WORK_ID.REMOTE_ID"
    private val PROFILE_ID_URI = "$WORK_ID.PROFILE_ID"
    private val DATA_TYPE_ID_URI = "$WORK_ID.DATA_TYPE"

    val CONSTRAINTS = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    fun data(remoteId: Int, profileId: Long, dataType: DownloadEventsManager.DataType = DownloadEventsManager.DataType.DEFAULT_TYPE) =
      workDataOf(REMOTE_ID_URI to remoteId, PROFILE_ID_URI to profileId, DATA_TYPE_ID_URI to dataType.name)
  }
}
