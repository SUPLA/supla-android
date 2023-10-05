package org.supla.android.features.temperaturesdownload

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.supla.android.data.source.local.RoomTemperatureLogDao
import org.supla.android.data.source.local.entity.TemperatureLogEntity
import org.supla.android.data.source.remote.rest.SuplaCloudService
import org.supla.android.data.source.remote.rest.channel.TemperatureMeasurement
import org.supla.android.events.DownloadEventsManager

@HiltWorker
class DownloadTemperaturesWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  suplaCloudServiceProvider: SuplaCloudService.Provider,
  downloadEventsManager: DownloadEventsManager,
  private val roomTemperatureLogDao: RoomTemperatureLogDao

) : BaseDownloadLogWorker<TemperatureMeasurement, TemperatureLogEntity>(
  appContext,
  workerParameters,
  suplaCloudServiceProvider,
  downloadEventsManager
) {

  override fun getInitialMeasurements(remoteId: Int) =
    cloudService.getInitialThermometerMeasurements(remoteId).execute()

  override fun getMeasurements(remoteId: Int, afterTimestamp: Long): List<TemperatureMeasurement> =
    cloudService.getThermometerMeasurements(
      remoteId = remoteId,
      limit = ITEMS_LIMIT_PER_REQUEST,
      afterTimestamp = afterTimestamp
    ).blockingFirst()

  override fun getMinTimestamp(remoteId: Int, profileId: Long): Long? =
    roomTemperatureLogDao.findMinTimestamp(remoteId, profileId).blockingGet()

  override fun getMaxTimestamp(remoteId: Int, profileId: Long): Long? =
    roomTemperatureLogDao.findMaxTimestamp(remoteId, profileId).blockingGet()

  override fun cleanMeasurements(remoteId: Int, profileId: Long) {
    roomTemperatureLogDao.delete(remoteId, profileId).blockingAwait()
  }

  override fun getLocalMeasurementsCount(remoteId: Int, profileId: Long): Int =
    roomTemperatureLogDao.findCount(remoteId, profileId).blockingGet() ?: 0

  override fun map(entry: TemperatureMeasurement, remoteId: Int, profileId: Long) =
    TemperatureLogEntity(
      id = null,
      channelId = remoteId,
      date = entry.date,
      temperature = entry.temperature,
      profileId = profileId
    )

  override fun insert(entries: List<TemperatureLogEntity>) {
    roomTemperatureLogDao.insert(entries).blockingAwait()
  }

  companion object {
    val WORK_ID: String = DownloadTemperaturesWorker::class.java.simpleName

    fun build(remoteId: Int, profileId: Long): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<DownloadTemperaturesWorker>()
        .setInputData(data(remoteId, profileId))
        .setConstraints(CONSTRAINTS)
        .build()
  }
}
