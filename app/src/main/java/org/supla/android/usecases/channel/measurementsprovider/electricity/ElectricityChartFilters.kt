package org.supla.android.usecases.channel.measurementsprovider.electricity
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

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import org.supla.android.R
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.ElectricityChartState
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.features.details.detailbase.history.ui.CheckboxItem
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.core.shared.data.model.rest.channel.ElectricityMeterConfigDto
import org.supla.core.shared.extensions.ifTrue

@Serializable
data class ElectricityChartFilters(
  val type: ElectricityMeterChartType,
  val availableTypes: Set<ElectricityMeterChartType>,
  val selectedPhases: Set<PhaseItem>,
  val availablePhases: Set<PhaseItem>
) : ChartDataSpec.Filters {
  companion object {
    fun default(): ElectricityChartFilters =
      ElectricityChartFilters(
        type = ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY,
        availableTypes = ElectricityMeterChartType.entries.toSet(),
        selectedPhases = PhaseItem.entries.toSet(),
        availablePhases = PhaseItem.entries.toSet()
      )

    fun restore(
      flags: List<SuplaChannelFlag>,
      value: SuplaChannelElectricityMeterValue?,
      electricityMeterConfigDto: ElectricityMeterConfigDto?,
      state: ChartState
    ): ElectricityChartFilters {
      val filters = (state as? ElectricityChartState)?.customFilters ?: default()
      val availablePhases = filterPhases(flags, PhaseItem.entries.toSet())
      val selectedPhases = filterPhases(flags, filters.selectedPhases)
        .let { if (it.isEmpty() && filters.type.needsPhases) availablePhases else it }

      return filters.copy(
        availableTypes = buildTypes(value, electricityMeterConfigDto),
        selectedPhases = selectedPhases,
        availablePhases = availablePhases
      )
    }

    private fun filterPhases(flags: List<SuplaChannelFlag>, phases: Set<PhaseItem>): Set<PhaseItem> {
      return phases
        .let { if (flags.contains(SuplaChannelFlag.PHASE1_UNSUPPORTED)) it.minus(PhaseItem.PHASE_1) else it }
        .let { if (flags.contains(SuplaChannelFlag.PHASE2_UNSUPPORTED)) it.minus(PhaseItem.PHASE_2) else it }
        .let { if (flags.contains(SuplaChannelFlag.PHASE3_UNSUPPORTED)) it.minus(PhaseItem.PHASE_3) else it }
    }

    private fun buildTypes(
      value: SuplaChannelElectricityMeterValue?,
      electricityMeterConfigDto: ElectricityMeterConfigDto?
    ): Set<ElectricityMeterChartType> {
      if (value == null) {
        return emptySet()
      }

      val measuredTypes = value.measuredValues.suplaElectricityMeterMeasuredTypes

      return mutableSetOf<ElectricityMeterChartType>().apply {
        val hasForwardedEnergy = measuredTypes.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY)
        if (hasForwardedEnergy) {
          add(ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY)
        }
        val hasReversedEnergy = measuredTypes.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY)
        if (hasReversedEnergy) {
          add(ElectricityMeterChartType.REVERSED_ACTIVE_ENERGY)
        }
        if (measuredTypes.contains(SuplaElectricityMeasurementType.FORWARD_REACTIVE_ENERGY)) {
          add(ElectricityMeterChartType.FORWARDED_REACTIVE_ENERGY)
        }
        if (measuredTypes.contains(SuplaElectricityMeasurementType.REVERSE_REACTIVE_ENERGY)) {
          add(ElectricityMeterChartType.REVERSED_REACTIVE_ENERGY)
        }
        if (hasForwardedEnergy && hasReversedEnergy) {
          add(ElectricityMeterChartType.BALANCE_ARITHMETIC)
          add(ElectricityMeterChartType.BALANCE_HOURLY)
          add(ElectricityMeterChartType.BALANCE_CHART_AGGREGATED)
        }
        measuredTypes.hasBalance.ifTrue { add(ElectricityMeterChartType.BALANCE_VECTOR) }
        electricityMeterConfigDto?.voltageLoggerEnabled?.ifTrue { add(ElectricityMeterChartType.VOLTAGE) }
        electricityMeterConfigDto?.currentLoggerEnabled?.ifTrue { add(ElectricityMeterChartType.CURRENT) }
        electricityMeterConfigDto?.powerActiveLoggerEnabled?.ifTrue { add(ElectricityMeterChartType.POWER_ACTIVE) }
      }
    }
  }
}

enum class PhaseItem(@ColorRes override val color: Int, @StringRes override val label: Int) : CheckboxItem {
  PHASE_1(R.color.phase1, R.string.details_em_phase1),
  PHASE_2(R.color.phase2, R.string.details_em_phase2),
  PHASE_3(R.color.phase3, R.string.details_em_phase3)
}

fun ChartDataSpec.Filters.ifPhase1(callback: () -> Unit) {
  if ((this as? ElectricityChartFilters)?.selectedPhases?.contains(PhaseItem.PHASE_1) != false) {
    callback()
  }
}

fun ChartDataSpec.Filters.ifPhase2(callback: () -> Unit) {
  if ((this as? ElectricityChartFilters)?.selectedPhases?.contains(PhaseItem.PHASE_2) != false) {
    callback()
  }
}

fun ChartDataSpec.Filters.ifPhase3(callback: () -> Unit) {
  if ((this as? ElectricityChartFilters)?.selectedPhases?.contains(PhaseItem.PHASE_3) != false) {
    callback()
  }
}

val List<SuplaElectricityMeasurementType>.hasBalance: Boolean
  get() = contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED) &&
    contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED)
