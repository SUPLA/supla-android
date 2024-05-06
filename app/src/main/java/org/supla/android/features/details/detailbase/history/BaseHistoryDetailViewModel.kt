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

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.Optional
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartParameters
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.chart.TemperatureChartState
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.datatype.EmptyChartData
import org.supla.android.data.model.general.HideableValue
import org.supla.android.data.model.general.RangeValueType
import org.supla.android.data.model.general.SelectableList
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.dayEnd
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.hour
import org.supla.android.extensions.monthEnd
import org.supla.android.extensions.monthStart
import org.supla.android.extensions.quarterEnd
import org.supla.android.extensions.quarterStart
import org.supla.android.extensions.setDay
import org.supla.android.extensions.setHour
import org.supla.android.extensions.shift
import org.supla.android.extensions.valuesFormatter
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.extensions.yearEnd
import org.supla.android.extensions.yearNo
import org.supla.android.extensions.yearStart
import org.supla.android.features.details.detailbase.history.ui.HistoryDetailProxy
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.DeleteChannelMeasurementsUseCase
import java.util.Date

abstract class BaseHistoryDetailViewModel(
  private val deleteChannelMeasurementsUseCase: DeleteChannelMeasurementsUseCase,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<HistoryDetailViewState, HistoryDetailViewEvent>(HistoryDetailViewState(), schedulers), HistoryDetailProxy {

  open fun loadData(remoteId: Int) {
    updateState { state ->
      state.copy(remoteId = remoteId, loading = true)
    }
    triggerDataLoad(remoteId)
  }

  override fun refresh() {
    updateState {
      it.copy(
        loading = true,
        initialLoadStarted = false,
        chartData = it.chartData.empty()
      )
    }
    triggerDataLoad(currentState().remoteId)
  }

  override fun changeSetActive(setId: HistoryDataSet.Id) {
    updateState { state ->
      val chartData = state.chartData.activateSet(setId)

      state.copy(
        chartData = chartData,
        withRightAxis = chartData.sets.firstOrNull { it.setId.type.rightAxis() && it.active } != null,
        withLeftAxis = chartData.sets.firstOrNull { it.setId.type.leftAxis() && it.active } != null
      )
    }
    updateUserState()
  }

  override fun changeRange(range: ChartRange) {
    if (range == ChartRange.CUSTOM) {
      updateState { it.copy(ranges = it.ranges?.copy(selected = range)) }
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
          ranges = state.ranges?.copy(selected = range),
          range = newDateRange,
          aggregations = aggregations(newDateRange, state.aggregations?.selected),
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

  override fun changeAggregation(aggregation: ChartDataAggregation) {
    updateState { state ->
      state.copy(
        aggregations = state.aggregations?.copy(selected = aggregation),
        chartData = state.chartData.empty(),
        loading = true
      ).also {
        triggerMeasurementsLoad(it)
      }
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
      val range = guardLet(state.range) { return@updateState state }.let { (range) ->
        when (state.editDate) {
          RangeValueType.START -> range.copy(start = range.start.setDay(date))
          RangeValueType.END -> range.copy(end = range.end.setDay(date))
          else -> range
        }
      }

      state.copy(
        range = range,
        editDate = null,
        aggregations = aggregations(range, state.aggregations?.selected),
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

      state.copy(
        range = range,
        editHour = null,
        aggregations = aggregations(range, state.aggregations?.selected),
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

  protected abstract fun triggerDataLoad(remoteId: Int)

  protected abstract fun measurementsMaybe(
    remoteId: Int,
    profileId: Long,
    start: Date,
    end: Date,
    chartRange: ChartRange,
    aggregation: ChartDataAggregation
  ): Single<Pair<ChartData, Optional<DateRange>>>

  protected fun triggerMeasurementsLoad(state: HistoryDetailViewState) {
    val (start, end) = guardLet(state.range?.start, state.range?.end) { return }
    val (remoteId) = guardLet(state.remoteId) { return }
    val (profileId) = guardLet(state.profileId) { return }
    val (chartRange) = guardLet(state.ranges?.selected) { return }
    val aggregation = state.aggregations?.selected ?: ChartDataAggregation.MINUTES

    measurementsMaybe(remoteId, profileId, start, end, chartRange, aggregation)
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleMeasurements(it.first, it.second, userStateHolder.getChartState(profileId, remoteId)) },
        onError = defaultErrorHandler("triggerMeasurementsLoad")
      )
      .disposeBySelf()
  }

  private fun shiftByRange(forward: Boolean) {
    updateState { state ->
      val (range) = guardLet(state.ranges?.selected) { return@updateState state }
      state.shiftRange(range, forward).also { triggerMeasurementsLoad(it) }
    }
  }

  private fun moveToDate(state: HistoryDetailViewState, date: Date?): HistoryDetailViewState {
    val (range) = guardLet(state.ranges?.selected) { return state }

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

  protected fun restoreRange(chartState: TemperatureChartState) {
    val selectedRange = chartState.chartRange
    val chartParameters = chartState.chartParameters?.let {
      HideableValue(it)
    }

    val dateRange = when (selectedRange) {
      ChartRange.LAST_DAY,
      ChartRange.LAST_WEEK,
      ChartRange.LAST_MONTH,
      ChartRange.LAST_QUARTER -> dateProvider.currentDate().let { DateRange(it.shift(-selectedRange.roundedDaysCount), it) }

      else -> chartState.dateRange ?: dateProvider.currentDate().let { DateRange(it.shift(-selectedRange.roundedDaysCount), it) }
    }

    updateState { state ->
      state.copy(
        ranges = SelectableList(selectedRange, ChartRange.values().toList()),
        aggregations = aggregations(dateRange, chartState.aggregation),
        range = dateRange,
        chartParameters = chartParameters
      )
    }
  }

  private fun handleMeasurements(chartData: ChartData, dateRange: Optional<DateRange>, chartState: TemperatureChartState) {
    updateState { state ->
      val dataWithActiveSet = chartData.activateSets(chartState.visibleSets)
      state.copy(
        // Update sets visibility
        chartData = dataWithActiveSet,
        withRightAxis = dataWithActiveSet.sets.firstOrNull { it.setId.type.rightAxis() && it.active } != null,
        withLeftAxis = dataWithActiveSet.sets.firstOrNull { it.setId.type.leftAxis() && it.active } != null,
        maxLeftAxis = dataWithActiveSet.getAxisMaxValue { it.leftAxis() },
        maxRightAxis = dataWithActiveSet.getAxisMaxValue { it.rightAxis() },
        minDate = if (dateRange.isEmpty) state.minDate else dateRange.get().start,
        maxDate = if (dateRange.isEmpty) state.maxDate else dateRange.get().end,
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

  private fun aggregations(
    currentRange: DateRange,
    selectedAggregation: ChartDataAggregation? = ChartDataAggregation.MINUTES
  ): SelectableList<ChartDataAggregation> {
    val minAggregation = currentRange.minAggregation
    val maxAggregation = currentRange.maxAggregation
    val aggregation = if (selectedAggregation?.between(minAggregation, maxAggregation) == true) selectedAggregation else minAggregation

    return SelectableList(
      selected = aggregation,
      items = ChartDataAggregation.values().filter { it.between(minAggregation, maxAggregation) }
    )
  }

  private fun updateUserState() {
    val state = currentState()
    val (aggregation) = guardLet(state.aggregations?.selected) { return }
    val (chartRange) = guardLet(state.ranges?.selected) { return }
    val (dateRange) = guardLet(state.range) { return }
    val visibleSets = state.chartData.sets.filter { it.active }.map { it.setId }

    userStateHolder.setChartState(
      state = TemperatureChartState(aggregation, chartRange, dateRange, state.chartParameters?.value, visibleSets),
      profileId = state.profileId,
      remoteId = state.remoteId
    )
  }

  private fun getStartDateForRange(range: ChartRange, date: Date, currentDate: Date, dateForCustom: Date, minDate: Date) = when (range) {
    ChartRange.DAY -> date.dayStart()
    ChartRange.LAST_DAY,
    ChartRange.LAST_WEEK,
    ChartRange.LAST_MONTH,
    ChartRange.LAST_QUARTER -> currentDate.shift(-range.roundedDaysCount)

    ChartRange.WEEK -> date.weekStart()
    ChartRange.MONTH -> date.monthStart()
    ChartRange.QUARTER -> date.quarterStart()
    ChartRange.YEAR -> date.yearStart()
    ChartRange.CUSTOM -> dateForCustom
    ChartRange.ALL_HISTORY -> minDate
  }

  private fun getEndDateForRange(range: ChartRange, end: Date, currentDate: Date, dateForCustom: Date, maxDate: Date) = when (range) {
    ChartRange.DAY -> end.dayEnd()
    ChartRange.LAST_DAY,
    ChartRange.LAST_WEEK,
    ChartRange.LAST_MONTH,
    ChartRange.LAST_QUARTER -> currentDate

    ChartRange.WEEK -> end.weekEnd()
    ChartRange.MONTH -> end.monthEnd()
    ChartRange.QUARTER -> end.quarterEnd()
    ChartRange.YEAR -> end.yearEnd()
    ChartRange.CUSTOM -> dateForCustom
    ChartRange.ALL_HISTORY -> maxDate
  }
}

sealed class HistoryDetailViewEvent : ViewEvent {
  object ShowDownloadInProgressToast : HistoryDetailViewEvent()
}

data class HistoryDetailViewState(
  val remoteId: Int = 0,
  val profileId: Long = 0,
  val channelFunction: Int = 0,
  val downloadConfigured: Boolean = false,
  val initialLoadStarted: Boolean = false,
  val chartData: ChartData = EmptyChartData,
  val range: DateRange? = null,
  val ranges: SelectableList<ChartRange>? = null,
  val aggregations: SelectableList<ChartDataAggregation>? = null,
  val loading: Boolean = false,
  val downloadState: DownloadEventsManager.State = DownloadEventsManager.State.Idle,

  val minDate: Date? = null,
  val maxDate: Date? = null,
  val withRightAxis: Boolean = false,
  val withLeftAxis: Boolean = false,
  val maxLeftAxis: Float? = null,
  val maxRightAxis: Float? = null,
  val chartParameters: HideableValue<ChartParameters>? = null,
  val showHistory: Boolean = true,

  val editDate: RangeValueType? = null,
  val editHour: RangeValueType? = null
) : ViewState() {

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

  val emptyChartMessage: StringProvider
    get() = when (downloadState) {
      is DownloadEventsManager.State.Started -> { context -> context.getString(R.string.history_refreshing) }

      is DownloadEventsManager.State.InProgress -> { context ->
        val description = context.getString(R.string.retrieving_data_from_the_server)
        val percentage = context.valuesFormatter.getPercentageString(downloadState.progress)
        "$description $percentage"
      }

      is DownloadEventsManager.State.Finished -> { context ->
        if (loading) {
          context.getString(R.string.history_refreshing)
        } else if (chartData.sets.firstOrNull { it.active } == null) {
          context.getString(R.string.history_no_data_selected)
        } else if (minDate == null && maxDate == null) {
          context.getString(R.string.history_no_data_available)
        } else if (range?.start?.after(range.end) == true) {
          context.getString(R.string.history_wrong_range)
        } else {
          context.getString(R.string.no_chart_data_available)
        }
      }

      is DownloadEventsManager.State.Failed -> { context -> context.getString(R.string.history_refreshing_failed) }
      else -> { context ->
        if (loading.not() && chartData.sets.isEmpty()) {
          context.getString(R.string.history_no_data_available)
        } else {
          context.getString(R.string.retrieving_data_from_the_server)
        }
      }
    }

  val showBottomBar: Boolean
    get() = when (ranges?.selected) {
      ChartRange.DAY,
      ChartRange.WEEK,
      ChartRange.MONTH,
      ChartRange.QUARTER,
      ChartRange.YEAR,
      ChartRange.ALL_HISTORY -> true

      else -> false
    }

  val allowNavigation: Boolean
    get() = when (ranges?.selected) {
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

  fun rangesMap(resources: Resources): Map<ChartRange, String> =
    mutableMapOf<ChartRange, String>().also { map ->
      ranges?.items?.forEach {
        map[it] = resources.getString(it.stringRes)
      }
    }

  fun aggregationsMap(resources: Resources): Map<ChartDataAggregation, String> =
    mutableMapOf<ChartDataAggregation, String>().also { map ->
      aggregations?.items?.forEach {
        map[it] = resources.getString(it.stringRes)
      }
    }

  fun rangeText(context: Context): String? {
    val (dateRange) = guardLet(range) { return null }
    val (chartRange) = guardLet(ranges?.selected) { return null }

    return when (chartRange) {
      ChartRange.DAY -> context.valuesFormatter.getDateString(dateRange.start)
      ChartRange.LAST_DAY -> weekdayAndHourString(context, dateRange)

      ChartRange.LAST_WEEK,
      ChartRange.LAST_MONTH -> dayAndHourString(context, dateRange)

      ChartRange.WEEK,
      ChartRange.LAST_QUARTER,
      ChartRange.QUARTER -> dateString(context, dateRange)

      ChartRange.MONTH -> context.valuesFormatter.getMonthAndYearString(dateRange.start)?.capitalize(Locale.current)
      ChartRange.YEAR -> context.valuesFormatter.getYearString(dateRange.start)

      ChartRange.CUSTOM,
      ChartRange.ALL_HISTORY -> longDateString(context, dateRange)
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
      ranges = ranges?.copy(selected = newChartRange),
      range = range?.shift(chartRange, forward),
      chartData = chartData.empty()
    )
  }

  fun editDayValidator(date: Date): Boolean {
    val (min, max) = guardLet(minDate, maxDate) { return true }
    val (range) = guardLet(range) { return min.before(date) && max.after(date) }

    return when (editDate) {
      RangeValueType.START -> min.before(date) && range.end.after(date)
      RangeValueType.END -> range.start.before(date) && max.after(date)
      else -> min.before(date) && max.after(date)
    }
  }

  private fun weekdayAndHourString(context: Context, range: DateRange): String {
    val rangeStart = context.valuesFormatter.getDayHourDateString(range.start)
    val rangeEnd = context.valuesFormatter.getDayHourDateString(range.end)
    return "$rangeStart - $rangeEnd"
  }

  private fun dayAndHourString(context: Context, range: DateRange): String {
    val rangeStart = context.valuesFormatter.getDayAndHourDateString(range.start)
    val rangeEnd = context.valuesFormatter.getDayAndHourDateString(range.end)
    return "$rangeStart - $rangeEnd"
  }

  private fun dateString(context: Context, range: DateRange): String {
    val startString = context.valuesFormatter.getShortDateString(range.start)
    val endString = context.valuesFormatter.getShortDateString(range.end)
    return "$startString - $endString"
  }

  private fun longDateString(context: Context, range: DateRange): String {
    val startString = context.valuesFormatter.getFullDateString(range.start)
    val endString = context.valuesFormatter.getFullDateString(range.end)
    return "$startString - $endString"
  }
}
