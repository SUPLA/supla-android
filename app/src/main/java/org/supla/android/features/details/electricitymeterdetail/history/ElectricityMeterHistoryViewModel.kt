package org.supla.android.features.details.electricitymeterdetail.history
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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.ChannelSets
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.ElectricityChartState
import org.supla.android.data.model.chart.datatype.BarChartData
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.general.MultipleSelectionList
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.detailbase.history.BaseHistoryDetailViewModel
import org.supla.android.features.details.detailbase.history.HistoryDetailViewState
import org.supla.android.features.details.detailbase.history.ui.CheckboxItem
import org.supla.android.features.details.detailbase.history.ui.DataSelectionDialogState
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.SpinnerItem
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsDataRangeUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.measurementsprovider.ElectricityChartFilters
import org.supla.android.usecases.channel.measurementsprovider.PhaseItem
import javax.inject.Inject

private val DEFAULT_TYPE = ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY
private val DEFAULT_PHASES = PhaseItem.ALL.toSet()

@HiltViewModel
class ElectricityMeterHistoryViewModel @Inject constructor(
  private val loadChannelMeasurementsDataRangeUseCase: LoadChannelMeasurementsDataRangeUseCase,
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val loadChannelMeasurementsUseCase: LoadChannelMeasurementsUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val userStateHolder: UserStateHolder,
  private val profileManager: ProfileManager,
  deleteChannelMeasurementsUseCase: DeleteChannelMeasurementsUseCase,
  dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseHistoryDetailViewModel(deleteChannelMeasurementsUseCase, userStateHolder, dateProvider, schedulers) {

  override fun provideSelectionDialogState(channelSets: ChannelSets, customFilters: ChartDataSpec.Filters?): DataSelectionDialogState {
    val selectedType = (customFilters as? ElectricityChartFilters)?.type ?: DEFAULT_TYPE
    val selectedPhases = (customFilters as? ElectricityChartFilters)?.phases ?: DEFAULT_PHASES

    return DataSelectionDialogState(
      channelName = channelSets.name,
      spinner = SingleSelectionList(selectedType, ElectricityMeterChartType.entries, R.string.details_em_chart_data_type),
      checkbox = getCheckboxOptions(selectedType, selectedPhases),
      checkboxSelector = { type, checkboxes ->
        getCheckboxOptions(type, checkboxes?.filterIsInstance<PhaseItem>()?.toSet() ?: DEFAULT_PHASES)
      }
    )
  }

  override fun confirmSelection(spinnerItem: SpinnerItem?, checkboxItems: Set<CheckboxItem>?) {
    val item = (spinnerItem as? ElectricityMeterChartType) ?: DEFAULT_TYPE
    val items = checkboxItems?.filterIsInstance<PhaseItem>()?.toSet() ?: DEFAULT_PHASES

    updateState {
      it.copy(
        chartCustomFilters = ElectricityChartFilters(item, items),
        dataSelectionDialogState = null,
        loading = true,
        initialLoadStarted = false,
        chartData = it.chartData.empty()
      )
    }
    updateUserState()

    triggerDataLoad(currentState().remoteId)
  }

  override fun loadChartState(profileId: Long, remoteId: Int): ChartState =
    userStateHolder.getChartState(profileId, remoteId) { gson, string -> gson.fromJson(string, ElectricityChartState::class.java) }
      ?: ElectricityChartState.default()

  override fun exportChartState(state: HistoryDetailViewState): ChartState? {
    val (aggregation) = guardLet(state.filters.selectedAggregation) { return null }
    val (chartRange) = guardLet(state.filters.selectedRange) { return null }
    val (dateRange) = guardLet(state.range) { return null }

    return ElectricityChartState(
      aggregation,
      chartRange,
      dateRange,
      state.chartParameters?.value,
      state.chartData.visibleSets,
      state.chartCustomFilters as? ElectricityChartFilters
    )
  }

  override fun triggerDataLoad(remoteId: Int) {
    Maybe.zip(
      readChannelByRemoteIdUseCase(remoteId),
      profileManager.getCurrentProfile().map { loadChartState(it.id, remoteId) }
    ) { first, second -> Pair(first, second) }
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleData(it.first, it.second) }
      )
      .disposeBySelf()
  }

  override fun measurementsMaybe(
    remoteId: Int,
    profileId: Long,
    spec: ChartDataSpec,
    chartRange: ChartRange
  ): Single<Pair<ChartData, Optional<DateRange>>> =
    Single.zip(
      loadChannelMeasurementsUseCase(remoteId, spec),
      loadChannelMeasurementsDataRangeUseCase(remoteId, profileId)
    ) { first, second -> Pair(BarChartData(DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, listOf(first)), second) }

  private fun handleData(channel: ChannelDataEntity, chartState: ChartState) {
    updateState { it.copy(profileId = channel.profileId, channelFunction = channel.function) }

    restoreRange(chartState)
    restoreCustomFilters(chartState)
    configureDownloadObserver(channel.remoteId)
    startInitialDataLoad(channel.remoteId, channel.profileId, channel.function)
  }

  private fun startInitialDataLoad(remoteId: Int, profileId: Long, channelFunction: Int) {
    if (currentState().initialLoadStarted) {
      // Needs to be performed only once
      return
    }
    updateState { it.copy(initialLoadStarted = true) }
    downloadChannelMeasurementsUseCase.invoke(remoteId, profileId, channelFunction)
  }

  private fun configureDownloadObserver(remoteId: Int) {
    if (currentState().downloadConfigured) {
      // Needs to be performed only once
      return
    }
    updateState { it.copy(downloadConfigured = true) }

    downloadEventsManager.observeProgress(remoteId).attachSilent()
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { handleDownloadEvents(it) },
        onError = defaultErrorHandler("configureDownloadObserver")
      )
      .disposeBySelf()
  }

  private fun getCheckboxOptions(
    type: SpinnerItem,
    selectedPhases: Set<PhaseItem> = DEFAULT_PHASES
  ): MultipleSelectionList<CheckboxItem>? =
    when (type) {
      ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY,
      ElectricityMeterChartType.REVERSED_ACTIVE_ENERGY,
      ElectricityMeterChartType.FORWARDED_REACTIVE_ENERGY,
      ElectricityMeterChartType.REVERSED_REACTIVE_ENERGY ->
        MultipleSelectionList(selectedPhases, PhaseItem.ALL, R.string.details_em_phases)

      else -> null
    }

  private fun restoreCustomFilters(state: ChartState) {
    val (electricityState) = guardLet(state as? ElectricityChartState) { return }
    updateState { it.copy(chartCustomFilters = electricityState.customFilters) }
  }
}
