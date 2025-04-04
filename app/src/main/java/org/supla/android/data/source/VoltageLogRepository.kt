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
import org.supla.android.data.source.local.dao.measurements.VoltageLogDao
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.local.entity.measurements.VoltageHistoryLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.HistoryMeasurement
import org.supla.android.data.source.remote.rest.channel.HistoryMeasurementType
import org.supla.android.usecases.channel.RemoveHiddenChannelsUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import retrofit2.Response
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoltageLogRepository @Inject constructor(
  private val voltageLogDao: VoltageLogDao
) : BaseMeasurementRepository<HistoryMeasurement, VoltageHistoryLogEntity>(voltageLogDao),
  CountProvider,
  RemoveHiddenChannelsUseCase.ChannelsDeletable {

  fun findMeasurements(
    remoteId: Int,
    profileId: Long,
    startDate: Date,
    endDate: Date,
    phase: Phase
  ): Observable<List<VoltageHistoryLogEntity>> {
    return voltageLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time, phase)
  }

  override fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<HistoryMeasurement>> =
    cloudService.getInitialHistoryMeasurements(remoteId, HistoryMeasurementType.VOLTAGE).execute()

  override fun getMeasurements(cloudService: SuplaCloudService, remoteId: Int, afterTimestamp: Long) =
    cloudService.getHistoryMeasurements(remoteId, afterTimestamp = afterTimestamp, HistoryMeasurementType.VOLTAGE)

  override fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    voltageLogDao.findMinTimestamp(remoteId, profileId)

  override fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    voltageLogDao.findMaxTimestamp(remoteId, profileId)

  override fun findOldestEntity(remoteId: Int, profileId: Long): Maybe<VoltageHistoryLogEntity> =
    voltageLogDao.findOldestEntity(remoteId, profileId)

  override fun delete(remoteId: Int, profileId: Long): Completable =
    voltageLogDao.delete(remoteId, profileId)

  override fun findCount(remoteId: Int, profileId: Long): Maybe<Int> =
    voltageLogDao.findCount(remoteId, profileId)

  override fun insert(entries: List<VoltageHistoryLogEntity>): Completable =
    voltageLogDao.insert(entries)

  override fun map(entry: HistoryMeasurement, groupingString: String, remoteId: Int, profileId: Long) =
    VoltageHistoryLogEntity(
      id = 0,
      channelId = remoteId,
      date = entry.date,
      phase = Phase.from(entry.phaseNo)!!,
      min = entry.min,
      max = entry.max,
      avg = entry.avg,
      groupingString = groupingString,
      profileId = profileId
    )

  override fun count(): Observable<Int> = voltageLogDao.count()
  override suspend fun deleteChannelRelated(remoteId: Int, profileId: Long) = voltageLogDao.deleteKtx(remoteId, profileId)
}
