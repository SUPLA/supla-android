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
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.gpm.SuplaChannelConfigMeterCounterType
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeter
import org.supla.android.extensions.TAG
import org.supla.android.extensions.toTimestamp
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

@Singleton
class DownloadGeneralPurposeMeterLogUseCase @Inject constructor(
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  private val channelConfigRepository: ChannelConfigRepository,
  suplaCloudServiceProvider: SuplaCloudService.Provider
) : BaseDownloadLogUseCase<GeneralPurposeMeter, GeneralPurposeMeterEntity>(
  suplaCloudServiceProvider,
  generalPurposeMeterLogRepository
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
    var importedEntries = 0
    var lastEntity = generalPurposeMeterLogRepository.findOldestEntity(remoteId, profileId).blockingGet()
    var afterTimestamp = lastEntity?.date?.toTimestamp() ?: 0
    val channelConfig = channelConfigRepository.findGpmConfig(profileId, remoteId, ChannelConfigType.GENERAL_PURPOSE_METER)
      .blockingGet() as SuplaChannelGeneralPurposeMeterConfig
    do {
      val entries = generalPurposeMeterLogRepository.getMeasurements(cloudService, remoteId, afterTimestamp).blockingFirst()

      if (entries.isEmpty()) {
        Trace.d(TAG, "Measurements end reached")
        return
      }

      Trace.d(TAG, "Measurements fetched ${entries.size}")
      lastEntity = saveMeasurements(lastEntity, entries, remoteId, profileId, channelConfig)
      afterTimestamp = lastEntity?.date?.toTimestamp() ?: 0

      importedEntries += entries.count()
      emitter.onNext(importedEntries / entriesToImport.toFloat())
    } while (emitter.isDisposed.not())
  }

  private fun saveMeasurements(
    lastEntity: GeneralPurposeMeterEntity?,
    entries: List<GeneralPurposeMeter>,
    remoteId: Int,
    profileId: Long,
    channelConfig: SuplaChannelGeneralPurposeMeterConfig
  ): GeneralPurposeMeterEntity? {
    var oldestEntity: GeneralPurposeMeterEntity? = lastEntity

    val correctedEntries = mutableListOf<GeneralPurposeMeterEntity>().also { list ->
      entries.map { entry ->
        val oldest = oldestEntity

        val entity: GeneralPurposeMeterEntity = if (oldest == null) {
          GeneralPurposeMeterEntity.create(entry = entry, channelId = remoteId, profileId = profileId)
        } else {
          createEntityAndComplementMissing(list, entry, oldest, remoteId, profileId, channelConfig)
        }

        list.add(entity)
        if (oldestEntity?.date?.time?.let { it < entity.date.time } != false) {
          oldestEntity = entity
        }
      }
    }

    generalPurposeMeterLogRepository.insert(correctedEntries).blockingAwait()

    return oldestEntity
  }

  private fun createEntityAndComplementMissing(
    list: MutableList<GeneralPurposeMeterEntity>,
    entry: GeneralPurposeMeter,
    oldest: GeneralPurposeMeterEntity,
    remoteId: Int,
    profileId: Long,
    channelConfig: SuplaChannelGeneralPurposeMeterConfig
  ): GeneralPurposeMeterEntity {
    val valueDiff = entry.value - oldest.value
    val reset = when (channelConfig.counterType) {
      SuplaChannelConfigMeterCounterType.ALWAYS_INCREMENT ->
        valueDiff < 0 && abs(valueDiff) > oldest.value.times(0.1)

      SuplaChannelConfigMeterCounterType.ALWAYS_DECREMENT ->
        valueDiff > 0 && valueDiff > oldest.value.times(0.1)

      SuplaChannelConfigMeterCounterType.INCREMENT_AND_DECREMENT -> false
    }
    val timeDiff = entry.date.toTimestamp() - oldest.date.toTimestamp()

    val valueIncrement = if (reset) entry.value else valueDiff

    return if (channelConfig.fillMissingData && timeDiff > ChartDataAggregation.MINUTES.timeInSec.times(1.5)) {
      val missingItemsCount = timeDiff.toFloat().div(ChartDataAggregation.MINUTES.timeInSec).roundToInt()
      val valueDivided = valueIncrement.div(missingItemsCount)
      generateMissingEntities(list, missingItemsCount, entry, valueDivided, remoteId, profileId, reset)

      GeneralPurposeMeterEntity.create(
        entry = entry,
        channelId = remoteId,
        profileId = profileId,
        valueIncrement = valueDivided,
        counterIncrement = 0, // to remove
      )
    } else {
      GeneralPurposeMeterEntity.create(
        entry = entry,
        channelId = remoteId,
        profileId = profileId,
        valueIncrement = valueIncrement,
        counterIncrement = 0, // to remove
        counterReset = reset
      )
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun generateMissingEntities(
    list: MutableList<GeneralPurposeMeterEntity>,
    missingItemsCount: Int,
    entry: GeneralPurposeMeter,
    valueDivided: Float,
    remoteId: Int,
    profileId: Long,
    reset: Boolean
  ) {
    for (itemNo in 1..<missingItemsCount) {
      list.add(
        GeneralPurposeMeterEntity.create(
          entry = entry,
          channelId = remoteId,
          date = Date(entry.date.time - ChartDataAggregation.MINUTES.timeInSec.times(1000).times(itemNo)),
          valueIncrement = valueDivided,
          counterIncrement = 0, // to remove
          value = entry.value - valueDivided.times(itemNo),
          counter = 0, // to remove
          manuallyComplemented = true,
          counterReset = if (itemNo == missingItemsCount - 1) reset else false,
          profileId = profileId
        )
      )
    }
  }
}
