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
import org.supla.android.data.source.local.dao.measurements.ImpulseCounterLogDao
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.ImpulseCounterMeasurement
import org.supla.android.features.measurementsdownload.workers.BaseDownloadLogWorker
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import retrofit2.Response
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImpulseCounterLogRepository @Inject constructor(
  private val impulseCounterLogDao: ImpulseCounterLogDao
) : BaseMeasurementRepository<ImpulseCounterMeasurement, ImpulseCounterLogEntity>(impulseCounterLogDao),
  CountProvider,
  RemoveHiddenChannelsUseCase.Deletable {

  fun findMeasurements(remoteId: Int, profileId: Long, startDate: Date, endDate: Date): Observable<List<ImpulseCounterLogEntity>> =
    impulseCounterLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)

  override fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<ImpulseCounterMeasurement>> =
    cloudService.getInitialImpulseCounterMeasurements(remoteId).execute()

  override fun getMeasurements(
    cloudService: SuplaCloudService,
    remoteId: Int,
    afterTimestamp: Long
  ): Observable<List<ImpulseCounterMeasurement>> =
    cloudService.getImpulseCounterMeasurements(
      remoteId = remoteId,
      limit = BaseDownloadLogWorker.ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    )

  override fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    impulseCounterLogDao.findMinTimestamp(remoteId, profileId)

  override fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    impulseCounterLogDao.findMaxTimestamp(remoteId, profileId)

  override fun findOldestEntity(remoteId: Int, profileId: Long): Maybe<ImpulseCounterLogEntity> =
    impulseCounterLogDao.findOldestEntity(remoteId, profileId)

  override fun delete(remoteId: Int, profileId: Long): Completable =
    impulseCounterLogDao.delete(remoteId, profileId)

  override fun findCount(remoteId: Int, profileId: Long): Maybe<Int> =
    impulseCounterLogDao.findCount(remoteId, profileId)

  override fun insert(entries: List<ImpulseCounterLogEntity>): Completable =
    impulseCounterLogDao.insert(entries)

  override fun map(entry: ImpulseCounterMeasurement, groupingString: String, remoteId: Int, profileId: Long) =
    ImpulseCounterLogEntity.create(entry = entry, groupingString = groupingString, channelId = remoteId, profileId = profileId)

  override fun count(): Observable<Int> = impulseCounterLogDao.count()

  override suspend fun deleteKtx(remoteId: Int, profileId: Long) = impulseCounterLogDao.deleteKtx(remoteId, profileId)
}
