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
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.DownloadEventsManager
import org.supla.android.features.measurementsdownload.workers.DownloadCurrentMeasurementsWorker
import org.supla.android.features.measurementsdownload.workers.DownloadElectricityMeasurementsWorker
import org.supla.android.features.measurementsdownload.workers.DownloadGeneralPurposeMeasurementsWorker
import org.supla.android.features.measurementsdownload.workers.DownloadGeneralPurposeMeterWorker
import org.supla.android.features.measurementsdownload.workers.DownloadHumidityWorker
import org.supla.android.features.measurementsdownload.workers.DownloadImpulseCounterWorker
import org.supla.android.features.measurementsdownload.workers.DownloadPowerActiveMeasurementsWorker
import org.supla.android.features.measurementsdownload.workers.DownloadTemperaturesAndHumidityWorker
import org.supla.android.features.measurementsdownload.workers.DownloadTemperaturesWorker
import org.supla.android.features.measurementsdownload.workers.DownloadThermostatHeatpolWorker
import org.supla.android.features.measurementsdownload.workers.DownloadVoltageMeasurementsWorker
import org.supla.core.shared.data.model.general.SuplaFunction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadChannelMeasurementsUseCase @Inject constructor(
  private val workManagerProxy: WorkManagerProxy
) {

  operator fun invoke(
    channelWithChildren: ChannelWithChildren,
    type: DownloadEventsManager.DataType = DownloadEventsManager.DataType.DEFAULT_TYPE
  ) {
    val remoteId = channelWithChildren.remoteId
    val profileId = channelWithChildren.profileId
    val function = channelWithChildren.function

    when {
      function == SuplaFunction.THERMOMETER ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadTemperaturesWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadTemperaturesWorker.build(remoteId, profileId)
        )

      function == SuplaFunction.HUMIDITY_AND_TEMPERATURE ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadTemperaturesAndHumidityWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadTemperaturesAndHumidityWorker.build(remoteId, profileId)
        )

      function == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadGeneralPurposeMeasurementsWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadGeneralPurposeMeasurementsWorker.build(remoteId, profileId)
        )

      function == SuplaFunction.GENERAL_PURPOSE_METER ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadGeneralPurposeMeterWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadGeneralPurposeMeterWorker.build(remoteId, profileId)
        )

      channelWithChildren.isOrHasElectricityMeter &&
        type == DownloadEventsManager.DataType.ELECTRICITY_VOLTAGE_TYPE ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadVoltageMeasurementsWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadVoltageMeasurementsWorker.build(remoteId, profileId)
        )

      channelWithChildren.isOrHasElectricityMeter &&
        type == DownloadEventsManager.DataType.ELECTRICITY_CURRENT_TYPE ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadCurrentMeasurementsWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadCurrentMeasurementsWorker.build(remoteId, profileId)
        )

      channelWithChildren.isOrHasElectricityMeter &&
        type == DownloadEventsManager.DataType.ELECTRICITY_POWER_ACTIVE_TYPE ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadPowerActiveMeasurementsWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadPowerActiveMeasurementsWorker.build(remoteId, profileId)
        )

      function == SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadThermostatHeatpolWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadThermostatHeatpolWorker.build(remoteId, profileId)
        )

      channelWithChildren.isOrHasElectricityMeter ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadElectricityMeasurementsWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadElectricityMeasurementsWorker.build(remoteId, profileId)
        )

      channelWithChildren.isOrHasImpulseCounter ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadImpulseCounterWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadImpulseCounterWorker.build(remoteId, profileId)
        )

      function == SuplaFunction.HUMIDITY ->
        workManagerProxy.enqueueUniqueWork(
          "${DownloadHumidityWorker.WORK_ID}.$remoteId",
          ExistingWorkPolicy.KEEP,
          DownloadHumidityWorker.build(remoteId, profileId)
        )

      else -> Timber.w("Tries to download something what is not supported (function: `$function`)")
    }
  }
}
