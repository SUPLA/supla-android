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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.Trace
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.isHvacThermostat
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteChannelMeasurementsUseCase @Inject constructor(
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository,
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val humidityLogRepository: HumidityLogRepository,
  private val impulseCounterLogRepository: ImpulseCounterLogRepository
) {

  operator fun invoke(remoteId: Int): Completable =
    readChannelWithChildrenUseCase(remoteId)
      .flatMapCompletable { channelWithChildren ->
        val profileId = channelWithChildren.profileId

        when {
          channelWithChildren.function == SuplaFunction.THERMOMETER ->
            temperatureLogRepository.delete(remoteId, profileId)

          channelWithChildren.function == SuplaFunction.HUMIDITY_AND_TEMPERATURE ->
            temperatureAndHumidityLogRepository.delete(remoteId, profileId)

          channelWithChildren.function == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT ->
            generalPurposeMeasurementLogRepository.delete(remoteId, profileId)

          channelWithChildren.function == SuplaFunction.GENERAL_PURPOSE_METER ->
            generalPurposeMeterLogRepository.delete(remoteId, profileId)

          channelWithChildren.function == SuplaFunction.HUMIDITY ->
            humidityLogRepository.delete(remoteId, profileId)

          channelWithChildren.isOrHasElectricityMeter ->
            electricityMeterLogRepository.delete(remoteId, profileId)

          channelWithChildren.isOrHasImpulseCounter ->
            impulseCounterLogRepository.delete(remoteId, profileId)

          channelWithChildren.channel.isHvacThermostat() ->
            Completable.merge(
              channelWithChildren.children.filter { it.relationType.isThermometer() }
                .map {
                  when (it.channel.function) {
                    SuplaFunction.THERMOMETER -> temperatureLogRepository.delete(it.channel.remoteId, profileId)
                    SuplaFunction.HUMIDITY_AND_TEMPERATURE -> temperatureAndHumidityLogRepository.delete(it.channel.remoteId, profileId)
                    else -> invalidFunctionCompletable(it.channel.function)
                  }
                }
            )

          else -> invalidFunctionCompletable(channelWithChildren.function)
        }
      }

  private fun invalidFunctionCompletable(function: SuplaFunction) =
    Completable.fromRunnable {
      Trace.e(DeleteChannelMeasurementsUseCase::class.simpleName, "Unsupported function while deleting $function")
    }
}
