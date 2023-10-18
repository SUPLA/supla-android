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
import org.supla.android.extensions.hasMeasurements
import org.supla.android.extensions.isHvacThermostat
import org.supla.android.lib.SuplaConst
import org.supla.android.profile.ProfileManager
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelWithChildrenMeasurementsDateRange @Inject constructor(
  private val profileManager: ProfileManager,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository
) {

  operator fun invoke(remoteId: Int): Maybe<DateRange> =
    readChannelWithChildrenUseCase(remoteId)
      .flatMapMerged { profileManager.getCurrentProfile() }
      .flatMap {
        if (it.first.channel.isHvacThermostat()) {
          Maybe.zip(
            findMinTime(it.first, it.second.id).map { long -> Date(long) },
            findMaxTime(it.first, it.second.id).map { long -> Date(long) }
          ) { min, max -> DateRange(min, max) }
        } else {
          Maybe.empty()
        }
      }

  private fun findMinTime(
    channelWithChildren: ChannelWithChildren,
    profileId: Long
  ): Maybe<Long> {
    val channelsWithMeasurements = mutableListOf<Channel>().also { list ->
      list.addAll(channelWithChildren.children.filter { it.channel.hasMeasurements() }.map { it.channel })
      if (channelWithChildren.channel.hasMeasurements()) {
        list.add(channelWithChildren.channel)
      }
    }

    val observables = mutableListOf<Maybe<Long>>().also { list ->
      channelsWithMeasurements.forEach {
        if (it.func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {
          list.add(temperatureLogRepository.findMinTimestamp(it.remoteId, profileId))
        } else if (it.func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          list.add(temperatureAndHumidityLogRepository.findMinTimestamp(it.remoteId, profileId))
        }
      }
    }

    return Maybe.zip(observables) { list -> list.filterIsInstance<Long>().min() }
  }

  private fun findMaxTime(
    channelWithChildren: ChannelWithChildren,
    profileId: Long
  ): Maybe<Long> {
    val channelsWithMeasurements = mutableListOf<Channel>().also { list ->
      list.addAll(channelWithChildren.children.filter { it.channel.hasMeasurements() }.map { it.channel })
      if (channelWithChildren.channel.hasMeasurements()) {
        list.add(channelWithChildren.channel)
      }
    }

    val observables = mutableListOf<Maybe<Long>>().also { list ->
      channelsWithMeasurements.forEach {
        if (it.func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {
          list.add(temperatureLogRepository.findMaxTimestamp(it.remoteId, profileId))
        } else if (it.func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          list.add(temperatureAndHumidityLogRepository.findMaxTimestamp(it.remoteId, profileId))
        }
      }
    }

    return Maybe.zip(observables) { it.filterIsInstance<Long>().max() }
  }
}
