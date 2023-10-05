package org.supla.android.features.temperaturesdownload
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

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.data.source.local.RoomTemperatureAndHumidityLogDao
import org.supla.android.data.source.local.entity.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureAndHumidityMeasurement
import org.supla.android.events.DownloadEventsManager
import retrofit2.Response

@HiltWorker
class DownloadTemperaturesAndHumiditiesWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  suplaCloudServiceProvider: SuplaCloudService.Provider,
  downloadEventsManager: DownloadEventsManager,
  private val roomTemperatureAndHumidityLogDao: RoomTemperatureAndHumidityLogDao
) : BaseDownloadLogWorker<TemperatureAndHumidityMeasurement, TemperatureAndHumidityLogEntity>(
  appContext,
  workerParameters,
  suplaCloudServiceProvider,
  downloadEventsManager
) {
  override fun getInitialMeasurements(remoteId: Int): Response<List<TemperatureAndHumidityMeasurement>> =
    cloudService.getInitialThermometerWithHumidityMeasurements(remoteId).execute()

  override fun getMeasurements(remoteId: Int, afterTimestamp: Long): List<TemperatureAndHumidityMeasurement> =
    cloudService.getThermometerWithHumidityMeasurements(
      remoteId = remoteId,
      limit = ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    ).blockingFirst()

  override fun getMinTimestamp(remoteId: Int, profileId: Long): Long? =
    roomTemperatureAndHumidityLogDao.findMinTimestamp(remoteId, profileId).blockingGet()

  override fun getMaxTimestamp(remoteId: Int, profileId: Long): Long? =
    roomTemperatureAndHumidityLogDao.findMaxTimestamp(remoteId, profileId).blockingGet()

  override fun cleanMeasurements(remoteId: Int, profileId: Long) {
    roomTemperatureAndHumidityLogDao.delete(remoteId, profileId)
  }

  override fun getLocalMeasurementsCount(remoteId: Int, profileId: Long): Int =
    roomTemperatureAndHumidityLogDao.findCount(remoteId, profileId).blockingGet() ?: 0

  override fun map(entry: TemperatureAndHumidityMeasurement, remoteId: Int, profileId: Long) =
    TemperatureAndHumidityLogEntity(
      id = null,
      channelId = remoteId,
      date = entry.date,
      temperature = entry.temperature,
      humidity = entry.humidity,
      profileId = profileId
    )

  override fun insert(entries: List<TemperatureAndHumidityLogEntity>) {
    roomTemperatureAndHumidityLogDao.insert(entries).blockingAwait()
  }

  companion object {
    val WORK_ID: String = DownloadTemperaturesAndHumiditiesWorker::class.java.simpleName

    fun build(remoteId: Int, profileId: Long): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<DownloadTemperaturesWorker>()
        .setInputData(data(remoteId, profileId))
        .setConstraints(CONSTRAINTS)
        .build()
  }
}
