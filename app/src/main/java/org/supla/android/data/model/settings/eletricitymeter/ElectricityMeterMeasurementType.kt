package org.supla.android.data.model.settings.eletricitymeter
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

import org.supla.android.R
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

enum class ElectricityMeterMeasurementType(override val label: LocalizedString) : SpinnerItem {
  FORWARD_ACTIVE_ENERGY(localizedString(R.string.details_em_forward_active_energy)),
  REVERSE_ACTIVE_ENERGY(localizedString(R.string.details_em_reverse_active_energy)),
  FORWARD_REACTIVE_ENERGY(localizedString(R.string.details_em_forward_reactive_energy)),
  REVERSE_REACTIVE_ENERGY(localizedString(R.string.details_em_reverse_reactive_energy)),
  ACTIVE_ENERGY_BALANCE(localizedString(R.string.details_em_active_energy_balance)),
  POWER_ACTIVE(localizedString(R.string.details_em_power_active)),
  CURRENT(localizedString(R.string.details_em_current)),
  VOLTAGE(localizedString(R.string.details_em_voltage));

  val aggregationOptions: List<ListValueAggregation>
    get() = if (aggregationAvailable) {
      ListValueAggregation.entries.filter { !isBalance || it != ListValueAggregation.NO_AGGREGATION }
    } else {
      emptyList()
    }

  val balancingAvailable: Boolean
    get() = when (this) {
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY -> true
      ACTIVE_ENERGY_BALANCE,
      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY,
      POWER_ACTIVE,
      CURRENT,
      VOLTAGE -> false
    }

  val aggregationAvailable: Boolean
    get() = when (this) {
      FORWARD_ACTIVE_ENERGY,
      REVERSE_ACTIVE_ENERGY,
      FORWARD_REACTIVE_ENERGY,
      REVERSE_REACTIVE_ENERGY,
      ACTIVE_ENERGY_BALANCE -> true
      POWER_ACTIVE,
      CURRENT,
      VOLTAGE -> false
    }

  val isBalance: Boolean
    get() = this == ACTIVE_ENERGY_BALANCE

  val suplaType: SuplaElectricityMeasurementType
    get() = when (this) {
      FORWARD_ACTIVE_ENERGY -> SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY
      REVERSE_ACTIVE_ENERGY -> SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY
      FORWARD_REACTIVE_ENERGY -> SuplaElectricityMeasurementType.FORWARD_REACTIVE_ENERGY
      REVERSE_REACTIVE_ENERGY -> SuplaElectricityMeasurementType.REVERSE_REACTIVE_ENERGY
      ACTIVE_ENERGY_BALANCE -> SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED
      POWER_ACTIVE -> SuplaElectricityMeasurementType.POWER_ACTIVE
      CURRENT -> SuplaElectricityMeasurementType.CURRENT
      VOLTAGE -> SuplaElectricityMeasurementType.VOLTAGE
    }

  fun inside(measuredValues: List<SuplaElectricityMeasurementType>): Boolean =
    when (this) {
      FORWARD_ACTIVE_ENERGY -> measuredValues.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY)
      REVERSE_ACTIVE_ENERGY -> measuredValues.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY)
      FORWARD_REACTIVE_ENERGY -> measuredValues.contains(SuplaElectricityMeasurementType.FORWARD_REACTIVE_ENERGY)
      REVERSE_REACTIVE_ENERGY -> measuredValues.contains(SuplaElectricityMeasurementType.REVERSE_REACTIVE_ENERGY)
      POWER_ACTIVE -> measuredValues.contains(SuplaElectricityMeasurementType.POWER_ACTIVE) ||
        measuredValues.contains(SuplaElectricityMeasurementType.POWER_ACTIVE_KW)
      CURRENT -> measuredValues.contains(SuplaElectricityMeasurementType.CURRENT)
      VOLTAGE -> measuredValues.contains(SuplaElectricityMeasurementType.VOLTAGE)
      ACTIVE_ENERGY_BALANCE ->
        (
          measuredValues.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED) &&
            measuredValues.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED)
          ) ||
          (
            measuredValues.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY) &&
              measuredValues.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY)
            )
    }

  companion object {
    fun from(suplaType: SuplaElectricityMeasurementType): ElectricityMeterMeasurementType =
      when (suplaType) {
        SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY -> FORWARD_ACTIVE_ENERGY
        SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY -> REVERSE_ACTIVE_ENERGY
        SuplaElectricityMeasurementType.FORWARD_REACTIVE_ENERGY -> FORWARD_REACTIVE_ENERGY
        SuplaElectricityMeasurementType.REVERSE_REACTIVE_ENERGY -> REVERSE_REACTIVE_ENERGY
        SuplaElectricityMeasurementType.POWER_ACTIVE -> POWER_ACTIVE
        SuplaElectricityMeasurementType.POWER_ACTIVE_KW -> POWER_ACTIVE
        SuplaElectricityMeasurementType.VOLTAGE -> VOLTAGE
        SuplaElectricityMeasurementType.CURRENT -> CURRENT
        SuplaElectricityMeasurementType.FREQUENCY,
        SuplaElectricityMeasurementType.POWER_REACTIVE,
        SuplaElectricityMeasurementType.POWER_APPARENT,
        SuplaElectricityMeasurementType.POWER_FACTOR,
        SuplaElectricityMeasurementType.PHASE_ANGLE,
        SuplaElectricityMeasurementType.CURRENT_OVER_65A,
        SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED,
        SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED,
        SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_12,
        SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_13,
        SuplaElectricityMeasurementType.VOLTAGE_PHASE_SEQUENCE,
        SuplaElectricityMeasurementType.CURRENT_PHASE_SEQUENCE,
        SuplaElectricityMeasurementType.POWER_REACTIVE_KVAR,
        SuplaElectricityMeasurementType.POWER_APPARENT_KVA -> FORWARD_ACTIVE_ENERGY
      }
  }
}
