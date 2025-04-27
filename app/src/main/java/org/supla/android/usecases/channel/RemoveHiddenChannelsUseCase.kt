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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.Trace
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelStateRepository
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HomePlusThermostatLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.PowerActiveLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomColorListRepository
import org.supla.android.data.source.TemperatureAndHumidityLogRepository
import org.supla.android.data.source.TemperatureLogRepository
import org.supla.android.data.source.VoltageLogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoveHiddenChannelsUseCase @Inject constructor(
  private val channelRepository: RoomChannelRepository,
  channelConfigRepository: ChannelConfigRepository,
  channelExtendedValueRepository: ChannelExtendedValueRepository,
  channelRelationRepository: ChannelRelationRepository,
  channelStateRepository: ChannelStateRepository,
  channelValueRepository: ChannelValueRepository,
  colorListRepository: RoomColorListRepository,
  currentLogRepository: CurrentLogRepository,
  androidAutoItemRepository: AndroidAutoItemRepository,
  electricityMeterLogRepository: ElectricityMeterLogRepository,
  generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  humidityLogRepository: HumidityLogRepository,
  impulseCounterLogRepository: ImpulseCounterLogRepository,
  powerActiveLogRepository: PowerActiveLogRepository,
  temperatureLogRepository: TemperatureLogRepository,
  temperatureAndHumidityLogRepository: TemperatureAndHumidityLogRepository,
  homePlusThermostatLogRepository: HomePlusThermostatLogRepository,
  voltageLogRepository: VoltageLogRepository
) {

  private val relatedRepositories: List<ChannelsDeletable> = listOf(
    channelRepository,
    channelConfigRepository,
    channelExtendedValueRepository,
    channelRelationRepository,
    channelStateRepository,
    channelValueRepository,
    colorListRepository,
    currentLogRepository,
    androidAutoItemRepository,
    electricityMeterLogRepository,
    generalPurposeMeterLogRepository,
    generalPurposeMeasurementLogRepository,
    humidityLogRepository,
    impulseCounterLogRepository,
    powerActiveLogRepository,
    temperatureLogRepository,
    temperatureAndHumidityLogRepository,
    homePlusThermostatLogRepository,
    voltageLogRepository
  )

  suspend operator fun invoke() {
    withContext(Dispatchers.IO) {
      val hiddenChannels = channelRepository.findHiddenChannels()
      Trace.i(TAG, "Found channels to remove: ${hiddenChannels.count()}")

      hiddenChannels.flatMap { channel ->
        relatedRepositories.map {
          launch { it.deleteChannelRelated(channel.remoteId, channel.profileId) }
        }
      }.joinAll()

      Trace.i(TAG, "Hidden channels removal finished")
    }
  }

  interface ChannelsDeletable {
    suspend fun deleteChannelRelated(remoteId: Int, profileId: Long)
  }

  companion object {
    private val TAG = RemoveHiddenChannelsUseCase::class.java.simpleName
  }
}
