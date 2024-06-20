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
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.isGpm
import org.supla.android.data.source.local.entity.complex.isThermometer
import org.supla.android.lib.SuplaConst
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelMeasurementsDataRangeUseCase @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository,
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository
) {
  operator fun invoke(remoteId: Int, profileId: Long): Single<Optional<DateRange>> =
    readChannelByRemoteIdUseCase(remoteId)
      .toSingle()
      .flatMap {
        if (it.isThermometer() || it.isGpm()) {
          Single.zip(
            findMinTime(it, profileId).map { long -> Date(long) },
            findMaxTime(it, profileId).map { long -> Date(long) }
          ) { min, max -> Optional.of(DateRange(min, max)) }
            .onErrorReturnItem(Optional.empty())
        } else {
          Single.error(IllegalArgumentException("Channel function not supported (${it.function}"))
        }
      }

  private fun findMinTime(
    channel: ChannelDataEntity,
    profileId: Long
  ): Single<Long> {
    return when (channel.function) {
      SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ->
        temperatureLogRepository.findMinTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        temperatureAndHumidityLogRepository.findMinTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT ->
        generalPurposeMeasurementLogRepository.findMinTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER ->
        generalPurposeMeterLogRepository.findMinTimestamp(channel.remoteId, profileId)

      else -> Single.error(IllegalArgumentException("Channel function not supported (${channel.function}"))
    }
  }

  private fun findMaxTime(
    channel: ChannelDataEntity,
    profileId: Long
  ): Single<Long> {
    return when (channel.function) {
      SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ->
        temperatureLogRepository.findMaxTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        temperatureAndHumidityLogRepository.findMaxTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT ->
        generalPurposeMeasurementLogRepository.findMaxTimestamp(channel.remoteId, profileId)

      SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER ->
        generalPurposeMeterLogRepository.findMaxTimestamp(channel.remoteId, profileId)

      else -> Single.error(IllegalArgumentException("Channel function not supported (${channel.function}"))
    }
  }
}
