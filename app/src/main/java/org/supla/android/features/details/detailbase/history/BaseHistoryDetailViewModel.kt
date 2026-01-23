package org.supla.android.features.details.detailbase.history
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

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataSpec
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartFilters
import org.supla.android.data.model.chart.ChartParameters
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.DefaultChartState
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.datatype.EmptyChartData
import org.supla.android.data.model.chart.hasCustomFilters
import org.supla.android.data.model.chart.style.ChartStyle
import org.supla.android.data.model.chart.style.Default
import org.supla.android.data.model.general.HideableValue
import org.supla.android.data.model.general.RangeValueType
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.beginOfNextHour
import org.supla.android.extensions.dayEnd
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.hour
import org.supla.android.extensions.monthEnd
import org.supla.android.extensions.monthStart
import org.supla.android.extensions.nextDay
import org.supla.android.extensions.quarterEnd
import org.supla.android.extensions.quarterStart
import org.supla.android.extensions.setDay
import org.supla.android.extensions.setHour
import org.supla.android.extensions.shift
import org.supla.android.extensions.subscribeBy
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.extensions.yearEnd
import org.supla.android.extensions.yearNo
import org.supla.android.extensions.yearStart
import org.supla.android.features.details.detailbase.history.ui.ChartDataSelectionDialogState
import org.supla.android.features.details.detailbase.history.ui.CheckboxItem
import org.supla.android.features.details.detailbase.history.ui.HistoryDetailScope
import org.supla.android.features.details.electricitymeterdetail.history.IntroductionPage
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.migration.GroupingStringMigrationUseCase
import org.supla.core.shared.data.model.rest.channel.ChannelDto
import org.supla.core.shared.data.model.rest.channel.DefaultChannelDto
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

abstract class BaseHistoryDetailViewModel(
  private val deleteChannelMeasurementsUseCase: DeleteChannelMeasurementsUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val groupingStringMigrationUseCase: GroupingStringMigrationUseCase,
  private val userStateHolder: UserStateHolder,
  private val profileManager: ProfileManager,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<HistoryDetailViewState, HistoryDetailViewEvent>(HistoryDetailViewState(), schedulers), HistoryDetailScope {

  open fun loadData(remoteId: Int) {
    updateState { state ->
      state.copy(
        remoteId = remoteId,
        initialLoadStarted = false,
        downloadConfigured = false,
        loading = true
      )
    }
    triggerDataLoad(remoteId)
  }

  override fun refresh() {
    updateState {
      it.copy(
        loading = true,
        initialLoadStarted = false,
        downloadConfigured = false,
        chartData = it.chartData.empty()
      )
    }
    triggerDataLoad(currentState().remoteId)
  }

  override fun showSelection(remoteId: Int, type: ChartEntryType) {
    updateState { state ->
      state.chartData.sets.firstOrNull { it.remoteId == remoteId }?.let { channelSets ->
        if (channelSets.function.hasCustomFilters()) {
          val dialogState = provideSelectionDialogState(channelSets, state.chartCustomFilters)
          if (dialogState != null) {
            return@let state.copy(chartDataSelectionDialogState = provideSelectionDialogState(channelSets, state.chartCustomFilters))
          }
        }

        if (state.chartData.onlyOneSetAndActive) {
          state // If only one active set available - disable deactivating.
        } else {
          val chartData = state.chartData.toggleActive(remoteId, type)
          if (chartData.noActiveSet) {
            state
          } else {
            state.copy(
              chartData = chartData,
              withRightAxis = chartData.sets.flatMap { it.dataSets }.firstOrNull { it.type.rightAxis() && it.active } != null,
              withLeftAxis = chartData.sets.flatMap { it.dataSets }.firstOrNull { it.type.leftAxis() && it.active } != null
            )
          }
        }
      } ?: state
    }

    updateUserState()
  }

  protected open fun cloudChannelProvider(channelWithChildren: ChannelWithChildren): Observable<ChannelDto> =
    Observable.just(DefaultChannelDto(channelWithChildren.remoteId))

  protected open fun provideSelectionDialogState(
    channelChartSets: ChannelChartSets,
    customFilters: ChartDataSpec.Filters?
  ): ChartDataSelectionDialogState? {
    return ChartDataSelectionDialogState(channelChartSets.name)
  }

  override fun hideSelection() {
    updateState { it.copy(chartDataSelectionDialogState = null) }
  }

  override fun changeFilter(spinnerItem: SpinnerItem) {
    if (spinnerItem is ChartRange) {
      changeRange(spinnerItem)
    }
    if (spinnerItem is ChartDataAggregation) {
      changeAggregation(spinnerItem)
    }
  }

  override fun confirmSelection(spinnerItem: SpinnerItem?, checkboxItems: Set<CheckboxItem>?) {
    // Used only in electricity history
  }

  private fun changeAggregation(aggregation: ChartDataAggregation) {
    updateState { state ->
      state.copy(
        filters = state.filters.select(aggregation),
        chartData = state.chartData.empty(),
        loading = true
      ).also {
        triggerMeasurementsLoad(it)
      }
    }
    updateUserState()
  }

  private fun changeRange(range: ChartRange) {
    if (range == ChartRange.CUSTOM) {
      updateState { it.copy(filters = it.filters.select(range)) }
    } else {
      updateState { state ->
        val (currentRange) = guardLet(state.range) { return@updateState state }
        val (maxDate, minDate) = guardLet(state.maxDate, state.minDate) { return@updateState state }
        val currentDate = dateProvider.currentDate()

        var rangeStart = getStartDateForRange(range, currentRange.end, currentDate, currentRange.start, minDate)
        var rangeEnd = getEndDateForRange(range, currentRange.end, currentDate, currentRange.end, maxDate)
        if (rangeStart.after(state.maxDate)) {
          rangeStart = getStartDateForRange(range, maxDate, currentDate, maxDate.dayStart(), minDate)
          rangeEnd = getEndDateForRange(range, maxDate, currentDate, maxDate.dayEnd(), maxDate)
        }

        val newDateRange = DateRange(rangeStart, rangeEnd)
        state.copy(
          filters = state.filters
            .select(range)
            .putAggregations(aggregations(newDateRange, range, state.filters.selectedAggregation, state.chartCustomFilters)),
          range = newDateRange,
          chartData = state.chartData.empty(),
          chartParameters = HideableValue(ChartParameters(1f, 1f, 0f, 0f)),
          loading = true
        ).also {
          triggerMeasurementsLoad(it)
        }
      }

      updateUserState()
    }
  }

  override fun moveRangeLeft() {
    shiftByRange(forward = false)
    updateUserState()
  }

  override fun moveRangeRight() {
    shiftByRange(forward = true)
    updateUserState()
  }

  override fun moveToDataBegin() {
    updateState { state ->
      moveToDate(state, state.minDate)
    }
    updateUserState()
  }

  override fun moveToDataEnd() {
    updateState { state ->
      moveToDate(state, state.maxDate)
    }
    updateUserState()
  }

  override fun updateChartPosition(scaleX: Float, scaleY: Float, x: Float, y: Float) {
    updateState {
      val parameters = it.chartParameters?.value?.copy(scaleX = scaleX, scaleY = scaleY, x = x, y = y)?.let { parameters ->
        HideableValue(parameters, true)
      } ?: HideableValue(ChartParameters(scaleX, scaleY, x, y))
      it.copy(chartParameters = parameters)
    }
    updateUserState()
  }

  override fun customRangeEditDate(type: RangeValueType) {
    updateState {
      it.copy(editDate = type)
    }
  }

  override fun customRangeEditHour(type: RangeValueType) {
    updateState {
      it.copy(editHour = type)
    }
  }

  override fun customRangeEditDateDismiss() {
    updateState {
      it.copy(editDate = null)
    }
  }

  override fun customRangeEditHourDismiss() {
    updateState {
      it.copy(editHour = null)
    }
  }

  override fun customRangeEditDateSave(date: Date) {
    updateState { state ->
      val dateRange = guardLet(state.range) { return@updateState state }
        .let { (range) ->
          when (state.editDate) {
            RangeValueType.START -> range.copy(start = range.start.setDay(date))
            RangeValueType.END -> range.copy(end = range.end.setDay(date))
            else -> range
          }
        }
      val (chartRange) = guardLet(state.filters.selectedRange) { return@updateState state }

      state.copy(
        range = dateRange,
        editDate = null,
        filters = state.filters.putAggregations(
          aggregations(dateRange, chartRange, state.filters.selectedAggregation, state.chartCustomFilters)
        ),
        chartData = state.chartData.empty(),
        chartParameters = HideableValue(ChartParameters(1f, 1f, 0f, 0f)),
        loading = true
      ).also {
        triggerMeasurementsLoad(it)
      }
    }

    updateUserState()
  }

  override fun customRangeEditHourSave(hour: Hour) {
    updateState { state ->
      val range = guardLet(state.range) { return@updateState state }.let { (range) ->
        when (state.editHour) {
          RangeValueType.START -> range.copy(start = range.start.setHour(hour.hour, hour.minute, 0))
          RangeValueType.END -> range.copy(end = range.end.setHour(hour.hour, hour.minute, 59))
          else -> range
        }
      }
      val (chartRange) = guardLet(state.filters.selectedRange) { return@updateState state }
      val aggregations = aggregations(range, chartRange, state.filters.selectedAggregation, state.chartCustomFilters)

      state.copy(
        range = range,
        editHour = null,
        filters = state.filters.putAggregations(aggregations),
        chartData = state.chartData.empty(),
        chartParameters = HideableValue(ChartParameters(1f, 1f, 0f, 0f)),
        loading = true
      ).also {
        triggerMeasurementsLoad(it)
      }
    }

    updateUserState()
  }

  fun deleteAndDownloadData(remoteId: Int) {
    if (currentState().downloadState is DownloadEventsManager.State.InProgress) {
      // Delete allowed only when no download in progress
      sendEvent(HistoryDetailViewEvent.ShowDownloadInProgressToast)
      return
    }

    updateState {
      it.copy(
        loading = true,
        initialLoadStarted = false,
        chartData = it.chartData.empty()
      )
    }

    deleteChannelMeasurementsUseCase.invoke(remoteId)
      .attachSilent()
      .subscribeBy(
        onComplete = { triggerDataLoad(remoteId) },
        onError = defaultErrorHandler("deleteAndDownloadData")
      )
      .disposeBySelf()
  }

  protected abstract fun handleData(channelWithChildren: ChannelWithChildren, channelDto: ChannelDto, chartState: ChartState)

  protected abstract fun measurementsMaybe(
    remoteId: Int,
    profileId: Long,
    spec: ChartDataSpec,
    chartRange: ChartRange
  ): Single<Pair<ChartData, Optional<DateRange>>>

  protected open fun loadChartState(profileId: Long, remoteId: Int): ChartState =
    userStateHolder.getDefaultChartState(profileId, remoteId)

  protected open fun exportChartState(state: HistoryDetailViewState): ChartState? {
    val (aggregation) = guardLet(state.filters.selectedAggregation) { return null }
    val (chartRange) = guardLet(state.filters.selectedRange) { return null }
    val (dateRange) = guardLet(state.range) { return null }
    val visibleSets = state.chartData.visibleSets

    return DefaultChartState(aggregation, chartRange, dateRange, state.chartParameters?.value, visibleSets)
  }

  protected fun triggerMeasurementsLoad(state: HistoryDetailViewState) {
    val (start, end) = guardLet(state.range?.start, state.range?.end) { return }
    val (remoteId) = guardLet(state.remoteId) { return }
    val (profileId) = guardLet(state.profileId) { return }
    val (chartRange) = guardLet(state.filters.selectedRange) { return }
    val aggregation = state.filters.selectedAggregation ?: ChartDataAggregation.MINUTES
    val chartDataSpec = ChartDataSpec(start, end, aggregation, state.chartCustomFilters)

    measurementsMaybe(remoteId, profileId, chartDataSpec, chartRange)
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleMeasurements(it.first, it.second, loadChartState(profileId, remoteId)) },
        onError = defaultErrorHandler("triggerMeasurementsLoad")
      )
      .disposeBySelf()
  }

  private fun triggerDataLoad(remoteId: Int) {
    Maybe.zip(
      readChannelWithChildrenUseCase(remoteId)
        .flatMap { groupingStringMigrationUseCase(it).andThen(Maybe.just(it)) },
      profileManager.getCurrentProfile().map { loadChartState(it.id, remoteId) },
    ) { first, second -> Pair(first, second) }
      .flatMap { pair ->
        try {
          cloudChannelProvider(pair.first)
            .firstElement()
            .map { Triple(pair.first, pair.second, it) }
        } catch (ex: Exception) {
          Maybe.error(ex)
        }
      }
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleData(it.first, it.third, it.second) },
        onError = { error ->
          Timber.e(error, "Subscription failed! (${this::class.java.name}:triggerDataLoad)")
          updateState { it.copy(loading = false, downloadState = DownloadEventsManager.State.Failed) }
        }
      )
      .disposeBySelf()
  }

  private fun shiftByRange(forward: Boolean) {
    updateState { state ->
      val (range) = guardLet(state.filters.selectedRange) { return@updateState state }
      state.shiftRange(range, forward).also { triggerMeasurementsLoad(it) }
    }
  }

  private fun moveToDate(state: HistoryDetailViewState, date: Date?): HistoryDetailViewState {
    val (range) = guardLet(state.filters.selectedRange) { return state }

    val rangeStart = when (range) {
      ChartRange.DAY -> date?.dayStart()
      ChartRange.WEEK -> date?.weekStart()
      ChartRange.MONTH -> date?.monthStart()
      ChartRange.QUARTER -> date?.quarterStart()
      ChartRange.YEAR -> date?.yearStart()
      else -> null
    }
    val rangeEnd = when (range) {
      ChartRange.DAY -> date?.dayEnd()
      ChartRange.WEEK -> date?.weekEnd()
      ChartRange.MONTH -> date?.monthEnd()
      ChartRange.QUARTER -> date?.quarterEnd()
      ChartRange.YEAR -> date?.yearEnd()
      else -> null
    }

    val (start, end) = guardLet(rangeStart, rangeEnd) { return state }
    val newDateRange = DateRange(start, end)

    return state.copy(
      range = newDateRange,
      chartData = state.chartData.empty()
    ).also {
      triggerMeasurementsLoad(it)
    }
  }

  protected fun restoreRange(chartState: ChartState) {
    val selectedRange = chartState.chartRange
    val chartParameters = chartState.chartParameters?.let {
      HideableValue(it)
    }

    val dateRange = when (selectedRange) {
      ChartRange.LAST_DAY -> dateProvider.currentDate().beginOfNextHour().let { DateRange(it.shift(-selectedRange.roundedDaysCount), it) }
      ChartRange.LAST_WEEK,
      ChartRange.LAST_MONTH,
      ChartRange.LAST_QUARTER -> dateProvider.currentDate().let {
        DateRange(it.shift(-selectedRange.roundedDaysCount).dayStart().nextDay(), it.dayEnd())
      }

      else -> chartState.dateRange ?: dateProvider.currentDate().let { DateRange(it.shift(-selectedRange.roundedDaysCount), it) }
    }

    updateState { state ->
      state.copy(
        filters = setupFilters(state.filters, selectedRange, dateRange, chartState, state.chartCustomFilters),
        range = dateRange,
        chartParameters = chartParameters
      )
    }
  }

  protected open fun setupFilters(
    filters: ChartFilters,
    selectedRange: ChartRange,
    dateRange: DateRange,
    chartState: ChartState,
    customFilters: ChartDataSpec.Filters?
  ) =
    filters
      .putRanges(SingleSelectionList(selectedRange, ChartRange.entries, R.string.history_range_label))
      .putAggregations(aggregations(dateRange, selectedRange, chartState.aggregation, customFilters))

  private fun handleMeasurements(chartData: ChartData, dateRange: Optional<DateRange>, chartState: ChartState) {
    updateState { state ->
      val dataWithActiveSet = chartData.activateSets(chartState.visibleSets)
      state.copy(
        // Update sets visibility
        chartData = dataWithActiveSet,
        withRightAxis = dataWithActiveSet.sets.flatMap { it.dataSets }.firstOrNull { it.type.rightAxis() && it.active } != null,
        withLeftAxis = dataWithActiveSet.sets.flatMap { it.dataSets }.firstOrNull { it.type.leftAxis() && it.active } != null,
        maxLeftAxis = dataWithActiveSet.getAxisMaxValue { it.leftAxis() },
        maxRightAxis = dataWithActiveSet.getAxisMaxValue { it.rightAxis() },
        minDate = if (dateRange.isEmpty) state.minDate else dateRange.get().start,
        maxDate = if (dateRange.isEmpty) state.maxDate else dateRange.get().end,
        range = dateRange.ifPresent { if (chartData.chartRange == ChartRange.ALL_HISTORY) it else state.range } ?: state.range,
        loading = false
      )
    }
  }

  protected fun mergeEvents(main: DownloadEventsManager.State, aux: DownloadEventsManager.State?): DownloadEventsManager.State {
    return if (aux == null) {
      main
    } else if (main == aux) {
      if (main is DownloadEventsManager.State.InProgress && main.progress > (aux as DownloadEventsManager.State.InProgress).progress) {
        aux
      } else {
        main
      }
    } else if (main.order < aux.order) {
      main
    } else {
      aux
    }
  }

  protected fun handleDownloadEvents(downloadState: DownloadEventsManager.State) {
    when (downloadState) {
      is DownloadEventsManager.State.InProgress,
      is DownloadEventsManager.State.Started -> {
        updateState { it.copy(downloadState = downloadState, loading = true) }
      }

      is DownloadEventsManager.State.Finished -> {
        updateState { state ->
          triggerMeasurementsLoad(state)
          state.copy(downloadState = downloadState)
        }
      }

      is DownloadEventsManager.State.Refresh -> {
        refresh()
      }

      else -> {
        updateState { it.copy(downloadState = downloadState, loading = false) }
      }
    }
  }

  protected open fun aggregations(
    dateRange: DateRange,
    chartRange: ChartRange,
    selectedAggregation: ChartDataAggregation? = ChartDataAggregation.MINUTES,
    customFilters: ChartDataSpec.Filters?
  ): SingleSelectionList<ChartDataAggregation> {
    val minAggregation = dateRange.minAggregation
    val maxAggregation = dateRange.maxAggregation(chartRange)
    val aggregation = if (selectedAggregation?.between(minAggregation, maxAggregation) == true) selectedAggregation else minAggregation

    return SingleSelectionList(
      selected = aggregation,
      items = allAggregations().filter { it.between(minAggregation, maxAggregation) },
      label = R.string.history_data_type
    )
  }

  protected open fun allAggregations() = ChartDataAggregation.defaultEntries

  protected fun updateUserState() {
    val state = currentState()
    val (chartState) = guardLet(exportChartState(state)) { return }

    userStateHolder.setChartState(
      state = chartState,
      profileId = state.profileId,
      remoteId = state.remoteId
    )
  }

  private fun getStartDateForRange(range: ChartRange, date: Date, currentDate: Date, dateForCustom: Date, minDate: Date) = when (range) {
    ChartRange.DAY -> date.dayStart()
    ChartRange.LAST_DAY -> currentDate.shift(-range.roundedDaysCount).beginOfNextHour()
    ChartRange.LAST_WEEK,
    ChartRange.LAST_MONTH,
    ChartRange.LAST_QUARTER,
    ChartRange.LAST_YEAR -> currentDate.shift(-range.roundedDaysCount).dayStart().nextDay()

    ChartRange.WEEK -> date.weekStart()
    ChartRange.MONTH -> date.monthStart()
    ChartRange.QUARTER -> date.quarterStart()
    ChartRange.YEAR -> date.yearStart()
    ChartRange.CUSTOM -> dateForCustom
    ChartRange.ALL_HISTORY -> minDate
  }

  private fun getEndDateForRange(range: ChartRange, end: Date, currentDate: Date, dateForCustom: Date, maxDate: Date) = when (range) {
    ChartRange.DAY -> end.dayEnd()
    ChartRange.LAST_DAY -> currentDate
    ChartRange.LAST_WEEK,
    ChartRange.LAST_MONTH,
    ChartRange.LAST_QUARTER,
    ChartRange.LAST_YEAR -> currentDate.dayEnd()

    ChartRange.WEEK -> end.weekEnd()
    ChartRange.MONTH -> end.monthEnd()
    ChartRange.QUARTER -> end.quarterEnd()
    ChartRange.YEAR -> end.yearEnd()
    ChartRange.CUSTOM -> dateForCustom
    ChartRange.ALL_HISTORY -> maxDate
  }
}

sealed class HistoryDetailViewEvent : ViewEvent {
  data object ShowDownloadInProgressToast : HistoryDetailViewEvent()
}

data class HistoryDetailViewState(
  val remoteId: Int = 0,
  val profileId: Long = 0,
  val channelFunction: Int = 0,
  val downloadConfigured: Boolean = false,
  val initialLoadStarted: Boolean = false,
  val chartData: ChartData = EmptyChartData,
  val range: DateRange? = null,
  val filters: ChartFilters = ChartFilters(),
  val loading: Boolean = false,
  val downloadState: DownloadEventsManager.State = DownloadEventsManager.State.Idle,
  val chartCustomFilters: ChartDataSpec.Filters? = null,
  val chartDataSelectionDialogState: ChartDataSelectionDialogState? = null,

  val chartStyle: ChartStyle = Default,
  val minDate: Date? = null,
  val maxDate: Date? = null,
  val withRightAxis: Boolean = false,
  val withLeftAxis: Boolean = false,
  val maxLeftAxis: Float? = null,
  val maxRightAxis: Float? = null,
  val chartParameters: HideableValue<ChartParameters>? = null,
  val showHistory: Boolean = true,

  val editDate: RangeValueType? = null,
  val editHour: RangeValueType? = null,

  val introductionPages: List<IntroductionPage>? = null
) : ViewState() {

  private val dateFormatter = DateFormatter()

  val shiftRightEnabled: Boolean
    get() {
      val (endDate, maxDate) = guardLet(range?.end, maxDate) { return false }
      return endDate < maxDate
    }

  val shiftLeftEnabled: Boolean
    get() {
      val (startDate, minDate) = guardLet(range?.start, minDate) { return false }
      return startDate > minDate
    }

  val emptyChartMessage: LocalizedString
    get() = when (downloadState) {
      is DownloadEventsManager.State.Started -> localizedString(R.string.history_refreshing)

      is DownloadEventsManager.State.InProgress -> {
        val percentage = ValuesFormatter.getPercentageString(downloadState.progress)
        localizedString("%s %s", localizedString(R.string.retrieving_data_from_the_server), percentage)
      }

      is DownloadEventsManager.State.Finished -> {
        if (loading) {
          localizedString(R.string.history_refreshing)
        } else if (chartData.sets.firstOrNull { it.active } == null) {
          localizedString(R.string.history_no_data_selected)
        } else if (minDate == null && maxDate == null) {
          localizedString(R.string.history_no_data_available)
        } else if (range?.start?.after(range.end) == true) {
          localizedString(R.string.history_wrong_range)
        } else {
          localizedString(R.string.no_chart_data_available)
        }
      }

      is DownloadEventsManager.State.Failed -> localizedString(R.string.history_refreshing_failed)
      else -> {
        if (loading.not() && chartData.sets.isEmpty()) {
          localizedString(R.string.history_no_data_available)
        } else {
          localizedString(R.string.retrieving_data_from_the_server)
        }
      }
    }

  val allowNavigation: Boolean
    get() = when (filters.selectedRange) {
      ChartRange.DAY,
      ChartRange.WEEK,
      ChartRange.MONTH,
      ChartRange.QUARTER,
      ChartRange.YEAR -> true

      else -> false
    }

  val editDateValue: Date?
    get() = when (editDate) {
      RangeValueType.START -> range?.start
      RangeValueType.END -> range?.end
      else -> null
    }

  val editHourValue: Hour?
    get() = when (editHour) {
      RangeValueType.START -> range?.start?.hour()
      RangeValueType.END -> range?.end?.hour()
      else -> null
    }

  val yearRange: IntRange?
    get() {
      val (min, max) = guardLet(minDate, maxDate) { return null }
      return IntRange(min.yearNo, max.yearNo)
    }

  val rangeText: String?
    get() {
      val (dateRange) = guardLet(range) { return null }
      val (chartRange) = guardLet(filters.selectedRange) { return null }

      return when (chartRange) {
        ChartRange.DAY -> dateFormatter.getDateString(dateRange.start)
        ChartRange.LAST_DAY -> weekdayAndHourString(dateRange)

        ChartRange.LAST_WEEK,
        ChartRange.LAST_MONTH -> dayAndHourString(dateRange)

        ChartRange.WEEK,
        ChartRange.LAST_QUARTER,
        ChartRange.QUARTER,
        ChartRange.LAST_YEAR -> dateString(dateRange)

        ChartRange.MONTH -> dateFormatter.getMonthAndYearString(dateRange.start)?.capitalize(Locale.current)
        ChartRange.YEAR -> dateFormatter.getYearString(dateRange.start)

        ChartRange.CUSTOM,
        ChartRange.ALL_HISTORY -> longDateString(dateRange)
      }
    }

  internal fun shiftRange(chartRange: ChartRange, forward: Boolean): HistoryDetailViewState {
    val newChartRange = when (chartRange) {
      ChartRange.DAY,
      ChartRange.WEEK,
      ChartRange.MONTH,
      ChartRange.QUARTER,
      ChartRange.YEAR,
      ChartRange.CUSTOM -> chartRange

      else -> ChartRange.CUSTOM
    }
    return copy(
      filters = filters.select(newChartRange),
      range = range?.shift(chartRange, forward),
      chartData = chartData.empty()
    )
  }

  fun editDayValidator(date: Date): Boolean {
    val localDate = Date(date.time - TimeZone.getTimeZone(Calendar.getInstance().timeZone.id).getOffset(date.time))
    val (min, max) = guardLet(minDate, maxDate) { return true }
    val (range) = guardLet(range) { return min.before(localDate) && max.after(localDate) }

    return when (editDate) {
      RangeValueType.START -> min.before(localDate) && range.end.after(localDate)
      RangeValueType.END -> range.start.before(localDate.dayEnd()) && max.after(localDate)
      else -> min.before(localDate) && max.after(localDate)
    }
  }

  private fun weekdayAndHourString(range: DateRange): String {
    val rangeStart = dateFormatter.getDayHourDateString(range.start)
    val rangeEnd = dateFormatter.getDayHourDateString(range.end)
    return "$rangeStart - $rangeEnd"
  }

  private fun dayAndHourString(range: DateRange): String {
    val rangeStart = dateFormatter.getDayAndHourDateString(range.start)
    val rangeEnd = dateFormatter.getDayAndHourDateString(range.end)
    return "$rangeStart - $rangeEnd"
  }

  private fun dateString(range: DateRange): String {
    val startString = dateFormatter.getShortDateString(range.start)
    val endString = dateFormatter.getShortDateString(range.end)
    return "$startString - $endString"
  }

  private fun longDateString(range: DateRange): String {
    val startString = dateFormatter.getFullDateString(range.start)
    val endString = dateFormatter.getFullDateString(range.end)
    return "$startString - $endString"
  }
}
