package org.supla.android.data.source
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

import androidx.room.rxjava3.EmptyResultSetException
import androidx.work.ListenableWorker
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Single
import org.supla.android.Trace
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.Measurement
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import retrofit2.Response

private const val ALLOWED_TIME_DIFFERENCE = 1800

abstract class BaseMeasurementRepository<T : Measurement, U>(
  private val suplaCloudServiceProvider: SuplaCloudService.Provider
) {

  protected abstract fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<T>>

  protected abstract fun getMeasurements(cloudService: SuplaCloudService, remoteId: Int, afterTimestamp: Long): Observable<List<T>>

  protected abstract fun map(entry: T, remoteId: Int, profileId: Long): U

  abstract fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long>

  abstract fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long>

  abstract fun delete(remoteId: Int, profileId: Long): Completable

  abstract fun findCount(remoteId: Int, profileId: Long): Maybe<Int>

  abstract fun insert(entries: List<U>): Completable

  fun loadMeasurements(remoteId: Int, profileId: Long): Observable<Float> = Observable.create { emitter ->
    val cloudService = suplaCloudServiceProvider.provide()

    val (initialMeasurementsPair) = guardLet(loadInitialMeasurements(remoteId, cloudService)) {
      emitter.onError(IllegalStateException("Could not load initial measurements"))
      return@create
    }

    val firstMeasurements = initialMeasurementsPair.first
    val totalCount = initialMeasurementsPair.second
    Trace.d(TAG, "Found initial remote entries (count: ${firstMeasurements.size}, total count: $totalCount)")

    // Check cleanup needed
    val (cleanMeasurements) = guardLet(checkCleanNeeded(firstMeasurements, remoteId, profileId)) {
      emitter.onError(IllegalStateException("Could not verify if clean needed"))
      return@create
    }

    try {
      performImport(totalCount, cleanMeasurements, remoteId, profileId, cloudService, emitter)
      emitter.onComplete()
    } catch (ex: Exception) {
      emitter.onError(IllegalStateException("Measurements import failed", ex))
    }
  }

  private fun loadInitialMeasurements(remoteId: Int, cloudService: SuplaCloudService): Pair<List<T>, Int>? {
    try {
      val firstMeasurementsResponse = getInitialMeasurements(cloudService, remoteId)
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
        Trace.d(TAG, "No entries to get - cleaning measurements")
        return true
      } else {
        val minTimestamp = try {
          findMinTimestamp(remoteId, profileId).blockingGet()
        } catch (ex: EmptyResultSetException) {
          Trace.d(TAG, "No entries in DB - no cleaning needed")
          return false
        }

        Trace.d(TAG, "Found local minimal timestamp $minTimestamp")
        for (entry in firstMeasurements) {
          if (kotlin.math.abs(minTimestamp - entry.date.time) < ALLOWED_TIME_DIFFERENCE) {
            Trace.d(TAG, "Entries similar - no cleaning needed")
            return false
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
    profileId: Long,
    cloudService: SuplaCloudService,
    emitter: ObservableEmitter<Float>
  ): ListenableWorker.Result {
    Trace.d(TAG, "Will clean measurements - $cleanMeasurements")
    if (cleanMeasurements) {
      delete(remoteId, profileId).blockingAwait()
    }

    val databaseCount = findCount(remoteId, profileId).blockingGet() ?: 0
    if (databaseCount == totalCount && !cleanMeasurements) {
      Trace.i(TAG, "Database and cloud has same size of measurements. Import skipped")
      return ListenableWorker.Result.success()
    }

    Trace.i(TAG, "Temperature measurements import started (db count: $databaseCount, remote count: $totalCount)")
    iterateAndImport(remoteId, profileId, totalCount, databaseCount, cloudService, emitter)

    return ListenableWorker.Result.success()
  }

  private fun iterateAndImport(
    remoteId: Int,
    profileId: Long,
    totalCount: Int,
    databaseCount: Int,
    cloudService: SuplaCloudService,
    emitter: ObservableEmitter<Float>
  ) {
    val entriesToImport = totalCount - databaseCount
    var importedEntries = 0
    var afterTimestamp = try {
      findMaxTimestamp(remoteId, profileId).blockingGet()
    } catch (ex: EmptyResultSetException) {
      0
    }
    do {
      val entries = getMeasurements(cloudService, remoteId, afterTimestamp.div(1000)).blockingFirst()

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
      ).blockingAwait()
      importedEntries += entries.count()
      emitter.onNext(importedEntries / entriesToImport.toFloat())
    } while (emitter.isDisposed.not())
  }
}
