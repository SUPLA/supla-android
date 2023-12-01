package org.supla.android.features.measurementsdownload
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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import org.supla.android.Trace
import org.supla.android.data.source.BaseMeasurementRepository
import org.supla.android.data.source.local.entity.measurements.BaseLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.Measurement
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.toTimestamp

private const val ALLOWED_TIME_DIFFERENCE = 1800

abstract class BaseDownloadLogUseCase<T : Measurement, U : BaseLogEntity>(
  private val suplaCloudServiceProvider: SuplaCloudService.Provider,
  private val baseMeasurementRepository: BaseMeasurementRepository<T, U>
) {

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
      val firstMeasurementsResponse = baseMeasurementRepository.getInitialMeasurements(cloudService, remoteId)
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
          baseMeasurementRepository.findMinTimestamp(remoteId, profileId).blockingGet()
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
  ) {
    Trace.d(TAG, "Will clean measurements - $cleanMeasurements")
    if (cleanMeasurements) {
      baseMeasurementRepository.delete(remoteId, profileId).blockingAwait()
    }

    val databaseCount = baseMeasurementRepository.findCount(remoteId, profileId).blockingGet() ?: 0
    if (databaseCount == totalCount && !cleanMeasurements) {
      Trace.i(TAG, "Database and cloud has same size of measurements. Import skipped")
      return
    }

    Trace.i(TAG, "Measurements import started (db count: $databaseCount, remote count: $totalCount)")
    iterateAndImport(remoteId, profileId, totalCount, databaseCount, cloudService, emitter)
  }

  protected open fun iterateAndImport(
    remoteId: Int,
    profileId: Long,
    totalCount: Int,
    databaseCount: Int,
    cloudService: SuplaCloudService,
    emitter: ObservableEmitter<Float>
  ) {
    val entriesToImport = totalCount - databaseCount
    var importedEntries = 0
    var lastEntity = baseMeasurementRepository.findOldestEntity(remoteId, profileId).blockingGet()
    var afterTimestamp = lastEntity?.date?.toTimestamp() ?: 0
    do {
      val entries = baseMeasurementRepository.getMeasurements(cloudService, remoteId, afterTimestamp).blockingFirst()

      if (entries.isEmpty()) {
        Trace.d(TAG, "Measurements end reached")
        return
      }

      Trace.d(TAG, "Measurements fetched ${entries.size}")
      baseMeasurementRepository.insert(
        entries.map { entry ->
          baseMeasurementRepository.map(entry, remoteId, profileId).also { entity ->
            if (lastEntity?.date?.time?.let { it < entity.date.time } != false) {
              lastEntity = entity
            }
          }
        }
      ).blockingAwait()

      afterTimestamp = lastEntity?.date?.toTimestamp() ?: 0

      importedEntries += entries.count()
      emitter.onNext(importedEntries / entriesToImport.toFloat())
    } while (emitter.isDisposed.not())
  }
}
