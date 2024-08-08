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
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.local.entity.custom.EnergyType
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterDiff
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.PhaseValues
import org.supla.android.data.source.local.entity.measurements.toKWh
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.ElectricityMeasurement
import org.supla.android.extensions.TAG
import org.supla.android.extensions.toTimestamp
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class DownloadElectricityMeterLogUseCase @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  suplaCloudServiceProvider: SuplaCloudService.Provider
) : BaseDownloadLogUseCase<ElectricityMeasurement, ElectricityMeterLogEntity>(suplaCloudServiceProvider, electricityMeterLogRepository) {

  override fun iterateAndImport(
    remoteId: Int,
    profileId: Long,
    totalCount: Int,
    databaseCount: Int,
    cloudService: SuplaCloudService,
    emitter: ObservableEmitter<Float>
  ) {
    val entriesToImport = totalCount - databaseCount
    val lastEntity = electricityMeterLogRepository.findOldestEntity(remoteId, profileId).blockingGet()

    var importedEntries = 0
    var afterTimestamp = lastEntity?.date?.toTimestamp() ?: 0
    var lastEntry = getLastEntry(cloudService, remoteId, afterTimestamp)

    if (afterTimestamp > 0 && lastEntry == null) {
      Trace.w(TAG, "Local entries found, but no remote entry, cleaning database entries")
      electricityMeterLogRepository.delete(remoteId, profileId).blockingAwait()
      afterTimestamp = 0
    }

    do {
      val entries = electricityMeterLogRepository.getMeasurements(cloudService, remoteId, afterTimestamp).blockingFirst()

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

  private fun getLastEntry(cloudService: SuplaCloudService, remoteId: Int, afterTimestamp: Long): ElectricityMeasurement? {
    if (afterTimestamp == 0L) {
      // Skip call when we know that we start from the beginning.
      return null
    }

    val lastEntries = cloudService.getElectricityMeasurements(
      remoteId = remoteId,
      order = "DESC",
      limit = 1,
      beforeTimestamp = afterTimestamp + 1
    ).blockingFirst()

    return if (lastEntries.isEmpty()) null else lastEntries[0]
  }

  private fun saveMeasurements(
    lastEntry: ElectricityMeasurement?,
    entries: List<ElectricityMeasurement>,
    remoteId: Int,
    profileId: Long
  ): ElectricityMeasurement? {
    var oldestEntry = lastEntry

    val complementedEntities = mutableListOf<ElectricityMeterLogEntity>().also { list ->
      entries.map { entry ->
        val oldest = oldestEntry

        if (oldest == null) {
          oldestEntry = entry
        } else {
          val entities = createEntities(entry, oldest, remoteId, profileId)
          list.addAll(entities)
          if (oldestEntry?.date?.time?.let { it < entry.date.time } != false) {
            oldestEntry = entry
          }
        }
      }
    }

    electricityMeterLogRepository.insert(complementedEntities).blockingAwait()

    return oldestEntry
  }

  private fun createEntities(
    entry: ElectricityMeasurement,
    oldest: ElectricityMeasurement,
    remoteId: Int,
    profileId: Long
  ): List<ElectricityMeterLogEntity> {
    val valueDiff = entry.diff(oldest)
    val timeDiff = entry.date.toTimestamp() - oldest.date.toTimestamp()

    return if (timeDiff > ChartDataAggregation.MINUTES.timeInSec.times(2)) {
      val missingItemsCount = timeDiff.toFloat().div(ChartDataAggregation.MINUTES.timeInSec).roundToInt()
      val valueDivided = valueDiff.div(missingItemsCount)
      generateEntities(missingItemsCount, entry, remoteId, profileId, valueDivided, valueDiff)
    } else {
      listOf(
        ElectricityMeterLogEntity.create(
          entry = entry,
          channelId = remoteId,
          profileId = profileId,
          electricityMeterDiff = valueDiff
        )
      )
    }
  }

  private fun generateEntities(
    missingItemsCount: Int,
    entry: ElectricityMeasurement,
    remoteId: Int,
    profileId: Long,
    valueDivided: ElectricityMeterDiff,
    valueDiff: ElectricityMeterDiff
  ): List<ElectricityMeterLogEntity> =
    mutableListOf<ElectricityMeterLogEntity>().also { list ->
      for (itemNo in 1..<missingItemsCount) {
        list.add(
          ElectricityMeterLogEntity.create(
            entry = entry,
            channelId = remoteId,
            profileId = profileId,
            date = Date(entry.date.time - ChartDataAggregation.MINUTES.timeInSec.times(1000).times(itemNo)),
            electricityMeterDiff = valueDivided,
            manuallyComplemented = true,
            counterReset = if (itemNo == missingItemsCount - 1) valueDiff.resetRecognized() else false
          )
        )
      }
      list.add(
        ElectricityMeterLogEntity.create(
          entry = entry,
          channelId = remoteId,
          profileId = profileId,
          electricityMeterDiff = valueDivided
        )
      )
    }

  fun ElectricityMeasurement.diff(entity: ElectricityMeasurement): ElectricityMeterDiff =
    ElectricityMeterDiff(
      phase1 = PhaseValues().apply {
        set(EnergyType.FAE, phase1Fae?.toKWh(), entity.phase1Fae?.toKWh())
        set(EnergyType.RAE, phase1Rae?.toKWh(), entity.phase1Rae?.toKWh())
        set(EnergyType.FRE, phase1Fre?.toKWh(), entity.phase1Fre?.toKWh())
        set(EnergyType.RRE, phase1Rre?.toKWh(), entity.phase1Rre?.toKWh())
      },
      phase2 = PhaseValues().apply {
        set(EnergyType.FAE, phase2Fae?.toKWh(), entity.phase2Fae?.toKWh())
        set(EnergyType.RAE, phase2Rae?.toKWh(), entity.phase2Rae?.toKWh())
        set(EnergyType.FRE, phase2Fre?.toKWh(), entity.phase2Fre?.toKWh())
        set(EnergyType.RRE, phase2Rre?.toKWh(), entity.phase2Rre?.toKWh())
      },
      phase3 = PhaseValues().apply {
        set(EnergyType.FAE, phase3Fae?.toKWh(), entity.phase3Fae?.toKWh())
        set(EnergyType.RAE, phase3Rae?.toKWh(), entity.phase3Rae?.toKWh())
        set(EnergyType.FRE, phase3Fre?.toKWh(), entity.phase3Fre?.toKWh())
        set(EnergyType.RRE, phase3Rre?.toKWh(), entity.phase3Rre?.toKWh())
      }
    ).also {
      it.set(EnergyType.FAE_BALANCED, faeBalanced?.toKWh(), entity.faeBalanced?.toKWh())
      it.set(EnergyType.RAE_BALANCED, raeBalanced?.toKWh(), entity.raeBalanced?.toKWh())
    }
}
