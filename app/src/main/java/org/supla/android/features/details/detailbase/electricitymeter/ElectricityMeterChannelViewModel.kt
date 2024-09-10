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

import android.annotation.SuppressLint
import org.supla.android.R
import org.supla.android.data.model.general.hasElectricityMeter
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.local.entity.complex.isElectricityMeter
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifTrue
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Measurement
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Summary
import org.supla.android.usecases.channel.GetChannelValueUseCase
import org.supla.android.usecases.channel.electricitymeter.ElectricityMeasurements
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import org.supla.android.usecases.channel.valueformatter.ChartMarkerElectricityMeterValueFormatter

interface ElectricityMeterChannelViewModel {

  fun updateElectricityMeterState(
    state: ElectricityMeterState?,
    channel: ChannelDataEntity,
    measurements: ElectricityMeasurements? = null,
    getChannelValueUseCase: GetChannelValueUseCase
  ): ElectricityMeterState? {
    val (extendedValue) = guardLet(channel.Electricity.value) {
      return tryLoadOfflineData(state, channel, measurements, getChannelValueUseCase)
    }

    val totalForwardActiveEnergy = extendedValue.summary.totalForwardActiveEnergy
    val totalReverseActiveEnergy = extendedValue.summary.totalReverseActiveEnergy
    val formatter = ChartMarkerElectricityMeterValueFormatter()

    val allTypes = extendedValue.measuredValues.suplaElectricityMeterMeasuredTypes.sortedBy { it.ordering }
    val phaseTypes = allTypes.filter { it.phaseType }

    val moreThanOnePhase = Phase.entries
      .filter { channel.flags and it.disabledFlag.rawValue == 0L }
      .size > 1
    val forwardAndReversedEnergy = allTypes.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED) &&
      allTypes.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED)

    val vectorBalancedValues = if (moreThanOnePhase && forwardAndReversedEnergy) {
      mapOf(
        SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY_BALANCED to formatter.format(extendedValue.totalForwardActiveEnergyBalanced),
        SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY_BALANCED to formatter.format(extendedValue.totalReverseActiveEnergyBalanced)
      )
    } else {
      null
    }

    return state.copyOrCreate(
      online = channel.isOnline(),
      totalForwardActiveEnergy = getForwardEnergy(extendedValue, totalForwardActiveEnergy.toFloat(), formatter),
      totalReversedActiveEnergy = getReverseEnergy(extendedValue, totalReverseActiveEnergy.toFloat(), formatter),
      currentMonthForwardActiveEnergy = measurements?.let { getForwardEnergy(extendedValue, it.forwardActiveEnergy, formatter) },
      currentMonthReversedActiveEnergy = measurements?.let { getReverseEnergy(extendedValue, it.reversedActiveEnergy, formatter) },
      phaseMeasurementTypes = phaseTypes,
      phaseMeasurementValues = getPhaseData(phaseTypes, channel.flags, extendedValue, formatter),
      vectorBalancedValues = vectorBalancedValues
    )
  }

  private fun tryLoadOfflineData(
    state: ElectricityMeterState?,
    channel: ChannelDataEntity,
    measurements: ElectricityMeasurements? = null,
    getChannelValueUseCase: GetChannelValueUseCase
  ): ElectricityMeterState? {
    if (!channel.isElectricityMeter() && !channel.hasElectricityMeter) {
      return state
    }

    val value: Double = getChannelValueUseCase(channel)
    val formatter = ChartMarkerElectricityMeterValueFormatter()

    return state.copyOrCreate(
      online = channel.isOnline(),
      totalForwardActiveEnergy = EnergyData(energy = formatter.format(value), price = null),
      totalReversedActiveEnergy = null,
      currentMonthForwardActiveEnergy = measurements?.let { EnergyData(energy = formatter.format(it.forwardActiveEnergy), price = null) },
      currentMonthReversedActiveEnergy = measurements?.let { EnergyData(energy = formatter.format(it.reversedActiveEnergy), price = null) },
      phaseMeasurementTypes = emptyList(),
      phaseMeasurementValues = emptyList(),
      vectorBalancedValues = emptyMap()
    )
  }

  private fun getForwardEnergy(
    extendedValue: SuplaChannelElectricityMeterValue,
    totalForwardActiveEnergy: Float,
    formatter: ChartMarkerElectricityMeterValueFormatter
  ): EnergyData? {
    val hasForwardEnergy = extendedValue.measuredValues and SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY.rawValue > 0

    return hasForwardEnergy.ifTrue {
      EnergyData(
        energy = formatter.format(totalForwardActiveEnergy),
        price = createCost(extendedValue.pricePerUnit, totalForwardActiveEnergy, extendedValue.currency)
      )
    }
  }

  private fun getReverseEnergy(
    extendedValue: SuplaChannelElectricityMeterValue,
    totalReverseActiveEnergy: Float,
    formatter: ChartMarkerElectricityMeterValueFormatter
  ): EnergyData? {
    val hasReverseEnergy = extendedValue.measuredValues and SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY.rawValue > 0

    return hasReverseEnergy.ifTrue(EnergyData(formatter.format(totalReverseActiveEnergy), null))
  }

  private fun getPhaseData(
    types: List<SuplaElectricityMeasurementType>,
    channelFlags: Long,
    extendedValue: SuplaChannelElectricityMeterValue,
    formatter: ChartMarkerElectricityMeterValueFormatter
  ): MutableList<PhaseWithMeasurements> {
    val phasesWithData =
      Phase.entries
        .filter { channelFlags and it.disabledFlag.rawValue == 0L }
        .map { PhaseWithData(it, extendedValue.getMeasurement(it.value, 0), extendedValue.getSummary(it.value)) }

    return mutableListOf<PhaseWithMeasurements>().apply {
      if (phasesWithData.size > 1) {
        add(PhaseWithMeasurements.allPhases(types, phasesWithData, formatter))
      }

      phasesWithData.forEach { add(PhaseWithMeasurements.forPhase(it, types, formatter)) }
    }
  }

  @SuppressLint("DefaultLocale")
  private fun createCost(price: Double, energy: Float, currency: String): String? =
    if (price == 0.0) {
      null
    } else {
      String.format("%.2f %s", energy.times(price), currency)
    }
}

private fun PhaseWithMeasurements.Companion.forPhase(
  phaseWithData: PhaseWithData,
  types: List<SuplaElectricityMeasurementType>,
  formatter: ChartMarkerElectricityMeterValueFormatter
): PhaseWithMeasurements {
  val values = mutableMapOf<SuplaElectricityMeasurementType, String>().apply {
    types.mapNotNull { type ->
      type.provider?.let { provider ->
        provider(phaseWithData.measurement, phaseWithData.summary)?.let {
          put(type, formatter.custom(it, type.precision))
        }
      }
    }
  }

  return PhaseWithMeasurements(phaseWithData.phase.label, values)
}

private fun PhaseWithMeasurements.Companion.allPhases(
  types: List<SuplaElectricityMeasurementType>,
  phasesWithData: List<PhaseWithData>,
  formatter: ChartMarkerElectricityMeterValueFormatter
): PhaseWithMeasurements {
  val values = mutableMapOf<SuplaElectricityMeasurementType, String>().apply {
    types.forEach { type ->
      // export values for given type as a list
      val values = phasesWithData.mapNotNull { type.provider?.invoke(it.measurement, it.summary) }
      // perform merge operation on the values for all phases to get single value
      if (values.isNotEmpty()) {
        type.merge(values)?.let { put(type, formatter.custom(it, type.precision)) }
      }
    }
  }
  return PhaseWithMeasurements(R.string.em_chart_all_phases, values)
}

private fun ChartMarkerElectricityMeterValueFormatter.custom(value: Float, precision: Int) =
  format(value, withUnit = false, precision = ChannelValueFormatter.Custom(precision))

private data class PhaseWithData(
  val phase: Phase,
  val measurement: Measurement?,
  val summary: Summary
)
