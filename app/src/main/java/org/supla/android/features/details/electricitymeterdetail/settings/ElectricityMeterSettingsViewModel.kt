package org.supla.android.features.details.electricitymeterdetail.settings
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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.shared.shareable
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.model.settings.ListValueAggregation
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterBalanceType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterMeasurementType
import org.supla.android.data.model.settings.eletricitymeter.ElectricityMeterSettings
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.list.RefreshElectricityMeterAggregatedValueUseCase
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject

@HiltViewModel
class ElectricityMeterSettingsViewModel @Inject constructor(
  private val refreshElectricityMeterAggregatedValueUseCase: RefreshElectricityMeterAggregatedValueUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val userStateHolder: UserStateHolder,
  schedulers: SuplaSchedulers
) : BaseViewModel<ElectricityMeterSettingsViewState, ElectricityMeterSettingsViewEvent>(
  ElectricityMeterSettingsViewState(),
  schedulers
),
  ElectricityMeterSettingsScope {

  private val availableBalancingOptions: MutableList<ElectricityMeterBalanceType> = mutableListOf()
  private var profileId = 0L
  private var remoteId = 0

  fun loadData(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = this::handleChannel,
        onError = defaultErrorHandler("loadData")
      )
      .disposeBySelf()
  }

  override fun onMetricOnListChange(type: ElectricityMeterMeasurementType) {
    val settings = userStateHolder.getElectricityMeterSettings(profileId, remoteId)
    if (settings.metricOnList == type) {
      return // No change
    }

    updateState { state ->
      val availableAggregations = type.aggregationOptions
      val selectedAggregation = availableAggregations.select(state.metricOnListAggregation?.selected)

      val availableBalances = type.balancingOptions(selectedAggregation)
      val selectedBalance = availableBalances.select(state.metricOnListBalancing?.selected)

      val updatedSettings = settings.copy(
        metricOnList = type,
        metricOnListAggregation = selectedAggregation,
        metricOnListBalancing = selectedBalance
      )
      userStateHolder.setElectricityMeterSettings(updatedSettings, profileId, remoteId)

      state.copy(
        metricOnList = state.metricOnList?.copy(selected = type),
        metricOnListAggregation = availableAggregations.selectionList(selectedAggregation),
        metricOnListBalancing = availableBalances.selectionList(selectedBalance),
      )
    }

    viewModelScope.launch {
      schedulers.io {
        refreshElectricityMeterAggregatedValueUseCase(profileId, remoteId)
      }
    }
  }

  override fun onMetricOnListAggregationChanged(aggregation: ListValueAggregation) {
    val settings = userStateHolder.getElectricityMeterSettings(profileId, remoteId)
    if (settings.metricOnListAggregation == aggregation) {
      return // No change
    }

    updateState {
      val availableBalances = settings.metricOnList.balancingOptions(aggregation)
      val selectedBalance = availableBalances.select(it.metricOnListBalancing?.selected)

      val updatedSettings = settings.copy(
        metricOnListAggregation = aggregation,
        metricOnListBalancing = selectedBalance
      )
      userStateHolder.setElectricityMeterSettings(updatedSettings, profileId, remoteId)

      it.copy(
        metricOnListAggregation = it.metricOnListAggregation?.copy(selected = aggregation),
        metricOnListBalancing = availableBalances.selectionList(selectedBalance)
      )
    }

    viewModelScope.launch {
      schedulers.io {
        refreshElectricityMeterAggregatedValueUseCase(profileId, remoteId)
      }
    }
  }

  override fun onMetricOnListBalancingChanged(type: ElectricityMeterBalanceType) {
    val settings = userStateHolder.getElectricityMeterSettings(profileId, remoteId)
    if (settings.metricOnListBalancing == type) {
      return // No change
    }
    val updatedSettings = settings.copy(metricOnListBalancing = type)
    userStateHolder.setElectricityMeterSettings(updatedSettings, profileId, remoteId)

    updateState {
      it.copy(metricOnListBalancing = it.metricOnListBalancing?.copy(selected = type))
    }

    viewModelScope.launch {
      schedulers.io {
        refreshElectricityMeterAggregatedValueUseCase(profileId, remoteId)
      }
    }
  }

  override fun onCurrentMonthBalancingChanged(type: ElectricityMeterBalanceType) {
    updateState {
      val settings = userStateHolder.getElectricityMeterSettings(profileId, remoteId)
      userStateHolder.setElectricityMeterSettings(settings.copy(currentMonthBalancing = type), profileId, remoteId)

      it.copy(currentMonthBalancing = it.currentMonthBalancing?.copy(selected = type))
    }
  }

  private fun handleChannel(channelData: ChannelDataEntity) {
    profileId = channelData.profileId
    remoteId = channelData.remoteId

    val (measuredValues) = guardLet(channelData.Electricity.measuredTypes) { return }
    val settings = userStateHolder.getElectricityMeterSettings(channelData.profileId, channelData.remoteId)

    val hasForwardEnergy = channelData.Electricity.measuredTypes.contains(SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY)
    val hasReverseEnergy = channelData.Electricity.measuredTypes.contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY)
    availableBalancingOptions.clear()
    availableBalancingOptions.addAll(
      ElectricityMeterSettings.balancingAllItems.filter {
        when (it) {
          ElectricityMeterBalanceType.VECTOR -> channelData.Electricity.hasBalance
          ElectricityMeterBalanceType.ARITHMETIC,
          ElectricityMeterBalanceType.HOURLY -> hasReverseEnergy && hasForwardEnergy
          else -> false
        }
      }
    )

    updateState { state ->
      val availableMetrics = ElectricityMeterMeasurementType.entries.filter { it.inside(measuredValues) }
      val selectedMetric = availableMetrics.select(settings.metricOnList)

      val availableAggregations = selectedMetric.aggregationOptions
      val selectedAggregation = availableAggregations.select(settings.metricOnListAggregation)

      val availableBalances = selectedMetric.balancingOptions(selectedAggregation)
      val selectedBalance = availableBalances.select(settings.metricOnListBalancing)

      state.copy(
        channelName = getCaptionUseCase(channelData.shareable),
        metricOnList = availableMetrics.selectionList(selectedMetric),
        metricOnListAggregation = availableAggregations.selectionList(selectedAggregation),
        metricOnListBalancing = availableBalances.selectionList(selectedBalance),
        currentMonthBalancing = currentMonthBalancingOptions(settings)
      )
    }
  }

  private fun List<ElectricityMeterMeasurementType>.select(item: ElectricityMeterMeasurementType): ElectricityMeterMeasurementType =
    firstOrNull { it == item } ?: firstOrNull() ?: ElectricityMeterMeasurementType.FORWARD_ACTIVE_ENERGY

  private fun List<ElectricityMeterMeasurementType>.selectionList(
    selected: ElectricityMeterMeasurementType
  ): SingleSelectionList<ElectricityMeterMeasurementType>? =
    ifTrue(size > 1) {
      SingleSelectionList(
        selected = selected,
        items = this,
        label = R.string.details_em_settings_list_metric
      )
    }

  private fun List<ListValueAggregation>.select(item: ListValueAggregation?): ListValueAggregation =
    firstOrNull { it == item } ?: firstOrNull() ?: ListValueAggregation.NO_AGGREGATION

  private fun List<ListValueAggregation>.selectionList(
    selected: ListValueAggregation
  ): SingleSelectionList<ListValueAggregation>? =
    ifTrue(size > 1) {
      SingleSelectionList(
        selected = selected,
        items = this,
        label = R.string.details_em_on_list_value
      )
    }

  private fun List<ElectricityMeterBalanceType>.select(item: ElectricityMeterBalanceType?): ElectricityMeterBalanceType =
    firstOrNull { it == item } ?: firstOrNull() ?: ElectricityMeterBalanceType.DEFAULT

  private fun List<ElectricityMeterBalanceType>.selectionList(
    selected: ElectricityMeterBalanceType
  ): SingleSelectionList<ElectricityMeterBalanceType>? =
    ifTrue(size > 1) {
      SingleSelectionList(
        selected = selected,
        items = this,
        label = R.string.details_em_on_list_balance
      )
    }

  private fun currentMonthBalancingOptions(settings: ElectricityMeterSettings): SingleSelectionList<ElectricityMeterBalanceType>? {
    return (availableBalancingOptions.size > 1).forTrue {
      SingleSelectionList(
        selected = availableBalancingOptions.firstOrNull { it == settings.currentMonthBalancing } ?: availableBalancingOptions.first(),
        items = availableBalancingOptions,
        label = R.string.details_em_last_month_balancing
      )
    }
  }

  private fun ElectricityMeterMeasurementType.balancingOptions(
    selectedAggregation: ListValueAggregation
  ): List<ElectricityMeterBalanceType> =
    if (balancingAvailable) {
      availableBalancingOptions.filter {
        // Hourly balance type is not available for current counter state (NO_AGGREGATION)
        it != ElectricityMeterBalanceType.HOURLY || selectedAggregation != ListValueAggregation.NO_AGGREGATION
      }
    } else {
      emptyList()
    }
}

sealed class ElectricityMeterSettingsViewEvent : ViewEvent
