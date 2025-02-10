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
import org.supla.android.data.source.local.dao.measurements.GeneralPurposeMeterLogDao
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeter
import org.supla.android.features.measurementsdownload.workers.BaseDownloadLogWorker
import org.supla.android.usecases.developerinfo.CountProvider
import retrofit2.Response
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralPurposeMeterLogRepository @Inject constructor(
  private val generalPurposeMeterLogDao: GeneralPurposeMeterLogDao
) : BaseMeasurementRepository<GeneralPurposeMeter, GeneralPurposeMeterEntity>(generalPurposeMeterLogDao), CountProvider {

  fun findMeasurements(remoteId: Int, profileId: Long, startDate: Date, endDate: Date): Observable<List<GeneralPurposeMeterEntity>> =
    generalPurposeMeterLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)

  override fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<GeneralPurposeMeter>> =
    cloudService.getInitialGpmCounterMeasurements(remoteId).execute()

  override fun getMeasurements(
    cloudService: SuplaCloudService,
    remoteId: Int,
    afterTimestamp: Long
  ): Observable<List<GeneralPurposeMeter>> =
    cloudService.getGpmCounterMeasurements(
      remoteId = remoteId,
      limit = BaseDownloadLogWorker.ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    )

  override fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeterLogDao.findMinTimestamp(remoteId, profileId)

  override fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    generalPurposeMeterLogDao.findMaxTimestamp(remoteId, profileId)

  override fun findOldestEntity(remoteId: Int, profileId: Long): Maybe<GeneralPurposeMeterEntity> =
    generalPurposeMeterLogDao.findOldestEntity(remoteId, profileId)

  override fun delete(remoteId: Int, profileId: Long): Completable =
    generalPurposeMeterLogDao.delete(remoteId, profileId)

  override fun findCount(remoteId: Int, profileId: Long): Maybe<Int> =
    generalPurposeMeterLogDao.findCount(remoteId, profileId)

  override fun insert(entries: List<GeneralPurposeMeterEntity>): Completable =
    generalPurposeMeterLogDao.insert(entries)

  override fun map(entry: GeneralPurposeMeter, groupingString: String, remoteId: Int, profileId: Long) =
    GeneralPurposeMeterEntity.create(entry = entry, groupingString = groupingString, channelId = remoteId, profileId = profileId)

  override fun count(): Observable<Int> = generalPurposeMeterLogDao.count()
}
