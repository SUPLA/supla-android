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
import org.supla.android.data.source.local.RoomTemperatureLogDao
import org.supla.android.data.source.local.entity.TemperatureLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureMeasurement
import org.supla.android.features.temperaturesdownload.BaseDownloadLogWorker
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureLogRepository @Inject constructor(
  private val temperatureLogDao: RoomTemperatureLogDao,
  suplaCloudServiceProvider: SuplaCloudService.Provider
) : BaseMeasurementRepository<TemperatureMeasurement, TemperatureLogEntity>(suplaCloudServiceProvider) {

  fun findMeasurements(remoteId: Int, profileId: Long, startDate: Date, endDate: Date): Observable<List<TemperatureLogEntity>> {
    return temperatureLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)
  }

  override fun map(entry: TemperatureMeasurement, remoteId: Int, profileId: Long) =
    TemperatureLogEntity(
      id = null,
      channelId = remoteId,
      date = entry.date,
      temperature = entry.temperature,
      profileId = profileId
    )

  override fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int) =
    cloudService.getInitialThermometerMeasurements(remoteId).execute()

  override fun getMeasurements(
    cloudService: SuplaCloudService,
    remoteId: Int,
    afterTimestamp: Long
  ): Observable<List<TemperatureMeasurement>> =
    cloudService.getThermometerMeasurements(
      remoteId = remoteId,
      limit = BaseDownloadLogWorker.ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    )

  override fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    temperatureLogDao.findMinTimestamp(remoteId, profileId)

  override fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    temperatureLogDao.findMaxTimestamp(remoteId, profileId)

  override fun delete(remoteId: Int, profileId: Long): Completable =
    temperatureLogDao.delete(remoteId, profileId)

  override fun findCount(remoteId: Int, profileId: Long): Maybe<Int> =
    temperatureLogDao.findCount(remoteId, profileId)

  override fun insert(entries: List<TemperatureLogEntity>): Completable =
    temperatureLogDao.insert(entries)
}
