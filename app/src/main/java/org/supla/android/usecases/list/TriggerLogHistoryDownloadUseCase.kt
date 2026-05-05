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
import org.supla.android.data.model.settings.ListValue
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val LOG_NORMAL_REFRESH_INTERVAL = 10 * 60 * 1000 // 10 minutes
private const val LOG_OCR_REFRESH_INTERVAL = 60 * 60 * 1000 // 1 hour

@Singleton
class TriggerLogHistoryDownloadUseCase @Inject constructor(
  private val impulseCounterLogRepository: ImpulseCounterLogRepository,
  private val profileRepository: RoomProfileRepository,
  private val channelRepository: RoomChannelRepository,
  private val userStateHolder: UserStateHolder,
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val dateProvider: DateProvider
) {

  private val noEntriesLastUpdate: MutableMap<EntryKey, Date> = mutableMapOf()

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
    }

    for (channel in channels) {
      val settings = userStateHolder.getImpulseCounterSettings(channel.profileId, channel.remoteId)
      if (settings.showOnList == ListValue.COUNTER_STATE) {
        continue
      }
      Timber.i("Found channel for aggregated value refresh - ${channel.remoteId} ${settings.showOnList}")

      val logsInterval = if (SuplaChannelFlag.OCR inside channel.flags) LOG_OCR_REFRESH_INTERVAL else LOG_NORMAL_REFRESH_INTERVAL

      val currentDate = dateProvider.currentDate()
      val lastEntry = impulseCounterLogRepository.findOldestEntity(channel.remoteId, channel.profileId).awaitSingleOrNull()
      if (lastEntry == null) {
        val key = EntryKey(channel.profileId, channel.remoteId)
        val lastDownloadDate = noEntriesLastUpdate[key]

        Timber.i("Found channel without entries, last download date $lastDownloadDate")
        if (lastDownloadDate == null || currentDate.time - lastDownloadDate.time > logsInterval) {
          downloadChannelMeasurementsUseCase.invoke(ChannelWithChildren(channel))
        }

        noEntriesLastUpdate[key] = currentDate
        continue
      }

      if (currentDate.time - lastEntry.date.time > logsInterval) {
        Timber.i("Last entry older then refresh interval, scheduling download!")
        downloadChannelMeasurementsUseCase.invoke(ChannelWithChildren(channel))
        continue
      }
    }
  }

  private data class EntryKey(
    val profileId: Long,
    val remoteId: Int
  )
}
