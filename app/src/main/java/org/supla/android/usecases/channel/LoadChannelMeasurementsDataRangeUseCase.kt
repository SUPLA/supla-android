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
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HomePlusThermostatLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.PowerActiveLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.VoltageLogRepository
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.DownloadEventsManager
import org.supla.core.shared.data.model.general.SuplaFunction
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelMeasurementsDataRangeUseCase @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  humidityAndTemperatureDataRangeProvider: HumidityAndTemperatureDataRangeProvider,
  generalPurposeMeasurementDataRangeProvider: GeneralPurposeMeasurementDataRangeProvider,
  generalPurposeMeterDataRangeProvider: GeneralPurposeMeterDataRangeProvider,
  electricityMeterDataRangeProvider: ElectricityMeterDataRangeProvider,
  impulseCounterDataRangeProvider: ImpulseCounterDataRangeProvider,
  thermometerDataRangeProvider: ThermometerDataRangeProvider,
  humidityDataRangeProvider: HumidityDataRangeProvider,
  voltageDataRangeProvider: VoltageDataRangeProvider,
  currentDataRangeProvider: CurrentDataRangeProvider,
  powerActiveDataRangeProvider: PowerActiveDataRangeProvider,
  thermostatHeatpolDataRangeProvider: ThermostatHeatpolDataRangeProvider
) {

  private val providers: List<ChannelDataRangeProvider> =
    listOf(
      thermometerDataRangeProvider,
      humidityAndTemperatureDataRangeProvider,
      generalPurposeMeasurementDataRangeProvider,
      generalPurposeMeterDataRangeProvider,
      electricityMeterDataRangeProvider,
      impulseCounterDataRangeProvider,
      humidityDataRangeProvider,
      voltageDataRangeProvider,
      currentDataRangeProvider,
      powerActiveDataRangeProvider,
      thermostatHeatpolDataRangeProvider
    )

  operator fun invoke(
    remoteId: Int,
    profileId: Long,
    type: DownloadEventsManager.DataType = DownloadEventsManager.DataType.DEFAULT_TYPE
  ): Single<Optional<DateRange>> =
    readChannelWithChildrenUseCase(remoteId)
      .toSingle()
      .flatMap { channel ->
        providers.forEach {
          if (it.handle(channel, type)) {
            return@flatMap it.minTime(remoteId, profileId).map { long -> Date(long) }
              .flatMap { minTime -> it.maxTime(remoteId, profileId).map { long -> Optional.of(DateRange(minTime, Date(long))) } }
              .onErrorReturnItem(Optional.empty())
          }
        }

        Single.error(IllegalArgumentException("Channel function not supported (${channel.function}"))
      }
}

interface ChannelDataRangeProvider {
  fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean
  fun minTime(remoteId: Int, profileId: Long): Single<Long>
  fun maxTime(remoteId: Int, profileId: Long): Single<Long>
}

@Singleton
class ThermometerDataRangeProvider @Inject constructor(
  private val temperatureLogRepository: TemperatureLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.function == SuplaFunction.THERMOMETER

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class HumidityAndTemperatureDataRangeProvider @Inject constructor(
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.function == SuplaFunction.HUMIDITY_AND_TEMPERATURE

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureAndHumidityLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    temperatureAndHumidityLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class GeneralPurposeMeterDataRangeProvider @Inject constructor(
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.function == SuplaFunction.GENERAL_PURPOSE_METER

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeterLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeterLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class GeneralPurposeMeasurementDataRangeProvider @Inject constructor(
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.function == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeasurementLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeasurementLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class ElectricityMeterDataRangeProvider @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.isOrHasElectricityMeter && type == DownloadEventsManager.DataType.DEFAULT_TYPE

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    electricityMeterLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    electricityMeterLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class VoltageDataRangeProvider @Inject constructor(
  private val voltageLogRepository: VoltageLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.isOrHasElectricityMeter && type == DownloadEventsManager.DataType.ELECTRICITY_VOLTAGE_TYPE

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    voltageLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    voltageLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class CurrentDataRangeProvider @Inject constructor(
  private val currentLogRepository: CurrentLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.isOrHasElectricityMeter && type == DownloadEventsManager.DataType.ELECTRICITY_CURRENT_TYPE

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    currentLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    currentLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class PowerActiveDataRangeProvider @Inject constructor(
  private val powerActiveLogRepository: PowerActiveLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.isOrHasElectricityMeter && type == DownloadEventsManager.DataType.ELECTRICITY_POWER_ACTIVE_TYPE

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    powerActiveLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    powerActiveLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class HumidityDataRangeProvider @Inject constructor(
  private val humidityLogRepository: HumidityLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.function == SuplaFunction.HUMIDITY

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    humidityLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    humidityLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class ImpulseCounterDataRangeProvider @Inject constructor(
  private val impulseCounterLogRepository: ImpulseCounterLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.isOrHasImpulseCounter

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    impulseCounterLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    impulseCounterLogRepository.findMaxTimestamp(remoteId, profileId)
}

@Singleton
class ThermostatHeatpolDataRangeProvider @Inject constructor(
  private val homePlusThermostatLogRepository: HomePlusThermostatLogRepository
) : ChannelDataRangeProvider {
  override fun handle(channelWithChildren: ChannelWithChildren, type: DownloadEventsManager.DataType): Boolean =
    channelWithChildren.function == SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS

  override fun minTime(remoteId: Int, profileId: Long): Single<Long> =
    homePlusThermostatLogRepository.findMinTimestamp(remoteId, profileId)

  override fun maxTime(remoteId: Int, profileId: Long): Single<Long> =
    homePlusThermostatLogRepository.findMaxTimestamp(remoteId, profileId)
}
