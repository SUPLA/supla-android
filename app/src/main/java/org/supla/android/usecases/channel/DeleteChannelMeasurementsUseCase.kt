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
import io.reactivex.rxjava3.core.Maybe
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteChannelMeasurementsUseCase @Inject constructor(
  private val channelRepository: RoomChannelRepository,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val temperatureLogRepository: TemperatureLogRepository,
  private val temperatureAndHumidityLogUseCase: TemperatureAndHumidityLogRepository,
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val humidityLogRepository: HumidityLogRepository
) {

  operator fun invoke(remoteId: Int): Completable =
    channelRepository.findByRemoteId(remoteId)
      .flatMap { channelEntity ->
        when (channelEntity.function) {
          SuplaFunction.HVAC_THERMOSTAT,
          SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
          SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL ->
            readChannelWithChildrenUseCase(remoteId).map { channelWithChildren ->
              channelWithChildren.children
                .filter { child -> child.relationType.isThermometer() }
                .map { child -> child.channel }
            }

          else -> Maybe.just(listOf(channelEntity))
        }
      }
      .flatMapCompletable { entities ->
        Completable.merge(entities.map { getDeleteCompletable(it.function, it.remoteId, it.profileId) })
      }

  private fun getDeleteCompletable(function: SuplaFunction, remoteId: Int, profileId: Long): Completable =
    when (function) {
      SuplaFunction.THERMOMETER ->
        temperatureLogRepository.delete(remoteId, profileId)

      SuplaFunction.HUMIDITY_AND_TEMPERATURE ->
        temperatureAndHumidityLogUseCase.delete(remoteId, profileId)

      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT ->
        generalPurposeMeasurementLogRepository.delete(remoteId, profileId)

      SuplaFunction.GENERAL_PURPOSE_METER ->
        generalPurposeMeterLogRepository.delete(remoteId, profileId)

      SuplaFunction.ELECTRICITY_METER ->
        electricityMeterLogRepository.delete(remoteId, profileId)

      SuplaFunction.HUMIDITY ->
        humidityLogRepository.delete(remoteId, profileId)

      else ->
        Completable.error(IllegalStateException("Deleting measurements for channel with function `$function` is not supported yet!"))
    }
}
