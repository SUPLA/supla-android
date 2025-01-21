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

import io.reactivex.rxjava3.core.ObservableEmitter
import org.supla.android.Trace
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.ImpulseCounterMeasurement
import org.supla.android.extensions.TAG
import org.supla.android.extensions.toTimestamp
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

@Singleton
class DownloadImpulseCounterLogUseCase @Inject constructor(
  private val impulseCounterLogRepository: ImpulseCounterLogRepository,
  suplaCloudServiceProvider: SuplaCloudService.Provider
) : BaseDownloadLogUseCase<ImpulseCounterMeasurement, ImpulseCounterLogEntity>(
  suplaCloudServiceProvider,
  impulseCounterLogRepository
) {

  override fun iterateAndImport(
    remoteId: Int,
    profileId: Long,
    totalCount: Int,
    databaseCount: Int,
    cloudService: SuplaCloudService,
    emitter: ObservableEmitter<Float>
  ) {
    val entriesToImport = totalCount - databaseCount
    val lastEntity = impulseCounterLogRepository.findOldestEntity(remoteId, profileId).blockingGet()

    var importedEntries = 0
    var afterTimestamp = lastEntity?.date?.toTimestamp() ?: 0
    var lastEntry = getLastEntry(cloudService, remoteId, afterTimestamp)

    if (afterTimestamp > 0 && lastEntry == null) {
      Trace.w(TAG, "Local entries found, but no remote entry, cleaning database entries")
      impulseCounterLogRepository.delete(remoteId, profileId).blockingAwait()
      afterTimestamp = 0
    }

    do {
      val entries = impulseCounterLogRepository.getMeasurements(cloudService, remoteId, afterTimestamp).blockingFirst()

      if (entries.isEmpty()) {
        Trace.d(TAG, "Measurements end reached")
        return
      }

      Trace.d(TAG, "Measurements fetched ${entries.size}")
      lastEntry = saveMeasurements(lastEntry, entries, remoteId, profileId)
      afterTimestamp = lastEntry?.date?.toTimestamp() ?: 0

      importedEntries += entries.count()
      emitter.onNext(importedEntries / entriesToImport.toFloat())
    } while (emitter.isDisposed.not())
  }

  private fun getLastEntry(cloudService: SuplaCloudService, remoteId: Int, afterTimestamp: Long): ImpulseCounterMeasurement? {
    if (afterTimestamp == 0L) {
      // Skip call when we know that we start from the beginning.
      return null
    }

    val lastEntries = cloudService.getImpulseCounterMeasurements(
      remoteId = remoteId,
      order = "DESC",
      limit = 1,
      beforeTimestamp = afterTimestamp + 1
    ).blockingFirst()

    return if (lastEntries.isEmpty()) null else lastEntries[0]
  }

  private fun saveMeasurements(
    lastEntry: ImpulseCounterMeasurement?,
    entries: List<ImpulseCounterMeasurement>,
    remoteId: Int,
    profileId: Long,
  ): ImpulseCounterMeasurement? {
    var oldestEntry = lastEntry

    val correctedEntries = mutableListOf<ImpulseCounterLogEntity>().also { list ->
      entries.map { entry ->
        val oldest = oldestEntry

        if (oldest == null) {
          oldestEntry = entry
        } else {
          val entity = createEntityAndComplementMissing(list, entry, oldest, remoteId, profileId)
          list.add(entity)
          if (oldestEntry?.date?.time?.let { it < entity.date.time } != false) {
            oldestEntry = entry
          }
        }
      }
    }

    impulseCounterLogRepository.insert(correctedEntries).blockingAwait()

    return oldestEntry
  }

  private fun createEntityAndComplementMissing(
    list: MutableList<ImpulseCounterLogEntity>,
    entry: ImpulseCounterMeasurement,
    oldest: ImpulseCounterMeasurement,
    remoteId: Int,
    profileId: Long,
  ): ImpulseCounterLogEntity {
    val counterDiff = entry.counter - oldest.counter
    val calculatedValueDiff = entry.calculatedValue - oldest.calculatedValue
    val reset = counterDiff < 0 && abs(counterDiff) > oldest.counter.times(0.1)
    val timeDiff = entry.date.toTimestamp() - oldest.date.toTimestamp()

    val counterIncrement = if (reset || counterDiff < 0) 0 else counterDiff
    val calculatedValueIncrement = if (reset || calculatedValueDiff < 0) 0f else calculatedValueDiff

    val missingItemsCount = timeDiff.toFloat().div(ChartDataAggregation.MINUTES.timeInSec).roundToInt()

    return if (timeDiff > ChartDataAggregation.MINUTES.timeInSec.times(1.5)) {
      val counterDivided = counterIncrement.div(missingItemsCount)
      val calculatedValueDivided = calculatedValueIncrement.div(missingItemsCount)

      generateMissingEntities(list, missingItemsCount, entry, counterDivided, calculatedValueDivided, remoteId, profileId, reset)

      ImpulseCounterLogEntity.create(
        entry = entry,
        groupingString = formatter.format(entry.date),
        channelId = remoteId,
        profileId = profileId,
        counter = counterDivided,
        calculatedValue = calculatedValueDivided
      )
    } else {
      ImpulseCounterLogEntity.create(
        entry = entry,
        groupingString = formatter.format(entry.date),
        channelId = remoteId,
        profileId = profileId,
        counter = counterIncrement,
        calculatedValue = calculatedValueIncrement
      )
    }
  }

  private fun generateMissingEntities(
    list: MutableList<ImpulseCounterLogEntity>,
    missingItemsCount: Int,
    entry: ImpulseCounterMeasurement,
    counterDivided: Long,
    calculatedValueDivided: Float,
    remoteId: Int,
    profileId: Long,
    reset: Boolean
  ) {
    for (itemNo in 1..<missingItemsCount) {
      val date = Date(entry.date.time - ChartDataAggregation.MINUTES.timeInSec.times(1000).times(itemNo))
      list.add(
        ImpulseCounterLogEntity.create(
          entry = entry,
          channelId = remoteId,
          groupingString = formatter.format(date),
          date = date,
          counter = counterDivided,
          calculatedValue = calculatedValueDivided,
          manuallyComplemented = true,
          counterReset = if (itemNo == missingItemsCount - 1) reset else false,
          profileId = profileId
        )
      )
    }
  }
}
