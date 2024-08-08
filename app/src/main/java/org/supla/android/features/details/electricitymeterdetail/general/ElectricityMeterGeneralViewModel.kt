package org.supla.android.features.details.electricitymeterdetail.general
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
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.data.source.remote.channel.suplaElectricityMeterMeasuredTypes
import org.supla.android.extensions.guardLet
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Measurement
import org.supla.android.lib.SuplaChannelElectricityMeterValue.Summary
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import org.supla.android.usecases.channel.valueformatter.ChartElectricityMeterValueFormatter
import javax.inject.Inject

@HiltViewModel
class ElectricityMeterGeneralViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  schedulers: SuplaSchedulers
) : BaseViewModel<ElectricityMeterGeneralViewModelState, ElectricityMeterGeneralViewEvent>(
  ElectricityMeterGeneralViewModelState(),
  schedulers
) {
  fun loadData(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = this::handleChannel,
        onError = defaultErrorHandler("loadData")
      )
      .disposeBySelf()
  }

  private fun handleChannel(channel: ChannelDataEntity) {
    val (extendedValue) = guardLet(channel.channelExtendedValueEntity?.getSuplaValue()?.ElectricityMeterValue) { return }
    val totalForwardActiveEnergy = extendedValue.summary.totalForwardActiveEnergy
    val totalReverseActiveEnergy = extendedValue.summary.totalReverseActiveEnergy
    val formatter = ChartElectricityMeterValueFormatter()

    val allTypes = extendedValue.measuredValues.suplaElectricityMeterMeasuredTypes
    val phaseTypes = allTypes.filter { it.phaseType }
    val typeFlags = allTypes.filter { it.isFlag }

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

    updateState {
      it.copy(
        viewState = it.viewState.copy(
          online = channel.isOnline(),
          totalForwardActiveEnergy = getForwardEnergy(extendedValue, totalForwardActiveEnergy, formatter),
          totalReversedActiveEnergy = getReverseEnergy(extendedValue, totalReverseActiveEnergy, formatter),
          phaseMeasurementTypes = phaseTypes,
          phaseMeasurementValues = getPhaseData(phaseTypes, typeFlags, channel.flags, extendedValue, formatter),
          vectorBalancedValues = vectorBalancedValues
        )
      )
    }
  }

  @SuppressLint("DefaultLocale")
  private fun createCost(price: Double, energy: Double, currency: String): String {
    return String.format("%.2f %s", energy.times(price), currency)
  }

  private fun getForwardEnergy(
    extendedValue: SuplaChannelElectricityMeterValue,
    totalForwardActiveEnergy: Double,
    formatter: ChartElectricityMeterValueFormatter
  ): EnergySummary? {
    val hasForwardEnergy = extendedValue.measuredValues and SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY.rawValue > 0

    return if (hasForwardEnergy) {
      EnergySummary(
        energy = formatter.format(totalForwardActiveEnergy),
        price = createCost(extendedValue.pricePerUnit, totalForwardActiveEnergy, extendedValue.currency)
      )
    } else {
      null
    }
  }

  private fun getReverseEnergy(
    extendedValue: SuplaChannelElectricityMeterValue,
    totalReverseActiveEnergy: Double,
    formatter: ChartElectricityMeterValueFormatter
  ): EnergySummary? {
    val hasReverseEnergy = extendedValue.measuredValues and SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY.rawValue > 0

    return if (hasReverseEnergy) {
      EnergySummary(
        energy = formatter.format(totalReverseActiveEnergy),
        price = createCost(extendedValue.pricePerUnit, totalReverseActiveEnergy, extendedValue.currency)
      )
    } else {
      null
    }
  }

  private fun getPhaseData(
    types: List<SuplaElectricityMeasurementType>,
    typeFlags: List<SuplaElectricityMeasurementType>,
    channelFlags: Long,
    extendedValue: SuplaChannelElectricityMeterValue,
    formatter: ChartElectricityMeterValueFormatter
  ): MutableList<PhaseWithMeasurements> {
    val phasesWithData =
      Phase.entries
        .filter { channelFlags and it.disabledFlag.rawValue == 0L }
        .map { PhaseWithData(it, extendedValue.getMeasurement(it.value, 0), extendedValue.getSummary(it.value)) }

    return mutableListOf<PhaseWithMeasurements>().apply {
      if (phasesWithData.size > 1) {
        add(PhaseWithMeasurements.allPhases(types, phasesWithData, formatter))
      }

      phasesWithData.forEach { add(PhaseWithMeasurements.forPhase(it, types, typeFlags, formatter)) }
    }
  }
}

private fun PhaseWithMeasurements.Companion.forPhase(
  phaseWithData: PhaseWithData,
  types: List<SuplaElectricityMeasurementType>,
  typeFlags: List<SuplaElectricityMeasurementType>,
  formatter: ChartElectricityMeterValueFormatter
): PhaseWithMeasurements {
  val values = mutableMapOf<SuplaElectricityMeasurementType, String>().apply {
    types.mapNotNull { type ->
      type.provider?.let { provider ->
        val value = provider(phaseWithData.measurement, phaseWithData.summary)
          .let { if (type.hasFlag && typeFlags.contains(type.flag!!)) it * 1000 else it }

        put(type, formatter.custom(value, type.precision))
      }
    }
  }

  return PhaseWithMeasurements(phaseWithData.phase.label, values)
}

private fun PhaseWithMeasurements.Companion.allPhases(
  types: List<SuplaElectricityMeasurementType>,
  phasesWithData: List<PhaseWithData>,
  formatter: ChartElectricityMeterValueFormatter
): PhaseWithMeasurements {
  val values = mutableMapOf<SuplaElectricityMeasurementType, String>().apply {
    types.forEach { type ->
      // export values for given type as a list
      val values = phasesWithData.mapNotNull { type.provider?.invoke(it.measurement, it.summary) }
      // perform merge operation on the values for all phases to get single value
      type.merge(values)?.let { put(type, formatter.custom(it, type.precision)) }
    }
  }
  return PhaseWithMeasurements(R.string.em_chart_all_phases, values)
}

fun ChartElectricityMeterValueFormatter.custom(value: Float, precision: Int) =
  format(value, withUnit = false, precision = ChannelValueFormatter.Custom(precision))

private data class PhaseWithData(
  val phase: Phase,
  val measurement: Measurement,
  val summary: Summary
)

sealed class ElectricityMeterGeneralViewEvent : ViewEvent

data class ElectricityMeterGeneralViewModelState(
  val viewState: ElectricityMeterGeneralViewState = ElectricityMeterGeneralViewState()
) : ViewState()
