package org.supla.android.features.measurementsdownload.workers
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
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeasurement
import org.supla.android.events.DownloadEventsManager
import org.supla.android.features.measurementsdownload.DownloadGeneralPurposeMeasurementLogUseCase

@HiltWorker
class DownloadGeneralPurposeMeasurementsWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParameters: WorkerParameters,
  downloadEventsManager: DownloadEventsManager,
  downloadGeneralPurposeMeasurementLogUseCase: DownloadGeneralPurposeMeasurementLogUseCase
) : BaseDownloadLogWorker<GeneralPurposeMeasurement, GeneralPurposeMeasurementEntity>(
  appContext,
  workerParameters,
  downloadEventsManager,
  downloadGeneralPurposeMeasurementLogUseCase
) {

  companion object {
    val WORK_ID: String = DownloadGeneralPurposeMeasurementsWorker::class.java.simpleName

    fun build(remoteId: Int, profileId: Long): OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<DownloadGeneralPurposeMeasurementsWorker>()
        .setInputData(data(remoteId, profileId))
        .setConstraints(CONSTRAINTS)
        .build()
  }
}
