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
import org.supla.android.data.source.local.dao.measurements.TemperatureAndHumidityLogDao
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureAndHumidityMeasurement
import org.supla.android.features.measurementsdownload.workers.BaseDownloadLogWorker
import org.supla.android.usecases.developerinfo.CountProvider
import retrofit2.Response
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureAndHumidityLogRepository @Inject constructor(
  private val temperatureAndHumidityLogDao: TemperatureAndHumidityLogDao
) : BaseMeasurementRepository<TemperatureAndHumidityMeasurement, TemperatureAndHumidityLogEntity>(), CountProvider {

  fun findMeasurements(remoteId: Int, profileId: Long, startDate: Date, endDate: Date): Observable<List<TemperatureAndHumidityLogEntity>> {
    return temperatureAndHumidityLogDao.findMeasurements(remoteId, profileId, startDate.time, endDate.time)
  }

  override fun getInitialMeasurements(cloudService: SuplaCloudService, remoteId: Int): Response<List<TemperatureAndHumidityMeasurement>> =
    cloudService.getInitialThermometerWithHumidityMeasurements(remoteId).execute()

  override fun getMeasurements(
    cloudService: SuplaCloudService,
    remoteId: Int,
    afterTimestamp: Long
  ): Observable<List<TemperatureAndHumidityMeasurement>> =
    cloudService.getThermometerWithHumidityMeasurements(
      remoteId = remoteId,
      limit = BaseDownloadLogWorker.ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    )

  override fun findMinTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    temperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId)

  override fun findMaxTimestamp(remoteId: Int, profileId: Long): Single<Long> =
    temperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId)

  override fun findOldestEntity(remoteId: Int, profileId: Long): Maybe<TemperatureAndHumidityLogEntity> =
    temperatureAndHumidityLogDao.findOldestEntity(remoteId, profileId)

  override fun delete(remoteId: Int, profileId: Long): Completable =
    temperatureAndHumidityLogDao.delete(remoteId, profileId)

  override fun findCount(remoteId: Int, profileId: Long): Maybe<Int> =
    temperatureAndHumidityLogDao.findCount(remoteId, profileId)

  override fun insert(entries: List<TemperatureAndHumidityLogEntity>): Completable =
    temperatureAndHumidityLogDao.insert(entries)

  override fun map(entry: TemperatureAndHumidityMeasurement, groupingString: String, remoteId: Int, profileId: Long) =
    TemperatureAndHumidityLogEntity(
      id = null,
      channelId = remoteId,
      date = entry.date,
      groupingString = groupingString,
      temperature = entry.temperature,
      humidity = entry.humidity,
      profileId = profileId
    )

  override fun count(): Observable<Int> = temperatureAndHumidityLogDao.count()
}
