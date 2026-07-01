package org.supla.android.usecases.list
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

import kotlinx.coroutines.rx3.awaitSingleOrNull
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_REFRESH_INTERVAL = 5 * 60 * 1000
private const val LOG_NORMAL_REFRESH_INTERVAL = 10 * 60 * 1000 // 10 minutes
private const val LOG_OCR_REFRESH_INTERVAL = 60 * 60 * 1000 // 1 hour

@Singleton
class TriggerLogHistoryDownloadUseCase @Inject constructor(
  downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  electricityMeterLogRepository: ElectricityMeterLogRepository,
  impulseCounterLogRepository: ImpulseCounterLogRepository,
  userStateHolder: UserStateHolder,
  dateProvider: DateProvider,
  private val profileRepository: ProfileRepository,
  private val channelRepository: ChannelRepository
) {

  private val handlers: List<ChannelHandler> = listOf(
    TriggerImpulseCounterHistoryDownloadHandler(
      downloadChannelMeasurementsUseCase,
      impulseCounterLogRepository,
      userStateHolder,
      dateProvider
    ),
    TriggerElectricityMeterHistoryDownloadHandler(
      downloadChannelMeasurementsUseCase,
      electricityMeterLogRepository,
      userStateHolder,
      dateProvider
    )
  )

  suspend operator fun invoke() {
    val activeProfile = profileRepository.findActiveProfileKtx() ?: return
    val profileId = activeProfile.id ?: return

    val channels = mutableListOf<ChannelDataEntity>().apply {
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.IC_GAS_METER))
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.IC_HEAT_METER))
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.IC_WATER_METER))
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.IC_ELECTRICITY_METER))
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.STAIRCASE_TIMER))
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.POWER_SWITCH))
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.LIGHTSWITCH))
      addAll(channelRepository.findChannelsBy(profileId, SuplaFunction.ELECTRICITY_METER))
    }

    outer@ for (channel in channels) {
      inner@ for (handler in handlers) {
        if (handler.canHandle(channel)) {
          handler.handle(channel)
          continue@outer
        }
      }
    }
  }
}

private data class EntryKey(
  val profileId: Long,
  val remoteId: Int
)

private interface ChannelHandler {
  fun canHandle(channel: ChannelDataEntity): Boolean
  suspend fun handle(channel: ChannelDataEntity)
}

private class TriggerImpulseCounterHistoryDownloadHandler(
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val impulseCounterLogRepository: ImpulseCounterLogRepository,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider
) : ChannelHandler {

  private val noEntriesLastUpdate: MutableMap<EntryKey, Date> = mutableMapOf()

  override fun canHandle(channel: ChannelDataEntity): Boolean =
    acceptableFunctions.contains(channel.function) && userStateHolder.impulseCounterSettingExists(channel.profileId, channel.remoteId)

  override suspend fun handle(channel: ChannelDataEntity) {
    val settings = userStateHolder.getImpulseCounterSettings(channel.profileId, channel.remoteId)
    if (settings.showOnList == ListValueAggregation.NO_AGGREGATION) {
      return
    }
    Timber.i("Found channel for aggregated value refresh - ${channel.remoteId} ${settings.showOnList}")

    val logsInterval = if (SuplaChannelFlag.OCR inside channel.flags) LOG_OCR_REFRESH_INTERVAL else LOG_NORMAL_REFRESH_INTERVAL

    val currentDate = dateProvider.currentDate()
    val lastEntry = impulseCounterLogRepository.findOldestEntity(channel.remoteId, channel.profileId).awaitSingleOrNull()
    if (lastEntry == null) {
      val key = EntryKey(channel.profileId, channel.remoteId)
      val lastDownloadDate = noEntriesLastUpdate[key]
      noEntriesLastUpdate[key] = currentDate

      if (lastDownloadDate != null && currentDate.time - lastDownloadDate.time <= logsInterval) {
        Timber.d("Found channel without entries, download skipped because of last try at $lastDownloadDate")
        return
      }

      Timber.d("Found channel without entries, triggering download")
      downloadChannelMeasurementsUseCase.invoke(ChannelWithChildren(channel))
      return
    }

    if (currentDate.time - lastEntry.date.time > logsInterval) {
      val key = EntryKey(channel.profileId, channel.remoteId)
      val lastDownloadDate = noEntriesLastUpdate[key]
      noEntriesLastUpdate[key] = currentDate

      // even if there is an entry, avoid downloading more often then once for 5 minutes
      if (lastDownloadDate != null && currentDate.time - lastDownloadDate.time <= MAX_REFRESH_INTERVAL) {
        Timber.d("Last entry older then refresh interval, but download already scheduled - skipping")
        return
      }

      Timber.i("Last entry older then refresh interval, scheduling download!")
      downloadChannelMeasurementsUseCase.invoke(ChannelWithChildren(channel))
      return
    }

    Timber.d("No action performed for ${channel.remoteId}")
  }

  companion object {
    val acceptableFunctions = listOf(
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH
    )
  }
}

private class TriggerElectricityMeterHistoryDownloadHandler(
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider
) : ChannelHandler {

  private val noEntriesLastUpdate: MutableMap<EntryKey, Date> = mutableMapOf()

  override fun canHandle(channel: ChannelDataEntity): Boolean =
    acceptableFunctions.contains(channel.function) && userStateHolder.electricityMeterSettingsExists(channel.profileId, channel.remoteId)

  override suspend fun handle(channel: ChannelDataEntity) {
    val settings = userStateHolder.getElectricityMeterSettings(channel.profileId, channel.remoteId)
    if (!settings.usingAggregatedValue) {
      return
    }
    Timber.i("Found channel for aggregated value refresh - ${channel.remoteId}")

    val currentDate = dateProvider.currentDate()
    val lastEntry = electricityMeterLogRepository.findOldestEntity(channel.remoteId, channel.profileId).awaitSingleOrNull()
    if (lastEntry == null) {
      val key = EntryKey(channel.profileId, channel.remoteId)
      val lastDownloadDate = noEntriesLastUpdate[key]
      noEntriesLastUpdate[key] = currentDate

      if (lastDownloadDate != null && currentDate.time - lastDownloadDate.time <= LOG_NORMAL_REFRESH_INTERVAL) {
        Timber.d("Found channel without entries, download skipped because of last try at $lastDownloadDate")
        return
      }

      Timber.d("Found channel without entries, triggering download")
      downloadChannelMeasurementsUseCase.invoke(ChannelWithChildren(channel))
      return
    }

    if (currentDate.time - lastEntry.date.time > LOG_NORMAL_REFRESH_INTERVAL) {
      val key = EntryKey(channel.profileId, channel.remoteId)
      val lastDownloadDate = noEntriesLastUpdate[key]
      noEntriesLastUpdate[key] = currentDate

      // even if there is an entry, avoid downloading more often then once for 5 minutes
      if (lastDownloadDate != null && currentDate.time - lastDownloadDate.time <= MAX_REFRESH_INTERVAL) {
        Timber.d("Last entry older then refresh interval, but download already scheduled - skipping")
        return
      }

      Timber.i("Last entry older then refresh interval, scheduling download!")
      downloadChannelMeasurementsUseCase.invoke(ChannelWithChildren(channel))
      return
    }

    Timber.d("No action performed for ${channel.remoteId}")
  }

  companion object {
    val acceptableFunctions = listOf(
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH
    )
  }
}
