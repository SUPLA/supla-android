package org.supla.android.usecases.channel.electricitymeter
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

import io.reactivex.rxjava3.core.Maybe
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.electricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.balanceHourly
import org.supla.android.data.source.remote.electricitymeter.hasForwardEnergy
import org.supla.android.data.source.remote.electricitymeter.hasReverseEnergy
import org.supla.android.features.details.detailbase.electricitymeter.EnergyData
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter
import org.supla.core.shared.extensions.ifTrue
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadElectricityMeterMeasurementsUseCase @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val profileRepository: RoomProfileRepository,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider
) {

  operator fun invoke(remoteId: Int, startDate: Date? = null, endDate: Date? = null): Maybe<ElectricityMeasurements> =
    profileRepository.findActiveProfile()
      .flatMapObservable {
        electricityMeterLogRepository.findMeasurements(
          remoteId,
          it.id!!,
          startDate ?: Date(0),
          endDate ?: dateProvider.currentDate()
        )
      }
      .firstElement()
      .flatMap { measurements -> readChannelByRemoteIdUseCase(remoteId).map { Pair(it, measurements) } }
      .map { (channel, measurements) ->
        when (userStateHolder.getElectricityMeterSettings(channel.profileId, channel.remoteId).balancing) {
          ElectricityMeterBalanceType.HOURLY -> hourlyBalance(measurements)
          ElectricityMeterBalanceType.ARITHMETIC -> arithmeticBalance(measurements)
          else -> defaultBalance(channel, measurements)
        }
      }

  private fun defaultBalance(channel: ChannelDataEntity, measurements: List<ElectricityMeterLogEntity>): ElectricityMeasurements =
    if (channel.Electricity.phases.size > 1 && channel.Electricity.hasBalance) {
      ElectricityMeasurements(
        measurements.mapNotNull { it.faeBalanced }.sum(),
        measurements.mapNotNull { it.raeBalanced }.sum()
      )
    } else {
      arithmeticBalance(measurements)
    }

  private fun arithmeticBalance(measurements: List<ElectricityMeterLogEntity>): ElectricityMeasurements =
    ElectricityMeasurements(
      measurements.map { (it.phase1Fae ?: 0f) + (it.phase2Fae ?: 0f) + (it.phase3Fae ?: 0f) }.sum(),
      measurements.map { (it.phase1Rae ?: 0f) + (it.phase2Rae ?: 0f) + (it.phase3Rae ?: 0f) }.sum()
    )

  private fun hourlyBalance(measurements: List<ElectricityMeterLogEntity>): ElectricityMeasurements {
    val balancedValue = measurements.balanceHourly(ChartDataAggregation.Formatter())
    return ElectricityMeasurements(
      balancedValue.map { it.forwarded }.sum(),
      balancedValue.map { it.reversed }.sum()
    )
  }
}

data class ElectricityMeasurements(
  val forwardActiveEnergy: Float,
  val reversedActiveEnergy: Float
) {
  fun toForwardEnergy(
    formatter: ListElectricityMeterValueFormatter,
    electricityMeterValue: SuplaChannelElectricityMeterValue? = null
  ): EnergyData? =
    if (electricityMeterValue != null) {
      with(electricityMeterValue) {
        hasForwardEnergy.ifTrue { EnergyData(formatter, forwardActiveEnergy.toDouble(), pricePerUnit, currency) }
      }
    } else {
      EnergyData(energy = formatter.format(forwardActiveEnergy))
    }

  fun toReverseEnergy(
    formatter: ListElectricityMeterValueFormatter,
    electricityMeterValue: SuplaChannelElectricityMeterValue? = null
  ): EnergyData? =
    if (electricityMeterValue != null) {
      electricityMeterValue.hasReverseEnergy.ifTrue { EnergyData(energy = formatter.format(reversedActiveEnergy)) }
    } else {
      EnergyData(energy = formatter.format(reversedActiveEnergy))
    }
}
