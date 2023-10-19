package org.supla.android.usecases.channel
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

import io.reactivex.rxjava3.core.Maybe
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.db.Channel
import org.supla.android.extensions.flatMapMerged
import org.supla.android.extensions.isThermometer
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelMeasurementsDateRangeUseCase @Inject constructor(
  private val profileManager: ProfileManager,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository
) {
  operator fun invoke(remoteId: Int): Maybe<DateRange> =
    readChannelByRemoteIdUseCase(remoteId)
      .flatMapMerged { profileManager.getCurrentProfile() }
      .flatMap {
        if (it.first.isThermometer()) {
          Maybe.zip(
            findMinTime(it.first, it.second.id).map { long -> Date(long) },
            findMaxTime(it.first, it.second.id).map { long -> Date(long) }
          ) { min, max -> DateRange(min, max) }
        } else {
          Maybe.empty()
        }
      }

  private fun findMinTime(
    channel: Channel,
    profileId: Long
  ): Maybe<Long> {
    return when (channel.func) {
      SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ->
        temperatureLogRepository.findMinTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        temperatureAndHumidityLogRepository.findMinTimestamp(channel.remoteId, profileId)

      else -> Maybe.empty()
    }
  }

  private fun findMaxTime(
    channel: Channel,
    profileId: Long
  ): Maybe<Long> {
    return when (channel.func) {
      SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ->
        temperatureLogRepository.findMaxTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        temperatureAndHumidityLogRepository.findMaxTimestamp(channel.remoteId, profileId)

      else -> Maybe.empty()
    }
  }
}
