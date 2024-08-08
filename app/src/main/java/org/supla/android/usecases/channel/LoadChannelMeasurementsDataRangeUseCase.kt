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
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.lib.SuplaConst
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelMeasurementsDataRangeUseCase @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  thermometerDataRangeProvide: ThermometerDataRangeProvide,
  humidityAndTemperatureDataRangeProvide: HumidityAndTemperatureDataRangeProvide,
  generalPurposeMeasurementDataRangeProvide: GeneralPurposeMeasurementDataRangeProvide,
  generalPurposeMeterDataRangeProvide: GeneralPurposeMeterDataRangeProvide,
  electricityMeterDataRangeProvide: ElectricityMeterDataRangeProvide
) {

  private val providers: List<ChannelDataRangeProvider> =
    listOf(
      thermometerDataRangeProvide,
      humidityAndTemperatureDataRangeProvide,
      generalPurposeMeasurementDataRangeProvide,
      generalPurposeMeterDataRangeProvide,
      electricityMeterDataRangeProvide
    )

  operator fun invoke(remoteId: Int, profileId: Long): Single<Optional<DateRange>> =
    readChannelByRemoteIdUseCase(remoteId)
      .toSingle()
      .flatMap { channel ->
        providers.forEach {
          if (it.handle(channel.function)) {
            return@flatMap Single.zip(
              it.minTime(remoteId, profileId).map { long -> Date(long) },
              it.maxTime(remoteId, profileId).map { long -> Date(long) }
            ) { min, max -> Optional.of(DateRange(min, max)) }
              .onErrorReturnItem(Optional.empty())
          }
        }

        Single.error(IllegalArgumentException("Channel function not supported (${channel.function}"))
      }
}

interface ChannelDataRangeProvider {
  fun handle(channelFunction: Int): Boolean
  fun minTime(remoteId: Int, profileId: Long): Single<Long>
  fun maxTime(remoteId: Int, profileId: Long): Single<Long>
}

@Singleton
class ThermometerDataRangeProvide @Inject constructor(
  private val temperatureLogRepository: TemperatureLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelFunction: Int): Boolean =
    channelFunction == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class HumidityAndTemperatureDataRangeProvide @Inject constructor(
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelFunction: Int): Boolean =
    channelFunction == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureAndHumidityLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class GeneralPurposeMeterDataRangeProvide @Inject constructor(
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelFunction: Int): Boolean =
    channelFunction == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeterLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class GeneralPurposeMeasurementDataRangeProvide @Inject constructor(
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelFunction: Int): Boolean =
    channelFunction == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeasurementLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class ElectricityMeterDataRangeProvide @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelFunction: Int): Boolean =
    when (channelFunction) {
      SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER,
      SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
      SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH -> true

      else -> false
    }

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    electricityMeterLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    electricityMeterLogRepository.findMaxTimestamp(remoteId, profileId)
}
