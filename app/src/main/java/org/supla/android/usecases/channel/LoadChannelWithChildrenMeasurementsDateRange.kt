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

import io.reactivex.rxjava3.core.Single
import org.supla.android.data.model.Optional
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

  operator fun invoke(remoteId: Int): Single<Optional<DateRange>> =
    readChannelWithChildrenUseCase(remoteId)
      .flatMapMerged { profileManager.getCurrentProfile() }
      .toSingle()
      .flatMap {
        if (it.first.channel.isHvacThermostat()) {
          Single.zip(
            findMinTime(it.first, it.second.id).map { long -> Date(long) },
            findMaxTime(it.first, it.second.id).map { long -> Date(long) }
          ) { min, max -> Optional.of(DateRange(min, max)) }
        } else {
          Single.error(IllegalArgumentException("Channel function not supported (${it.first.channel.func}"))
        }
      }

  private fun findMinTime(
    channelWithChildren: ChannelWithChildren,
    profileId: Long
  ): Single<Long> {
    val channelsWithMeasurements = mutableListOf<Channel>().also { list ->
      list.addAll(channelWithChildren.children.filter { it.channel.hasMeasurements() }.map { it.channel })
      if (channelWithChildren.channel.hasMeasurements()) {
        list.add(channelWithChildren.channel)
      }
    }

    val observables = mutableListOf<Single<Optional<Long>>>().also { list ->
      channelsWithMeasurements.forEach {
        if (it.func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {
          list.add(
            temperatureLogRepository
              .findMinTimestamp(it.remoteId, profileId)
              .map { value -> Optional.of(value) }
              .onErrorReturnItem(Optional.empty())
          )
        } else if (it.func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          list.add(
            temperatureAndHumidityLogRepository
              .findMinTimestamp(it.remoteId, profileId)
              .map { value -> Optional.of(value) }
              .onErrorReturnItem(Optional.empty())
          )
        }
      }
    }

    return Single.zip(observables) { list ->
      list.filterIsInstance<Optional<Long>>().filter { !it.isEmpty }.minOf { it.get() }
    }
  }

  private fun findMaxTime(
    channelWithChildren: ChannelWithChildren,
    profileId: Long
  ): Single<Long> {
    val channelsWithMeasurements = mutableListOf<Channel>().also { list ->
      list.addAll(channelWithChildren.children.filter { it.channel.hasMeasurements() }.map { it.channel })
      if (channelWithChildren.channel.hasMeasurements()) {
        list.add(channelWithChildren.channel)
      }
    }

    val observables = mutableListOf<Single<Optional<Long>>>().also { list ->
      channelsWithMeasurements.forEach {
        if (it.func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER) {
          list.add(
            temperatureLogRepository
              .findMaxTimestamp(it.remoteId, profileId)
              .map { value -> Optional.of(value) }
              .onErrorReturnItem(Optional.empty())
          )
        } else if (it.func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE) {
          list.add(
            temperatureAndHumidityLogRepository
              .findMaxTimestamp(it.remoteId, profileId)
              .map { value -> Optional.of(value) }
              .onErrorReturnItem(Optional.empty())
          )
        }
      }
    }

    return Single.zip(observables) { lists ->
      lists.filterIsInstance<Optional<Long>>().filter { !it.isEmpty }.maxOf { it.get() }
    }
  }
}
