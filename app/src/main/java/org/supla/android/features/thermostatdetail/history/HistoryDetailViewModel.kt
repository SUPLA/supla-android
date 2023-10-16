package org.supla.android.features.thermostatdetail.history
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
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.TemperatureChartState
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.BitmapProvider
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.chart.ChartEntryType
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
import org.supla.android.extensions.valuesFormatter
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.extensions.yearEnd
import org.supla.android.extensions.yearStart
import org.supla.android.features.thermostatdetail.history.data.ChartDataAggregation
import org.supla.android.features.thermostatdetail.history.data.ChartRange
import org.supla.android.features.thermostatdetail.history.data.DateRange
import org.supla.android.features.thermostatdetail.history.data.SelectableList
import org.supla.android.features.thermostatdetail.history.ui.HistoryDetailProxy
import org.supla.android.profile.ProfileManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.DownloadChannelMeasurementsUseCase
import org.supla.android.usecases.channel.LoadChannelWithChildrenMeasurementsUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
  private val downloadChannelMeasurementsUseCase: DownloadChannelMeasurementsUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  private val loadChannelWithChildrenMeasurementsUseCase: LoadChannelWithChildrenMeasurementsUseCase,
  private val downloadEventsManager: DownloadEventsManager,
  private val profileManager: ProfileManager,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<HistoryDetailViewState, HistoryDetailViewEvent>(HistoryDetailViewState(), schedulers), HistoryDetailProxy {

  fun loadData(remoteId: Int) {
    triggerDataLoad(remoteId)
    updateState { state ->
      state.copy(remoteId = remoteId, loading = true)
    }
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
  }

  override fun changeRange(range: ChartRange) {
    updateState { state ->
      val (currentRange) = guardLet(state.range) { return@updateState state }
      val currentDate = dateProvider.currentDate()

      val rangeStart = when (range) {
        ChartRange.DAY -> currentRange.end.dayStart()
        ChartRange.LAST_DAY,
        ChartRange.LAST_WEEK,
        ChartRange.LAST_MONTH,
        ChartRange.LAST_QUARTER -> currentDate.shift(-range.roundedDaysCount)

        ChartRange.WEEK -> (currentRange.end).weekStart()
        ChartRange.MONTH -> (currentRange.end).monthStart()
        ChartRange.QUARTER -> (currentRange.end).quarterStart()
        ChartRange.YEAR -> (currentRange.end).yearStart()
        ChartRange.CUSTOM -> currentRange.start
      }
      val rangeEnd = when (range) {
        ChartRange.DAY -> currentRange.end.dayEnd()
        ChartRange.LAST_DAY,
        ChartRange.LAST_WEEK,
        ChartRange.LAST_MONTH,
        ChartRange.LAST_QUARTER -> currentDate

        ChartRange.WEEK -> (currentRange.end).weekEnd()
        ChartRange.MONTH -> (currentRange.end).monthEnd()
        ChartRange.QUARTER -> (currentRange.end).quarterEnd()
        ChartRange.YEAR -> (currentRange.end).yearEnd()
        ChartRange.CUSTOM -> currentRange.end
      }

      state.copy(
        ranges = state.ranges?.copy(selected = range),
        range = DateRange(rangeStart, rangeEnd),
        aggregations = aggregations(state, range),
        sets = state.sets.map { set -> set.copy(entries = emptyList()) }
      ).also {
        triggerMeasurementsLoad(it)
      }
    }

    updateUserState()
  }

  override fun moveRangeLeft() {
    shiftByRange(forward = false)
    updateUserState()
  }

  override fun moveRangeRight() {
    shiftByRange(forward = true)
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

  private fun shiftByRange(forward: Boolean) {
    updateState { state ->
      val (range) = guardLet(state.ranges?.selected) { return@updateState state }
      state.shiftRange(range, forward).also { triggerMeasurementsLoad(it) }
    }
  }

  private fun triggerDataLoad(remoteId: Int) {
    Maybe.zip(
      readChannelWithChildrenUseCase(remoteId),
      profileManager.getCurrentProfile().map { userStateHolder.getTemperatureChartState(it.id, remoteId) },
    ) { first, second ->
      Pair(first, second)
    }
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleData(it.first, it.second) },
        onError = defaultErrorHandler("triggerDataLoad")
      )
      .disposeBySelf()
  }

  private fun triggerMeasurementsLoad(state: HistoryDetailViewState) {
    val (start, end) = guardLet(state.range?.start, state.range?.end) { return }
    val (remoteId) = guardLet(state.remoteId) { return }
    val aggregation = state.aggregations?.selected ?: ChartDataAggregation.MINUTES

    loadChannelWithChildrenMeasurementsUseCase(remoteId, start, end, aggregation)
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleMeasurements(it) },
        onError = defaultErrorHandler("triggerMeasurementsLoad")
      )
      .disposeBySelf()
  }

  private fun handleData(channel: ChannelWithChildren, chartState: TemperatureChartState) {
    val selectedRange = chartState.chartRange
    val dateRange = chartState.dateRange
    val currentDate = dateProvider.currentDate()

    updateState { state ->
      state.copy(
        profileId = channel.channel.profileId,
        ranges = SelectableList(selectedRange, ChartRange.values().toList()),
        aggregations = aggregations(state, selectedRange, chartState.aggregation),
        range = dateRange ?: DateRange(currentDate.shift(-selectedRange.roundedDaysCount), currentDate)
      )
    }

    configureDownloadObserver(channel)
    startInitialDataLoad(channel)
  }

  private fun startInitialDataLoad(channel: ChannelWithChildren) {
    if (currentState().initialLoadStarted) {
      // Needs to be performed only once
      return
    }

    channel.children.firstOrNull { it.relationType.isMainThermometer() }?.channel?.let {
      downloadChannelMeasurementsUseCase.invoke(it.remoteId, it.profileId, it.func)
    }
    channel.children.firstOrNull { it.relationType.isAuxThermometer() }?.channel?.let {
      downloadChannelMeasurementsUseCase.invoke(it.remoteId, it.profileId, it.func)
    }
  }

  private fun handleMeasurements(sets: List<HistoryDataSet>) {
    updateState { it.copy(sets = sets, loading = false) }
  }

  private fun configureDownloadObserver(channel: ChannelWithChildren) {
    if (currentState().downloadConfigured) {
      // Needs to be performed only once
      return
    }

    updateState { it.copy(downloadConfigured = true) }

    val mainThermometerId = channel.children.firstOrNull { it.relationType.isMainThermometer() }?.channel?.remoteId
    val auxThermometerId = channel.children.firstOrNull { it.relationType.isAuxThermometer() }?.channel?.remoteId

    val observables = mutableListOf<Observable<DownloadEventsManager.State>>()
    mainThermometerId?.let { observables.add(downloadEventsManager.observeProgress(it)) }
    auxThermometerId?.let { observables.add(downloadEventsManager.observeProgress(it)) }

    val observable = if (observables.count() == 2) {
      Observable.combineLatest(observables[0], observables[1]) { first, second ->
        Pair<DownloadEventsManager.State, DownloadEventsManager.State?>(first, second)
      }
    } else if (observables.count() == 1) {
      observables[0].map { Pair<DownloadEventsManager.State, DownloadEventsManager.State?>(it, null) }
    } else {
      Observable.empty()
    }

    observable.attachSilent()
      .map { mergeEvents(it.first, it.second) }
      .distinctUntilChanged()
      .subscribeBy(
        onNext = { handleDownloadEvents(it) },
        onError = defaultErrorHandler("configureDownloadObserver")
      )
      .disposeBySelf()
  }

  private fun mergeEvents(main: DownloadEventsManager.State, aux: DownloadEventsManager.State?): DownloadEventsManager.State {
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

  private fun handleDownloadEvents(downloadState: DownloadEventsManager.State) {
    when (downloadState) {
      is DownloadEventsManager.State.InProgress,
      is DownloadEventsManager.State.Started,
      is DownloadEventsManager.State.Failed -> {
        updateState { state ->
          state.copy(
            downloadState = downloadState,
            loading = true
          )
        }
      }

      is DownloadEventsManager.State.Finished -> {
        updateState { state ->
          triggerMeasurementsLoad(state)
          state.copy(downloadState = downloadState)
        }
      }

      else -> {
        updateState { it.copy(loading = false) }
      }
    }
  }

  private fun aggregations(
    state: HistoryDetailViewState,
    currentRange: ChartRange,
    selectedAggregation: ChartDataAggregation = ChartDataAggregation.MINUTES
  ): SelectableList<ChartDataAggregation> {
    val maxAggregation = currentRange.maxAggregation.let {
      if (it == null) { // It's custom state, calculate days difference
        val (startDay, endDay) = guardLet(state.range?.start, state.range?.end) { return@let ChartDataAggregation.YEARS }

        val differenceInDays = (endDay.time - startDay.time).div(3600000).div(24)
        for (range in ChartRange.values()) {
          if (differenceInDays <= range.roundedDaysCount) {
            return@let range.maxAggregation
          }
        }
      }

      it
    } ?: ChartDataAggregation.YEARS

    val newAggregations = SelectableList(
      selected = state.aggregations?.selected ?: selectedAggregation,
      items = ChartDataAggregation.values().filter { it.timeInSec <= maxAggregation.timeInSec }
    )
    if (!newAggregations.items.contains(newAggregations.selected)) {
      return newAggregations.copy(selected = newAggregations.items.last())
    }

    return newAggregations
  }

  private fun updateUserState() {
    val state = currentState()
    val (aggregation) = guardLet(state.aggregations?.selected) { return }
    val (chartRange) = guardLet(state.ranges?.selected) { return }
    val (dateRange) = guardLet(state.range) { return }

    userStateHolder.setTemperatureChartState(
      state = TemperatureChartState(aggregation, chartRange, dateRange),
      profileId = state.profileId,
      remoteId = state.remoteId
    )
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
  val downloadState: DownloadEventsManager.State = DownloadEventsManager.State.Idle
) : ViewState() {

  val shiftRightEnabled: Boolean
    get() = Date().dayEnd() != range?.end

  val emptyChartMessage: StringProvider
    get() = when (downloadState) {
      is DownloadEventsManager.State.Started -> { context -> context.getString(R.string.history_refreshing) }

      is DownloadEventsManager.State.InProgress -> { context ->
        val description = context.getString(R.string.retrieving_data_from_the_server)
        val percentage = context.valuesFormatter.getPercentageString(downloadState.progress)
        "$description $percentage"
      }

      is DownloadEventsManager.State.Finished -> { context ->
        if (loading || sets.firstOrNull { it.entries.isNotEmpty() } != null) {
          context.getString(R.string.history_refreshing)
        } else {
          context.getString(R.string.no_chart_data_available)
        }
      }

      is DownloadEventsManager.State.Failed -> { context -> context.getString(R.string.history_refreshing_failed) }
      else -> { context -> context.getString(R.string.retrieving_data_from_the_server) }
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

      ChartRange.WEEK,
      ChartRange.MONTH,
      ChartRange.LAST_WEEK,
      ChartRange.LAST_MONTH -> dayAndHourString(context, dateRange)

      ChartRange.LAST_QUARTER,
      ChartRange.QUARTER,
      ChartRange.YEAR -> dateString(context, dateRange)

      ChartRange.CUSTOM -> {
        val days = dateRange.daysCount
        when {
          days <= ChartRange.LAST_MONTH.roundedDaysCount -> dayAndHourString(context, dateRange)
          else -> dateString(context, dateRange)
        }
      }
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
    val startString = context.valuesFormatter.getDateString(range.start)
    val endString = context.valuesFormatter.getDateString(range.end)
    return "$startString - $endString"
  }
}

data class HistoryDataSet(
  val setId: Id,
  val function: Int,
  val iconProvider: BitmapProvider,
  val valueProvider: StringProvider,
  @ColorRes val color: Int,
  val entries: List<List<Entry>> = emptyList(),
  val active: Boolean = true
) {

  data class Id(
    val remoteId: Int,
    val type: ChartEntryType
  )
}

private fun lineDataSet(set: List<Entry>, @ColorRes colorRes: Int, type: ChartEntryType, resources: Resources) =
  LineDataSet(set, "Test").apply {
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
