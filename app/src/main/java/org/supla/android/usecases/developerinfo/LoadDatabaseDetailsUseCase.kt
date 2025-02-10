package org.supla.android.usecases.developerinfo
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

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.ChannelExtendedValueRepository
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelStateRepository
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.CurrentLogRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.GeneralPurposeMeasurementLogRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.HumidityLogRepository
import org.supla.android.data.source.ImpulseCounterLogRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.NotificationRepository
import org.supla.android.data.source.PowerActiveLogRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.RoomUserIconRepository
import org.supla.android.data.source.VoltageLogRepository
import org.supla.android.data.source.local.entity.ChannelConfigEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.data.source.local.entity.measurements.CurrentHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.VoltageHistoryLogEntity
import javax.inject.Inject
import javax.inject.Singleton

enum class TableDetailType {
  SUPLA, MEASUREMENTS
}

data class TableDetail(
  val name: String,
  val count: Int
)

interface CountProvider {
  fun count(): Observable<Int>
}

@Singleton
class LoadDatabaseDetailsUseCase @Inject constructor(
  channelConfigRepository: ChannelConfigRepository,
  channelExtendedValueRepository: ChannelExtendedValueRepository,
  channelGroupRelationRepository: ChannelGroupRelationRepository,
  channelGroupRepository: ChannelGroupRepository,
  channelRelationRepository: ChannelRelationRepository,
  channelStateRepository: ChannelStateRepository,
  channelValueRepository: ChannelValueRepository,
  currentLogRepository: CurrentLogRepository,
  electricityMeterLogRepository: ElectricityMeterLogRepository,
  generalPurposeMeasurementLogRepository: GeneralPurposeMeasurementLogRepository,
  generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  humidityLogRepository: HumidityLogRepository,
  impulseCounterLogRepository: ImpulseCounterLogRepository,
  locationRepository: LocationRepository,
  notificationRepository: NotificationRepository,
  powerActiveLogRepository: PowerActiveLogRepository,
  channelRepository: RoomChannelRepository,
  profileRepository: RoomProfileRepository,
  sceneRepository: RoomSceneRepository,
  userIconRepository: RoomUserIconRepository,
  voltageLogRepository: VoltageLogRepository,
) {

  private val suplaCountProviders: Map<String, CountProvider> = mapOf(
    ProfileEntity.TABLE_NAME to profileRepository,
    ChannelEntity.TABLE_NAME to channelRepository,
    ChannelConfigEntity.TABLE_NAME to channelConfigRepository,
    ChannelExtendedValueEntity.TABLE_NAME to channelExtendedValueRepository,
    ChannelGroupRelationEntity.TABLE_NAME to channelGroupRelationRepository,
    ChannelGroupEntity.TABLE_NAME to channelGroupRepository,
    ChannelRelationEntity.TABLE_NAME to channelRelationRepository,
    ChannelStateEntity.TABLE_NAME to channelStateRepository,
    ChannelValueEntity.TABLE_NAME to channelValueRepository,
    LocationEntity.TABLE_NAME to locationRepository,
    NotificationEntity.TABLE_NAME to notificationRepository,
    SceneEntity.TABLE_NAME to sceneRepository,
    UserIconEntity.TABLE_NAME to userIconRepository
  )

  private val measurementsCountProviders: Map<String, CountProvider> = mapOf(
    CurrentHistoryLogEntity.TABLE_NAME to currentLogRepository,
    ElectricityMeterLogEntity.TABLE_NAME to electricityMeterLogRepository,
    GeneralPurposeMeasurementEntity.TABLE_NAME to generalPurposeMeasurementLogRepository,
    GeneralPurposeMeterEntity.TABLE_NAME to generalPurposeMeterLogRepository,
    HumidityLogEntity.TABLE_NAME to humidityLogRepository,
    ImpulseCounterLogEntity.TABLE_NAME to impulseCounterLogRepository,
    PowerActiveHistoryLogEntity.TABLE_NAME to powerActiveLogRepository,
    VoltageHistoryLogEntity.TABLE_NAME to voltageLogRepository
  )

  operator fun invoke(type: TableDetailType): Observable<List<TableDetail>> {
    val countProviders = when (type) {
      TableDetailType.SUPLA -> suplaCountProviders
      TableDetailType.MEASUREMENTS -> measurementsCountProviders
    }

    val observables = countProviders.keys.map { tableName ->
      countProviders[tableName]!!.count().map { TableDetail(tableName, it) }
    }

    return Observable.zip(observables) { list -> list.filterIsInstance<TableDetail>() }
  }
}
