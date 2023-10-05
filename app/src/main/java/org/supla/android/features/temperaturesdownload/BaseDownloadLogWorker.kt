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
import org.supla.android.Trace
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.Measurement
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifLet
import retrofit2.Response

private const val ALLOWED_TIME_DIFFERENCE = 1800

abstract class BaseDownloadLogWorker<T : Measurement, U> constructor(
  appContext: Context,
  workerParameters: WorkerParameters,
  private val suplaCloudServiceProvider: SuplaCloudService.Provider,
  private val downloadEventsManager: DownloadEventsManager
) : Worker(appContext, workerParameters) {

  protected val remoteId: Int?
    get() = inputData.getInt(REMOTE_ID_URI, -1).let {
      return@let if (it < 0) null else it
    }
  protected val profileId: Long?
    get() = inputData.getLong(PROFILE_ID_URI, -1).let {
      return@let if (it < 0) null else it
    }

  protected val cloudService by lazy {
    suplaCloudServiceProvider.provide()
  }

  override fun doWork(): Result {
    Log.e(TAG, "Worker started")

    val (remoteId) = guardLet(remoteId) {
      Trace.w(TAG, "Download temperatures worker failed - remoteId < 0!")
      return Result.failure()
    }
    val (profileId) = guardLet(profileId) {
      Trace.w(TAG, "Download temperatures worker failed - profileId < 0!")
      return Result.failure()
    }

    downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.InProgress(0f))

    // Load initial measurements
    val (initialMeasurementsPair) = guardLet(loadInitialMeasurements(remoteId)) {
      Trace.e(TAG, "Could not load initial measurements")
      downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Failed)
      return Result.failure()
    }

    val firstMeasurements = initialMeasurementsPair.first
    val totalCount = initialMeasurementsPair.second
    Trace.d(TAG, "Found initial remote entries (count: ${firstMeasurements.size}, total count: $totalCount)")

    // Check cleanup needed
    val (cleanMeasurements) = guardLet(checkCleanNeeded(firstMeasurements, remoteId, profileId)) {
      Trace.e(TAG, "Could not verify if clean needed")
      downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Failed)
      return Result.failure()
    }

    // Perform measurements import
    return try {
      performImport(totalCount, cleanMeasurements, remoteId, profileId).also {
        downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Finished)
      }
    } catch (ex: Exception) {
      Trace.e(TAG, "Measurements import failed", ex)
      downloadEventsManager.emitProgressState(remoteId, DownloadEventsManager.State.Failed)
      Result.failure()
    }
  }

  protected abstract fun getInitialMeasurements(remoteId: Int): Response<List<T>>

  protected abstract fun getMeasurements(remoteId: Int, afterTimestamp: Long): List<T>

  protected abstract fun getMinTimestamp(remoteId: Int, profileId: Long): Long?

  protected abstract fun getMaxTimestamp(remoteId: Int, profileId: Long): Long?

  protected abstract fun cleanMeasurements(remoteId: Int, profileId: Long)

  protected abstract fun getLocalMeasurementsCount(remoteId: Int, profileId: Long): Int

  protected abstract fun insert(entries: List<U>)

  protected abstract fun map(entry: T, remoteId: Int, profileId: Long): U

  private fun loadInitialMeasurements(remoteId: Int): Pair<List<T>, Int>? {
    try {
      val firstMeasurementsResponse = getInitialMeasurements(remoteId)
      if (firstMeasurementsResponse.code() != 200) {
        Trace.e(TAG, "Initial measurements load failed: ${firstMeasurementsResponse.message()}")
        return null
      }
      val firstMeasurements = firstMeasurementsResponse.body()
      val totalCount = firstMeasurementsResponse.headers()["X-Total-Count"]?.toInt()

      if (firstMeasurements != null && totalCount != null) {
        return Pair(firstMeasurements, totalCount)
      }
    } catch (ex: Exception) {
      Trace.e(TAG, "Initial measurements load failed", ex)
    }

    return null
  }

  private fun checkCleanNeeded(firstMeasurements: List<T>, remoteId: Int, profileId: Long): Boolean? {
    try {
      if (firstMeasurements.isEmpty()) {
        Trace.d(TAG, "No entries - cleaning measurements")
        return true
      } else {
        ifLet(getMinTimestamp(remoteId, profileId)) { (minTimestamp) ->
          Trace.d(TAG, "Found local minimal timestamp $minTimestamp")
          for (entry in firstMeasurements) {
            if (kotlin.math.abs(minTimestamp - entry.date.time) < ALLOWED_TIME_DIFFERENCE) {
              Trace.d(TAG, "Entries similar - no cleaning needed")
              return false
            }
          }
        }
      }
      return true
    } catch (ex: Exception) {
      Trace.e(TAG, "Could not verify if clean needed", ex)
      return null
    }
  }

  private fun performImport(
    totalCount: Int,
    cleanMeasurements: Boolean,
    remoteId: Int,
    profileId: Long
  ): Result {
    Trace.d(TAG, "Will clean measurements - $cleanMeasurements")
    if (cleanMeasurements) {
      cleanMeasurements(remoteId, profileId)
    }

    val databaseCount = getLocalMeasurementsCount(remoteId, profileId)
    if (databaseCount == totalCount && !cleanMeasurements) {
      Trace.i(TAG, "Database and cloud has same size of measurements. Import skipped")
      return Result.success()
    }

    Trace.i(TAG, "Temperature measurements import started (db count: $databaseCount, remote count: $totalCount)")
    iterateAndImport(remoteId, profileId, totalCount, databaseCount)

    return Result.success()
  }

  private fun iterateAndImport(remoteId: Int, profileId: Long, totalCount: Int, databaseCount: Int) {
    val entriesToImport = totalCount - databaseCount
    var importedEntries = 0
    var afterTimestamp = getMaxTimestamp(remoteId, profileId) ?: 0
    do {
      val entries = getMeasurements(remoteId, afterTimestamp)

      if (entries.isEmpty()) {
        Trace.d(TAG, "Measurements end reached")
        return
      }

      Trace.d(TAG, "Measurements fetched ${entries.size}")
      insert(
        entries.map {
          if (it.date.time > afterTimestamp) {
            afterTimestamp = it.date.time
          }

          map(it, remoteId, profileId)
        }
      )
      importedEntries += entries.count()
      downloadEventsManager.emitProgressState(
        remoteId = remoteId,
        state = DownloadEventsManager.State.InProgress(importedEntries / entriesToImport.toFloat())
      )
    } while (isStopped.not())
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
