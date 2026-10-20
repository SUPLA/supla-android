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

import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import kotlin.math.min

fun ElectricityMeterLogEntity.getValues(spec: ChartDataSpec): FloatArray =
  when ((spec.customFilters as? ElectricityChartFilters)?.type) {
    null -> noFiltersValues()
    ElectricityMeterChartType.BALANCE_VECTOR -> vectorBalance()
    ElectricityMeterChartType.BALANCE_ARITHMETIC -> arithmeticBalance()
    ElectricityMeterChartType.BALANCE_CHART_AGGREGATED -> arithmeticChartBalance()
    else -> phasesValues(spec.customFilters)
  }

private fun ElectricityMeterLogEntity.noFiltersValues() =
  mutableListOf<Float>()
    .apply {
      add(phase1Fae ?: 0f)
      add(phase2Fae ?: 0f)
      add(phase3Fae ?: 0f)
    }
    .toFloatArray()

private fun ElectricityMeterLogEntity.arithmeticBalance(): FloatArray {
  val consumption = (phase1Fae ?: 0f) + (phase2Fae ?: 0f) + (phase3Fae ?: 0f)
  val production = (phase1Rae ?: 0f) + (phase2Rae ?: 0f) + (phase3Rae ?: 0f)
  return balanceValues(consumption, production)
}

private fun ElectricityMeterLogEntity.arithmeticChartBalance(): FloatArray {
  val consumption = (phase1Fae ?: 0f) + (phase2Fae ?: 0f) + (phase3Fae ?: 0f)
  val production = (phase1Rae ?: 0f) + (phase2Rae ?: 0f) + (phase3Rae ?: 0f)
  return chartBalancedValues(consumption, production)
}

private fun ElectricityMeterLogEntity.vectorBalance(): FloatArray =
  balanceValues(faeBalanced ?: 0f, raeBalanced ?: 0f)

private fun ElectricityMeterLogEntity.phasesValues(filters: ElectricityChartFilters) =
  mutableListOf<Float>()
    .apply {
      if (filters.selectedPhases.contains(PhaseItem.PHASE_1)) {
        add(phase1.valueFor(filters.type) ?: 0f)
      }
      if (filters.selectedPhases.contains(PhaseItem.PHASE_2)) {
        add(phase2.valueFor(filters.type) ?: 0f)
      }
      if (filters.selectedPhases.contains(PhaseItem.PHASE_3)) {
        add(phase3.valueFor(filters.type) ?: 0f)
      }
    }
    .toFloatArray()

fun balanceValues(consumption: Float, production: Float): FloatArray {
  return floatArrayOf(consumption, -production)
}

fun chartBalancedValues(consumption: Float, production: Float): FloatArray {
  val smaller = min(consumption, production)
  return floatArrayOf(-1 * smaller, consumption - smaller, -1 * (production - smaller), smaller)
}
