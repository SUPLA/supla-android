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
import org.supla.android.data.source.local.dao.measurements.ElectricityMeterLogDao
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.ElectricityMeasurement
import org.supla.android.features.measurementsdownload.workers.BaseDownloadLogWorker
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import retrofit2.Response
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectricityMeterLogRepository @Inject constructor(
  private val electricityMeterLogDao: ElectricityMeterLogDao
) : BaseMeasurementRepository<ElectricityMeasurement, ElectricityMeterLogEntity>(electricityMeterLogDao),
  CountProvider,
  RemoveHiddenChannelsUseCase.ChannelsDeletable {

  fun findMeasurements(remoteId: Int, profileId: Long, startDate: Date, endDate: Date) =
    electricityMeterLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)

  fun findMeasurementsGrouped(remoteId: Int, profileId: Long, startDate: Date, endDate: Date, groupingStart: Int, groupingLength: Int) =
    electricityMeterLogDao.findMeasurementsGrouped(remoteId, profileId, startDate.time, endDate.time, groupingStart, groupingLength)

  fun findMeasurementsHourlyGrouped(
    remoteId: Int,
    profileId: Long,
    startDate: Date,
    endDate: Date,
    groupingStart: Int,
    groupingLength: Int
  ) =
    electricityMeterLogDao.findMeasurementsHourlyGrouped(remoteId, profileId, startDate.time, endDate.time, groupingStart, groupingLength)

  override fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<ElectricityMeasurement>> =
    cloudService.getInitialElectricityMeasurements(remoteId).execute()

  override fun getMeasurements(
    cloudService: SuplaCloudService,
    remoteId: Int,
    afterTimestamp: Long
  ): Observable<List<ElectricityMeasurement>> =
    cloudService.getElectricityMeasurements(
      remoteId = remoteId,
      limit = BaseDownloadLogWorker.ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    )

  override fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    electricityMeterLogDao.findMinTimestamp(remoteId, profileId)

  override fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    electricityMeterLogDao.findMaxTimestamp(remoteId, profileId)

  override fun findOldestEntity(remoteId: Int, profileId: Long): Maybe<ElectricityMeterLogEntity> =
    electricityMeterLogDao.findOldestEntity(remoteId, profileId)

  override fun delete(remoteId: Int, profileId: Long): Completable =
    electricityMeterLogDao.delete(remoteId, profileId)

  override fun findCount(remoteId: Int, profileId: Long): Maybe<Int> =
    electricityMeterLogDao.findCount(remoteId, profileId)

  override fun insert(entries: List<ElectricityMeterLogEntity>): Completable =
    electricityMeterLogDao.insert(entries)

  override fun map(entry: ElectricityMeasurement, groupingString: String, remoteId: Int, profileId: Long): ElectricityMeterLogEntity =
    ElectricityMeterLogEntity.create(
      entry = entry,
      groupingString = groupingString,
      channelId = remoteId,
      profileId = profileId
    )

  override fun count(): Observable<Int> = electricityMeterLogDao.count()

  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) = electricityMeterLogDao.deleteKtx(remoteId, profileId)
}
