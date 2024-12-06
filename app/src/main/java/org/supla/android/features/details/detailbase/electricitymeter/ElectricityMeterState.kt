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

import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType

data class ElectricityMeterState(
  val online: Boolean? = null,
  val totalForwardActiveEnergy: EnergyData? = null,
  val totalReversedActiveEnergy: EnergyData? = null,
  val currentMonthDownloading: Boolean = false,
  val currentMonthForwardActiveEnergy: EnergyData? = null,
  val currentMonthReversedActiveEnergy: EnergyData? = null,
  val phaseMeasurementTypes: List<SuplaElectricityMeasurementType> = emptyList(),
  val phaseMeasurementValues: List<PhaseWithMeasurements> = emptyList(),
  val vectorBalancedValues: Map<SuplaElectricityMeasurementType, String>? = null,
  val showIntroduction: Boolean = false
)

fun ElectricityMeterState?.copyOrCreate(
  online: Boolean? = null,
  totalForwardActiveEnergy: EnergyData? = null,
  totalReversedActiveEnergy: EnergyData? = null,
  currentMonthDownloading: Boolean = false,
  currentMonthForwardActiveEnergy: EnergyData? = null,
  currentMonthReversedActiveEnergy: EnergyData? = null,
  phaseMeasurementTypes: List<SuplaElectricityMeasurementType> = emptyList(),
  phaseMeasurementValues: List<PhaseWithMeasurements> = emptyList(),
  vectorBalancedValues: Map<SuplaElectricityMeasurementType, String>? = null,
  showIntroduction: Boolean = false
): ElectricityMeterState =
  this?.copy(
    online = online,
    totalForwardActiveEnergy = totalForwardActiveEnergy,
    totalReversedActiveEnergy = totalReversedActiveEnergy,
    currentMonthDownloading = currentMonthDownloading,
    currentMonthForwardActiveEnergy = currentMonthForwardActiveEnergy,
    currentMonthReversedActiveEnergy = currentMonthReversedActiveEnergy,
    phaseMeasurementTypes = phaseMeasurementTypes,
    phaseMeasurementValues = phaseMeasurementValues,
    vectorBalancedValues = vectorBalancedValues,
    showIntroduction = showIntroduction
  ) ?: ElectricityMeterState(
    online = online,
    totalForwardActiveEnergy = totalForwardActiveEnergy,
    totalReversedActiveEnergy = totalReversedActiveEnergy,
    currentMonthDownloading = currentMonthDownloading,
    currentMonthForwardActiveEnergy = currentMonthForwardActiveEnergy,
    currentMonthReversedActiveEnergy = currentMonthReversedActiveEnergy,
    phaseMeasurementTypes = phaseMeasurementTypes,
    phaseMeasurementValues = phaseMeasurementValues,
    vectorBalancedValues = vectorBalancedValues,
    showIntroduction = showIntroduction
  )
