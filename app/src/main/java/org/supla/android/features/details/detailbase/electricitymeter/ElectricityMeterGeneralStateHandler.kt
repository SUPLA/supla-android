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

import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType.CURRENT_PHASE_SEQUENCE
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType.FREQUENCY
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_12
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType.VOLTAGE_PHASE_ANGLE_13
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType.VOLTAGE_PHASE_SEQUENCE
import org.supla.android.data.source.remote.channel.hasForwardAndReverseEnergy
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.data.source.remote.electricitymeter.getForwardEnergy
import org.supla.android.data.source.remote.electricitymeter.getReverseEnergy
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Measurement
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Summary
import org.supla.android.usecases.channel.measurements.ElectricityMeasurements
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter
import org.supla.core.shared.extensions.ifTrue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElectricityMeterGeneralStateHandler @Inject constructor(
  private val noExtendedValueStateHandler: NoExtendedValueStateHandler,
  private val preferences: Preferences
) {
  fun updateState(
    state: ElectricityMeterState?,
    channelWithChildren: ChannelWithChildren,
    measurements: ElectricityMeasurements? = null
  ): ElectricityMeterState? {
    val (extendedValue) = guardLet(channelWithChildren.channel.Electricity.value) {
      return noExtendedValueStateHandler.updateState(state, channelWithChildren, measurements)
    }
    val allTypes = extendedValue.measuredValues.suplaElectricityMeterMeasuredTypes.sortedBy { it.ordering }
    val phaseTypes = allTypes.filter { it.phaseType }

    val moreThanOnePhase = Phase.entries
      .filter { channelWithChildren.flags and it.disabledFlag.rawValue == 0L }
      .size > 1

    val formatter = ListElectricityMeterValueFormatter(useNoValue = false)
    val vectorBalancedValues = (moreThanOnePhase && allTypes.hasForwardAndReverseEnergy).ifTrue {
      mapOf(
        FORWARD_ACTIVE_ENERGY_BALANCED to formatter.format(extendedValue.totalForwardActiveEnergyBalanced, withUnit = false),
        REVERSE_ACTIVE_ENERGY_BALANCED to formatter.format(extendedValue.totalReverseActiveEnergyBalanced, withUnit = false)
      )
    }

    return state.copyOrCreate(
      online = channelWithChildren.status.online,
      totalForwardActiveEnergy = extendedValue.getForwardEnergy(formatter),
      totalReversedActiveEnergy = extendedValue.getReverseEnergy(formatter),
      currentMonthForwardActiveEnergy = measurements?.toForwardEnergy(formatter, extendedValue),
      currentMonthReversedActiveEnergy = measurements?.toReverseEnergy(formatter, extendedValue),
      phaseMeasurementTypes = phaseTypes,
      phaseMeasurementValues = getPhaseData(phaseTypes, channelWithChildren.flags, extendedValue, formatter),
      vectorBalancedValues = vectorBalancedValues,
      electricGridParameters = getGridParameters(channelWithChildren.flags, extendedValue, formatter),
      showIntroduction = preferences.shouldShowEmGeneralIntroduction() && channelWithChildren.status.online && moreThanOnePhase
    )
  }

  private fun getPhaseData(
    types: List<SuplaElectricityMeasurementType>,
    channelFlags: Long,
    extendedValue: SuplaChannelElectricityMeterValue,
    formatter: ListElectricityMeterValueFormatter
  ): MutableList<PhaseWithMeasurements> {
    val phasesWithData =
      Phase.entries
        .filter { channelFlags and it.disabledFlag.rawValue == 0L }
        .map { PhaseWithData(it, extendedValue.getMeasurement(it.value, 0), extendedValue.getSummary(it.value)) }

    return mutableListOf<PhaseWithMeasurements>().apply {
      if (phasesWithData.size > 1) {
        add(PhaseWithMeasurements.allPhases(types, phasesWithData, formatter))
      }

      phasesWithData.forEach { add(it.toPhase(types, formatter)) }
    }
  }

  private fun getGridParameters(
    channelFlags: Long,
    extendedValue: SuplaChannelElectricityMeterValue,
    formatter: ListElectricityMeterValueFormatter
  ): Map<SuplaElectricityMeasurementType, String>? {
    val measuredValues = extendedValue.measuredValues.suplaElectricityMeterMeasuredTypes
    val result = mutableMapOf<SuplaElectricityMeasurementType, String>()

    if (measuredValues.contains(FREQUENCY)) {
      Phase.entries
        .firstOrNull { channelFlags and it.disabledFlag.rawValue == 0L }
        ?.let {
          extendedValue.getMeasurement(it.value, 0)?.frequency?.let { value ->
            result[FREQUENCY] = formatter.custom(value.toFloat(), FREQUENCY.precision)
          }
        }
    }
    VOLTAGE_PHASE_ANGLE_12.let {
      if (measuredValues.contains(it)) {
        result[it] = formatter.custom(extendedValue.voltagePhaseAngle12.toFloat().div(10), it.precision)
      }
    }
    VOLTAGE_PHASE_ANGLE_13.let {
      if (measuredValues.contains(it)) {
        result[it] = formatter.custom(extendedValue.voltagePhaseAngle13.toFloat().div(10), it.precision)
      }
    }
    if (measuredValues.contains(VOLTAGE_PHASE_SEQUENCE)) {
      result[VOLTAGE_PHASE_SEQUENCE] = extendedValue.voltagePhaseSequence.text
    }
    if (measuredValues.contains(CURRENT_PHASE_SEQUENCE)) {
      result[CURRENT_PHASE_SEQUENCE] = extendedValue.currentPhaseSequence.text
    }

    return if (result.isEmpty()) null else result
  }
}

private fun PhaseWithMeasurements.Companion.allPhases(
  types: List<SuplaElectricityMeasurementType>,
  phasesWithData: List<PhaseWithData>,
  formatter: ListElectricityMeterValueFormatter
): PhaseWithMeasurements {
  val values = types
    .associateWith { type -> phasesWithData.mapNotNull { type.provider?.invoke(it.measurement, it.summary) } }
    .filterValues { it.isNotEmpty() }
    .mapValues { it.key.merge(it.value) }
    .filterValues { it != null }
    .mapValues {
      when (val value = it.value!!) {
        is SuplaElectricityMeasurementType.Value.Single ->
          formatter.custom(value.value, it.key.precision)

        is SuplaElectricityMeasurementType.Value.Double ->
          "${formatter.custom(value.first, 0)}- ${formatter.custom(value.second, 0)}"
      }
    }

  return PhaseWithMeasurements(R.string.em_chart_all_phases, values)
}

private fun ListElectricityMeterValueFormatter.custom(value: Float, precision: Int) =
  format(value, withUnit = false, precision = ChannelValueFormatter.Custom(precision))

private data class PhaseWithData(
  val phase: Phase,
  val measurement: Measurement?,
  val summary: Summary
) {
  fun toPhase(
    types: List<SuplaElectricityMeasurementType>,
    formatter: ListElectricityMeterValueFormatter
  ): PhaseWithMeasurements {
    val values = types
      .associateWith { type -> type.provider?.invoke(measurement, summary)?.let { formatter.custom(it, type.precision) } }
      .filterValues { it != null }
      .mapValues { it.value!! }

    return PhaseWithMeasurements(phase.label, values)
  }
}
