package org.supla.android.usecases.channel.measurements.electricitymeter
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
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.source.ElectricityMeterLogRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.balanceHourly
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.measurements.ElectricityMeasurements
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadElectricityMeterMeasurementsUseCase @Inject constructor(
  private val electricityMeterLogRepository: ElectricityMeterLogRepository,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider
) {

  operator fun invoke(
    profileId: Long,
    remoteId: Int,
    balancing: ElectricityMeterBalanceType? = null,
    startTimestamp: Long? = null,
    endTimestamp: Long? = null
  ): Maybe<ElectricityMeasurements> =
    electricityMeterLogRepository.findMeasurements(
      remoteId,
      profileId,
      startTimestamp ?: 0,
      endTimestamp ?: dateProvider.currentTimestamp()
    )
      .firstElement()
      .flatMap { measurements -> readChannelByRemoteIdUseCase(remoteId).map { Pair(it, measurements) } }
      .map { (channel, measurements) ->

        when (balancing ?: userStateHolder.getElectricityMeterSettings(channel.profileId, channel.remoteId).currentMonthBalancing) {
          ElectricityMeterBalanceType.HOURLY -> hourlyBalance(measurements)
          ElectricityMeterBalanceType.ARITHMETIC -> arithmeticBalance(measurements)
          else -> defaultBalance(channel, measurements)
        }
      }

  private fun defaultBalance(channel: ChannelDataEntity, measurements: List<ElectricityMeterLogEntity>): ElectricityMeasurements =
    if (channel.Electricity.phases.size > 1 && channel.Electricity.hasBalance) {
      ElectricityMeasurements(
        forwardActiveEnergy = measurements.mapNotNull { it.faeBalanced }.sum(),
        reversedActiveEnergy = measurements.mapNotNull { it.raeBalanced }.sum()
      )
    } else {
      arithmeticBalance(measurements)
    }

  private fun arithmeticBalance(measurements: List<ElectricityMeterLogEntity>): ElectricityMeasurements =
    ElectricityMeasurements(
      forwardActiveEnergy = measurements.map { (it.phase1Fae ?: 0f) + (it.phase2Fae ?: 0f) + (it.phase3Fae ?: 0f) }.sum(),
      reversedActiveEnergy = measurements.map { (it.phase1Rae ?: 0f) + (it.phase2Rae ?: 0f) + (it.phase3Rae ?: 0f) }.sum()
    )

  private fun hourlyBalance(measurements: List<ElectricityMeterLogEntity>): ElectricityMeasurements {
    val balancedValue = measurements.balanceHourly()
    return ElectricityMeasurements(
      forwardActiveEnergy = balancedValue.map { it.forwarded }.sum(),
      reversedActiveEnergy = balancedValue.map { it.reversed }.sum()
    )
  }
}
