package org.supla.android.usecases.migration
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
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.isGpMeasurement
import org.supla.android.data.source.local.entity.isGpMeter
import org.supla.core.shared.data.model.general.SuplaFunction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupingStringMigrationUseCase @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  private val humidityLogRepository: HumidityLogRepository,
  private val impulseCounterLogRepository: ImpulseCounterLogRepository,
  private val temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository,
  private val temperatureLogRepository: TemperatureLogRepository
) {

  operator fun invoke(channelWithChildren: ChannelWithChildren): Completable {
    return when {
      channelWithChildren.isOrHasElectricityMeter ->
        electricityMeterLogRepository.migrateGroupingString(channelWithChildren.remoteId, channelWithChildren.profileId)

      channelWithChildren.isOrHasImpulseCounter ->
        impulseCounterLogRepository.migrateGroupingString(channelWithChildren.remoteId, channelWithChildren.profileId)

      channelWithChildren.isGpMeasurement() ->
        generalPurposeMeasurementLogRepository.migrateGroupingString(channelWithChildren.remoteId, channelWithChildren.profileId)

      channelWithChildren.isGpMeter() ->
        generalPurposeMeterLogRepository.migrateGroupingString(channelWithChildren.remoteId, channelWithChildren.profileId)

      channelWithChildren.function == SuplaFunction.HUMIDITY ->
        humidityLogRepository.migrateGroupingString(channelWithChildren.remoteId, channelWithChildren.profileId)

      channelWithChildren.function == SuplaFunction.HUMIDITY_AND_TEMPERATURE ->
        temperatureAndHumidityLogRepository.migrateGroupingString(channelWithChildren.remoteId, channelWithChildren.profileId)

      channelWithChildren.function == SuplaFunction.THERMOMETER ->
        temperatureLogRepository.migrateGroupingString(channelWithChildren.remoteId, channelWithChildren.profileId)

      else ->
        Completable.fromRunnable {
          Timber.w("No migration defined for function: ${channelWithChildren.function} (channel id: ${channelWithChildren.remoteId})")
        }
    }
  }
}
