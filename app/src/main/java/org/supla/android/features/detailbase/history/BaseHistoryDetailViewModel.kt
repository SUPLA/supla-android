package org.supla.android.features.detailbase.history
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
import androidx.annotation.ColorRes
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
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
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartParameters
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.chart.TemperatureChartState
import org.supla.android.data.model.general.HideableValue
import org.supla.android.data.model.general.SelectableList
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.dayEnd
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.monthEnd
import org.supla.android.extensions.monthStart
import org.supla.android.extensions.quarterEnd
import org.supla.android.extensions.quarterStart
import org.supla.android.extensions.shift
import org.supla.android.extensions.toPx
import org.supla.android.extensions.toTimestamp
import org.supla.android.extensions.valuesFormatter
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.extensions.yearEnd
import org.supla.android.extensions.yearStart
import org.supla.android.features.calendarpicker.CalendarRangePickerState
import org.supla.android.features.detailbase.history.ui.HistoryDetailProxy
import org.supla.android.tools.SuplaSchedulers
import java.util.Date

abstract class BaseHistoryDetailViewModel(
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<HistoryDetailViewState, HistoryDetailViewEvent>(HistoryDetailViewState(), schedulers), HistoryDetailProxy {

  fun loadData(remoteId: Int) {
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
        sets = it.sets.map { set -> set.copy(entries = emptyList()) }
      )
    }
    triggerDataLoad(currentState().remoteId)
  }

  override fun changeSetActive(setId: HistoryDataSet.Id) {
    updateState { state ->
      state.copy(
        sets = state.sets.map {
          if (it.setId == setId) {
            it.copy(active = it.active.not())
          } else {
            it
          }
        }
      )
    }
    updateUserState()
  }

  override fun changeRange(range: ChartRange) {
    if (range == ChartRange.CUSTOM) {
      openCustomDateSelectionDialog()
    } else {
      updateState { state ->
        val (currentRange) = guardLet(state.range) { return@updateState state }
        val (maxDate) = guardLet(state.maxDate) { return@updateState state }
        val currentDate = dateProvider.currentDate()

        var rangeStart = getStartDateForRange(range, currentRange.end, currentDate, currentRange.start)
        var rangeEnd = getEndDateForRange(range, currentRange.end, currentDate, currentRange.end)
        if (rangeStart.after(state.maxDate)) {
          rangeStart = getStartDateForRange(range, maxDate, currentDate, maxDate.dayStart())
          rangeEnd = getEndDateForRange(range, maxDate, currentDate, maxDate.dayEnd())
        }

        val newDateRange = DateRange(rangeStart, rangeEnd)
        state.copy(
          ranges = state.ranges?.copy(selected = range),
          range = newDateRange,
          aggregations = aggregations(newDateRange, state.aggregations?.selected),
          sets = state.sets.map { set -> set.copy(entries = emptyList()) },
          chartParameters = HideableValue(ChartParameters(1f, 1f, 0f, 0f))
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
      state.copy(aggregations = state.aggregations?.copy(selected = aggregation)).also {
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

  override fun dateRangeCancelSelection() {
    updateState {
      it.copy(datePickerState = null)
    }
  }

  override fun dateRangeDaySelection(startDate: Date?, endDate: Date?) {
    val (start) = guardLet(startDate) {
      updateState { it.copy(datePickerState = it.datePickerState?.copy(selectedRange = null)) }
      return
    }

    updateState {
      val (pickerState) = guardLet(it.datePickerState) { return@updateState it }
      if (endDate == null) {
        it.copy(datePickerState = pickerState.copy(selectedRange = DateRange(start = start, end = start)))
      } else {
        it.copy(datePickerState = pickerState.copy(selectedRange = DateRange(start = start, end = endDate)))
      }
    }
  }

  override fun dateRangeSave() {
    updateState { state ->
      val (newRange) = guardLet(state.datePickerState?.selectedRange) { return@updateState state }

      state.copy(
        range = newRange,
        ranges = state.ranges?.copy(selected = ChartRange.CUSTOM),
        aggregations = aggregations(newRange, state.aggregations?.selected),
        datePickerState = null,
        sets = state.sets.map { set -> set.copy(entries = emptyList()) },
        chartParameters = HideableValue(ChartParameters(1f, 1f, 0f, 0f))
      ).also {
        triggerMeasurementsLoad(it)
      }
    }
    updateUserState()
  }

  protected abstract fun triggerDataLoad(remoteId: Int)

  protected abstract fun measurementsMaybe(
    remoteId: Int,
    profileId: Long,
    start: Date,
    end: Date,
    aggregation: ChartDataAggregation
  ): Single<Pair<List<HistoryDataSet>, Optional<DateRange>>>

  private fun triggerMeasurementsLoad(state: HistoryDetailViewState) {
    val (start, end) = guardLet(state.range?.start, state.range?.end) { return }
    val (remoteId) = guardLet(state.remoteId) { return }
    val (profileId) = guardLet(state.profileId) { return }
    val aggregation = state.aggregations?.selected ?: ChartDataAggregation.MINUTES

    measurementsMaybe(remoteId, profileId, start, end, aggregation)
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleMeasurements(it.first, it.second, userStateHolder.getTemperatureChartState(profileId, remoteId)) },
        onError = defaultErrorHandler("triggerMeasurementsLoad")
      )
      .disposeBySelf()
  }

  private fun openCustomDateSelectionDialog() {
    updateState {
      val selectedRange = if (it.ranges?.selected == ChartRange.CUSTOM) {
        it.range
      } else {
        null
      }
      val selectableRange = if (it.minDate != null && it.maxDate != null) {
        DateRange(it.minDate, it.maxDate)
      } else {
        null
      }
      it.copy(
        datePickerState = CalendarRangePickerState(
          selectedRange = selectedRange,
          selectableRange = selectableRange
        )
      )
    }
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
      sets = state.sets.map { set -> set.copy(entries = emptyList()) }
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

  private fun handleMeasurements(sets: List<HistoryDataSet>, dateRange: Optional<DateRange>, chartState: TemperatureChartState) {
    updateState { state ->
      state.copy(
        // Update sets visibility
        sets = sets.map { set -> set.copy(active = chartState.visibleSets?.contains(set.setId) ?: true) },
        withHumidity = sets.firstOrNull { it.setId.type == ChartEntryType.HUMIDITY } != null,
        maxTemperature = sets
          .filter { it.setId.type == ChartEntryType.TEMPERATURE }
          .mapNotNull { set -> set.entries.maxOfOrNull { entries -> entries.maxOf { it.y } } }
          .maxOfOrNull { it }
          ?.plus(2), // Adds some additional space on chart
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
    val visibleSets = state.sets.filter { it.active }.map { it.setId }

    userStateHolder.setTemperatureChartState(
      state = TemperatureChartState(aggregation, chartRange, dateRange, state.chartParameters?.value, visibleSets),
      profileId = state.profileId,
      remoteId = state.remoteId
    )
  }

  private fun getStartDateForRange(range: ChartRange, date: Date, currentDate: Date, dateForCustom: Date) = when (range) {
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
  }

  private fun getEndDateForRange(range: ChartRange, end: Date, currentDate: Date, dateForCustom: Date) = when (range) {
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
  }
}

sealed class HistoryDetailViewEvent : ViewEvent

data class HistoryDetailViewState(
  val remoteId: Int = 0,
  val profileId: Long = 0,
  val downloadConfigured: Boolean = false,
  val initialLoadStarted: Boolean = false,
  val sets: List<HistoryDataSet> = emptyList(),
  val range: DateRange? = null,
  val ranges: SelectableList<ChartRange>? = null,
  val aggregations: SelectableList<ChartDataAggregation>? = null,
  val loading: Boolean = false,
  val downloadState: DownloadEventsManager.State = DownloadEventsManager.State.Idle,

  val minDate: Date? = null,
  val maxDate: Date? = null,
  val withHumidity: Boolean = false,
  val maxTemperature: Float? = null,
  val chartParameters: HideableValue<ChartParameters>? = null,

  val datePickerState: CalendarRangePickerState? = null
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
        } else if (sets.firstOrNull { it.active } == null) {
          context.getString(R.string.history_no_data_selected)
        } else if (minDate == null && maxDate == null) {
          context.getString(R.string.history_no_data_available)
        } else {
          context.getString(R.string.no_chart_data_available)
        }
      }

      is DownloadEventsManager.State.Failed -> { context -> context.getString(R.string.history_refreshing_failed) }
      else -> { context ->
        if (loading.not() && sets.isEmpty()) {
          context.getString(R.string.history_no_data_available)
        } else {
          context.getString(R.string.retrieving_data_from_the_server)
        }
      }
    }

  val showBottomNavigation: Boolean
    get() = when (ranges?.selected) {
      ChartRange.DAY,
      ChartRange.WEEK,
      ChartRange.MONTH,
      ChartRange.QUARTER,
      ChartRange.YEAR,
      ChartRange.CUSTOM -> true

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

  val xMin: Float?
    get() {
      if (chartMarginNotNeeded()) {
        return range?.start?.toTimestamp()?.toFloat()
      }
      val (daysCount) = guardLet(range?.daysCount) { return range?.start?.toTimestamp()?.toFloat() }
      return range?.start?.toTimestamp()?.minus(chartRangeMargin(daysCount))?.toFloat()
    }

  val xMax: Float?
    get() {
      if (chartMarginNotNeeded()) {
        return range?.end?.toTimestamp()?.toFloat()
      }
      val (daysCount) = guardLet(range?.daysCount) { return range?.end?.toTimestamp()?.toFloat() }
      return range?.end?.toTimestamp()?.plus(chartRangeMargin(daysCount))?.toFloat()
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

      ChartRange.CUSTOM -> longDateString(context, dateRange)
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
      range = range?.shift(chartRange, forward)
    )
  }

  fun combinedData(resources: Resources): CombinedData? {
    val lineDataSets = mutableListOf<ILineDataSet?>().also { list ->
      sets.forEach { set ->
        if (set.active && set.entries.isNotEmpty()) {
          set.entries.forEach {
            list.add(lineDataSet(it, set.color, set.setId.type, resources))
          }
        }
      }
    }

    return if (lineDataSets.isEmpty()) {
      null
    } else {
      CombinedData().apply { setData(LineData(lineDataSets)) }
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

  private fun chartRangeMargin(daysCount: Int): Int {
    val (aggregation) = guardLet(aggregations?.selected) {
      return when {
        daysCount <= 1 -> 60 * 60 // 1 hour in seconds
        else -> 24 * 60 * 60 // 1 day in seconds
      }
    }

    return aggregation.timeInSec.times(0.6).toInt()
  }

  private fun chartMarginNotNeeded() = when (ranges?.selected) {
    ChartRange.LAST_DAY, ChartRange.LAST_WEEK, ChartRange.LAST_MONTH, ChartRange.LAST_QUARTER, ChartRange.CUSTOM -> false
    else -> true
  }
}

private fun lineDataSet(set: List<Entry>, @ColorRes colorRes: Int, type: ChartEntryType, resources: Resources) =
  LineDataSet(set, "").apply {
    setDrawValues(false)
    mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    cubicIntensity = 0.05f
    color = ResourcesCompat.getColor(resources, colorRes, null)
    circleColors = listOf(ResourcesCompat.getColor(resources, colorRes, null))
    setDrawCircleHole(false)
    circleRadius = 0.5.dp.toPx()
    axisDependency = when (type) {
      ChartEntryType.TEMPERATURE -> YAxis.AxisDependency.LEFT
      ChartEntryType.HUMIDITY -> YAxis.AxisDependency.RIGHT
    }
  }
