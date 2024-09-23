package org.supla.android.features.details.detailbase.electricitymeter
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

import org.supla.android.data.model.general.hasElectricityMeter
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.isElectricityMeter
import org.supla.android.usecases.channel.GetChannelValueUseCase
import org.supla.android.usecases.channel.electricitymeter.ElectricityMeasurements
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoExtendedValueStateHandler @Inject constructor(
  private val getChannelValueUseCase: GetChannelValueUseCase
) {

  fun updateState(
    state: ElectricityMeterState?,
    channel: ChannelDataEntity,
    measurements: ElectricityMeasurements? = null
  ): ElectricityMeterState? {
    if (!channel.isElectricityMeter() && !channel.hasElectricityMeter) {
      return state
    }

    val value: Double = getChannelValueUseCase(channel)
    val formatter = ListElectricityMeterValueFormatter(useNoValue = false)

    return state.copyOrCreate(
      online = channel.isOnline(),
      totalForwardActiveEnergy = EnergyData(energy = formatter.format(value), price = null),
      totalReversedActiveEnergy = null,
      currentMonthForwardActiveEnergy = measurements?.toForwardEnergy(formatter),
      currentMonthReversedActiveEnergy = measurements?.toReverseEnergy(formatter),
      phaseMeasurementTypes = emptyList(),
      phaseMeasurementValues = emptyList(),
      vectorBalancedValues = emptyMap()
    )
  }
}
