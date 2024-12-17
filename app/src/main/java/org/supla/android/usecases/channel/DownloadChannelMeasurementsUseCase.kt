package org.supla.android.usecases.channel
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

import androidx.work.ExistingWorkPolicy
import org.supla.android.Trace
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.extensions.TAG
import org.supla.android.features.measurementsdownload.workers.DownloadElectricityMeasurementsWorker
import org.supla.android.features.measurementsdownload.workers.DownloadGeneralPurposeMeasurementsWorker
import org.supla.android.features.measurementsdownload.workers.DownloadGeneralPurposeMeterWorker
import org.supla.android.features.measurementsdownload.workers.DownloadHumidityWorker
import org.supla.android.features.measurementsdownload.workers.DownloadTemperaturesAndHumidityWorker
import org.supla.android.features.measurementsdownload.workers.DownloadTemperaturesWorker
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadChannelMeasurementsUseCase @Inject constructor(
  private val workManagerProxy: WorkManagerProxy
) {

  operator fun invoke(remoteId: Int, profileId: Long, function: SuplaFunction) {
    when (function) {
      SuplaFunction.THERMOMETER ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadTemperaturesWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadTemperaturesWorker.build(remoteId, profileId)
        )

      SuplaFunction.HUMIDITY_AND_TEMPERATURE ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadTemperaturesAndHumidityWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadTemperaturesAndHumidityWorker.build(remoteId, profileId)
        )

      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadGeneralPurposeMeasurementsWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadGeneralPurposeMeasurementsWorker.build(remoteId, profileId)
        )

      SuplaFunction.GENERAL_PURPOSE_METER ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadGeneralPurposeMeterWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadGeneralPurposeMeterWorker.build(remoteId, profileId)
        )

      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadElectricityMeasurementsWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadElectricityMeasurementsWorker.build(remoteId, profileId)
        )

      SuplaFunction.HUMIDITY ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadHumidityWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadHumidityWorker.build(remoteId, profileId)
        )

      else -> Trace.w(TAG, "Tries to download something what is not supported (function: `$function`)")
    }
  }
}
