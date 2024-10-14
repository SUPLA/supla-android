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
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.ElectricityChartState
import org.supla.android.data.model.chart.datatype.BarChartData
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.datatype.PieChartData
import org.supla.android.data.model.chart.style.ChartStyle
import org.supla.android.data.model.chart.style.ElectricityChartStyle
import org.supla.android.data.model.general.MultipleSelectionList
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.Electricity
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.data.source.remote.channel.suplaFlags
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifTrue
import org.supla.android.features.details.detailbase.history.BaseHistoryDetailViewModel
import org.supla.android.features.details.detailbase.history.HistoryDetailViewState
import org.supla.android.features.details.detailbase.history.ui.ChartDataSelectionDialogState
import org.supla.android.features.details.detailbase.history.ui.CheckboxItem
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.SpinnerItem
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsDataRangeUseCase
import org.supla.android.usecases.channel.LoadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.channel.measurementsprovider.electricity.ElectricityChartFilters
import org.supla.android.usecases.channel.measurementsprovider.electricity.PhaseItem
import javax.inject.Inject

private val DEFAULT_PHASES = PhaseItem.entries.toSet()

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

  override fun provideSelectionDialogState(
    channelChartSets: ChannelChartSets,
    customFilters: ChartDataSpec.Filters?
  ): ChartDataSelectionDialogState? {
    val (electricityChartFilters) = guardLet(customFilters as? ElectricityChartFilters) { return null }
    val availableTypes = electricityChartFilters.availableTypes.toList()
    val selectedType = electricityChartFilters.type
    val availablePhases = electricityChartFilters.availablePhases
    val selectedPhases = electricityChartFilters.selectedPhases

    if (availableTypes.size == 1 && availablePhases.size <= 1) {
      return null
    }

    return ChartDataSelectionDialogState(
      channelName = channelChartSets.name,
      spinner = SingleSelectionList(selectedType, availableTypes, R.string.details_em_chart_data_type),
      checkbox = getCheckboxOptions(selectedType, availablePhases, selectedPhases),
      checkboxSelector = { type, checkboxes ->
        getCheckboxOptions(type, availablePhases, checkboxes?.filterIsInstance<PhaseItem>()?.toSet() ?: DEFAULT_PHASES)
      }
    )
  }

  override fun confirmSelection(spinnerItem: SpinnerItem?, checkboxItems: Set<CheckboxItem>?) {
    val (type) = guardLet((spinnerItem as? ElectricityMeterChartType)) { return }
    val phases = checkboxItems?.filterIsInstance<PhaseItem>()?.toSet() ?: emptySet()

    updateState { state ->
      val (dateRange) = guardLet(state.range) { return@updateState state }
      val (chartRange) = guardLet(state.filters.selectedRange) { return@updateState state }

      (state.chartCustomFilters as? ElectricityChartFilters)?.let {
        val customFilters = state.chartCustomFilters.copy(type = type, selectedPhases = phases)
        state.copy(
          chartCustomFilters = customFilters,
          chartDataSelectionDialogState = null,
          loading = true,
          initialLoadStarted = false,
          filters = state.filters.putAggregations(aggregations(dateRange, chartRange, state.filters.selectedAggregation, customFilters)),
          chartData = state.chartData.empty()
        )
      } ?: state
    }
    updateUserState()

    triggerMeasurementsLoad(currentState())
  }

  override fun loadChartState(profileId: Long, remoteId: Int): ChartState =
    userStateHolder.getElectricityChartState(profileId, remoteId)
      // currently there is only one set and has to be always visible, should be removed when chart comparisons added
      // If not set to null, set will be displayed as not active
      .copy(visibleSets = null)

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
    ) { first, second -> Pair(getChartData(spec, chartRange, first), second) }

  override fun chartStyle(): ChartStyle = ElectricityChartStyle

  override fun aggregations(
    dateRange: DateRange,
    chartRange: ChartRange,
    selectedAggregation: ChartDataAggregation?,
    customFilters: ChartDataSpec.Filters?
  ): SingleSelectionList<ChartDataAggregation> {
    val (filters) = guardLet(customFilters as? ElectricityChartFilters) {
      return super.aggregations(dateRange, chartRange, selectedAggregation, customFilters)
    }

    val aggregations = super.aggregations(dateRange, chartRange, selectedAggregation, customFilters)
      .let {
        filters.type.isBalance.ifTrue {
          // In balance charts no ranking is available
          val aggregations = it.items.filter { aggregation -> !aggregation.isRank }
          it.copy(
            selected = if (it.selected.isRank) aggregations.first() else it.selected,
            items = aggregations
          )
        } ?: it
      }
    return if (filters.type == ElectricityMeterChartType.BALANCE_HOURLY && aggregations.items.contains(ChartDataAggregation.MINUTES)) {
      aggregations.copy(
        selected = if (aggregations.selected == ChartDataAggregation.MINUTES) ChartDataAggregation.HOURS else aggregations.selected,
        items = aggregations.items.minus(ChartDataAggregation.MINUTES)
      )
    } else {
      aggregations
    }
  }

  override fun allAggregations() = ChartDataAggregation.entries

  private fun handleData(channel: ChannelDataEntity, chartState: ChartState) {
    updateState { it.copy(profileId = channel.profileId, channelFunction = channel.function.value) }

    restoreCustomFilters(channel.flags.suplaFlags, channel.Electricity.value, chartState)
    restoreRange(chartState)
    configureDownloadObserver(channel.remoteId)
    startInitialDataLoad(channel.remoteId, channel.profileId, channel.function.value)
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
    availablePhases: Set<PhaseItem>,
    selectedPhases: Set<PhaseItem> = availablePhases
  ): MultipleSelectionList<CheckboxItem> {
    val allPhases = if ((type as? ElectricityMeterChartType)?.needsPhases == true) PhaseItem.entries.toSet() else emptySet()

    val disabledPhases = when (type) {
      ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY,
      ElectricityMeterChartType.REVERSED_ACTIVE_ENERGY,
      ElectricityMeterChartType.FORWARDED_REACTIVE_ENERGY,
      ElectricityMeterChartType.REVERSED_REACTIVE_ENERGY -> allPhases.minus(availablePhases)

      else -> allPhases
    }
    return if (selectedPhases.isEmpty()) {
      MultipleSelectionList(allPhases.minus(disabledPhases), allPhases, R.string.details_em_phases, disabledPhases)
    } else {
      MultipleSelectionList(selectedPhases, allPhases, R.string.details_em_phases, disabledPhases)
    }
  }

  private fun restoreCustomFilters(flags: List<SuplaChannelFlag>, value: SuplaChannelElectricityMeterValue?, state: ChartState) =
    updateState { it.copy(chartCustomFilters = ElectricityChartFilters.restore(flags, value, state)) }

  private fun getChartData(spec: ChartDataSpec, chartRange: ChartRange, sets: ChannelChartSets): ChartData {
    return if (spec.aggregation.isRank) {
      PieChartData(DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, listOf(sets))
    } else {
      BarChartData(DateRange(spec.startDate, spec.endDate), chartRange, spec.aggregation, listOf(sets))
    }
  }
}
