package org.supla.android.usecases.list
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

import kotlinx.coroutines.rx3.awaitFirstOrNull
import kotlinx.coroutines.rx3.awaitSingleOrNull
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterMeasurementType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterSettings
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.usecases.channel.measurements.electricitymeter.LoadElectricityMeterMeasurementsUseCase
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ElectricityMeterValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.withUnit
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RefreshElectricityMeterAggregatedValueUseCase @Inject constructor(
  private val loadElectricityMeterMeasurementsUseCase: LoadElectricityMeterMeasurementsUseCase,
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val channelValueRepository: ChannelValueRepository,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider
) {

  val formatter = ElectricityMeterValueFormatter()

  suspend operator fun invoke(profileId: Long, remoteId: Int) {
    val settings = userStateHolder.getElectricityMeterSettings(profileId, remoteId)
    if (!settings.usingAggregatedValue) {
      Timber.w("Refresh electricity meter aggregated value started for counter state!")
      return
    }

    val lastEntry = electricityMeterLogRepository.findOldestEntity(remoteId, profileId).awaitSingleOrNull()
    if (lastEntry == null) {
      Timber.i("Data up to date - no update needed.")
      channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT)
      return
    }

    val startTimestamp = settings.metricOnListAggregation.aggregationStartDate(dateProvider.currentDateTime)?.toEpochSecond()?.times(1000)

    val aggregatedValue: Float? = loadAggregatedValue(profileId, remoteId, settings, startTimestamp)
    if (aggregatedValue == null) {
      Timber.i("Aggregated value null - setting no value text into DB.")
      channelValueRepository.updateAggregatedValue(profileId, remoteId, NO_VALUE_TEXT)
      return
    }

    val unit = settings.metricOnList.suplaType.unit
    val formatted = formatter.format(aggregatedValue, withUnit(unit, showNoValueText = false))
    channelValueRepository.updateAggregatedValue(profileId, remoteId, formatted)
  }

  private suspend fun loadAggregatedValue(
    profileId: Long,
    remoteId: Int,
    settings: ElectricityMeterSettings,
    startTimestamp: Long?
  ): Float? =
    try {
      when (settings.metricOnList) {
        ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY ->
          loadElectricityMeterMeasurementsUseCase(profileId, remoteId, settings.metricOnListBalancing, startTimestamp)
            .awaitSingleOrNull()
            ?.forwardActiveEnergy
        ElectricityMeterMeasurementType.REVERSE_ACTIVE_ENERGY ->
          loadElectricityMeterMeasurementsUseCase(profileId, remoteId, settings.metricOnListBalancing, startTimestamp)
            .awaitSingleOrNull()
            ?.reversedActiveEnergy
        ElectricityMeterMeasurementType.FORWARD_REACTIVE_ENERGY -> loadEntries(profileId, remoteId, startTimestamp)
          ?.fold(0f) { acc, entry -> acc + entry.phasesFre }
        ElectricityMeterMeasurementType.REVERSE_REACTIVE_ENERGY -> loadEntries(profileId, remoteId, startTimestamp)
          ?.fold(0f) { acc, entry -> acc + entry.phasesRre }
        ElectricityMeterMeasurementType.ACTIVE_ENERGY_BALANCE ->
          loadElectricityMeterMeasurementsUseCase(profileId, remoteId, ElectricityMeterBalanceType.ARITHMETIC, startTimestamp)
            .awaitSingleOrNull()
            ?.summarized
        ElectricityMeterMeasurementType.POWER_ACTIVE,
        ElectricityMeterMeasurementType.CURRENT,
        ElectricityMeterMeasurementType.VOLTAGE -> null
      }
    } catch (ex: Exception) {
      Timber.e(ex, "Failed by getting aggregated value.")
      null
    }

  private suspend fun loadEntries(profileId: Long, remoteId: Int, startTimestamp: Long?) =
    electricityMeterLogRepository
      .findMeasurements(remoteId, profileId, startTimestamp ?: 0, dateProvider.currentTimestamp())
      .awaitFirstOrNull()

  private val ElectricityMeterLogEntity.phasesFre: Float
    get() = (phase1Fre ?: 0f) + (phase2Fre ?: 0f) + (phase3Fre ?: 0f)

  private val ElectricityMeterLogEntity.phasesRre: Float
    get() = (phase1Rre ?: 0f) + (phase2Rre ?: 0f) + (phase3Rre ?: 0f)
}
