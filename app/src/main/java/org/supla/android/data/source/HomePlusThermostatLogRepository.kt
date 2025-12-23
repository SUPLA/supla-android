package org.supla.android.data.source
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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.local.dao.measurements.HomePlusThermostatLogDao
import org.supla.android.data.source.local.entity.measurements.HomePlusThermostatLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.ThermostatMeasurement
import org.supla.android.features.measurementsdownload.workers.BaseDownloadLogWorker
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.profile.DeleteProfileUseCase
import retrofit2.Response
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomePlusThermostatLogRepository @Inject constructor(
  private val homePlusThermostatLogDao: HomePlusThermostatLogDao
) : BaseMeasurementRepository<ThermostatMeasurement, HomePlusThermostatLogEntity>(homePlusThermostatLogDao),
  RemoveHiddenChannelsUseCase.ChannelsDeletable,
  DeleteProfileUseCase.ProfileRemover {

  fun findMeasurements(remoteId: Int, profileId: Long, startDate: Date, endDate: Date): Observable<List<HomePlusThermostatLogEntity>> =
    homePlusThermostatLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)

  override fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<ThermostatMeasurement>> =
    cloudService.getInitialThermostatHeatpolMeasurements(remoteId).execute()

  override fun getMeasurements(
    cloudService: SuplaCloudService,
    remoteId: Int,
    afterTimestamp: Long
  ): Observable<List<ThermostatMeasurement>> =
    cloudService.getThermostatHeatpolMeasurements(
      remoteId = remoteId,
      limit = BaseDownloadLogWorker.ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    )

  override fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    homePlusThermostatLogDao.findMinTimestamp(remoteId, profileId)

  override fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    homePlusThermostatLogDao.findMaxTimestamp(remoteId, profileId)

  override fun findOldestEntity(remoteId: Int, profileId: Long): Maybe<HomePlusThermostatLogEntity> =
    homePlusThermostatLogDao.findOldestEntity(remoteId, profileId)

  override fun delete(remoteId: Int, profileId: Long): Completable =
    homePlusThermostatLogDao.delete(remoteId, profileId)

  override fun findCount(remoteId: Int, profileId: Long): Maybe<Int> =
    homePlusThermostatLogDao.findCount(remoteId, profileId)

  override fun insert(entries: List<HomePlusThermostatLogEntity>): Completable =
    homePlusThermostatLogDao.insert(entries)

  override fun map(entry: ThermostatMeasurement, groupingString: String, remoteId: Int, profileId: Long) =
    HomePlusThermostatLogEntity(
      id = null,
      channelId = remoteId,
      date = entry.date,
      isOn = entry.on,
      measuredTemperature = entry.measuredTemperature,
      presetTemperature = entry.presetTemperature,
      groupingString = groupingString,
      profileId = profileId
    )

  override fun findCountWithoutGroupingString(remoteId: Int, profileId: Long): Single<Int> =
    homePlusThermostatLogDao.emptyGroupingStringCount(remoteId, profileId)

  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) = homePlusThermostatLogDao.deleteKtx(remoteId, profileId)
  override fun deleteByProfile(profileId: Long): Completable = homePlusThermostatLogDao.deleteByProfile(profileId)
}
