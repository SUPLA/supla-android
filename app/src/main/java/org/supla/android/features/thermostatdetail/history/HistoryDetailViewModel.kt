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
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.BitmapProvider
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.dayEnd
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.shift
import org.supla.android.extensions.toPx
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.thermostatdetail.history.data.ChartDataAggregation
import org.supla.android.features.thermostatdetail.history.data.ChartRange
import org.supla.android.features.thermostatdetail.history.data.SelectableList
import org.supla.android.features.thermostatdetail.history.ui.HistoryDetailProxy
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
  private val dateProvider: DateProvider,
  private val downloadEventsManager: DownloadEventsManager,
  schedulers: SuplaSchedulers
) : BaseViewModel<HistoryDetailViewState, HistoryDetailViewEvent>(HistoryDetailViewState(), schedulers), HistoryDetailProxy {

  override fun onViewCreated() {
    updateState { state ->
      state.copy(
        ranges = SelectableList(ChartRange.DAY, ChartRange.values().toList()),
        aggregations = SelectableList(ChartDataAggregation.MINUTES, ChartDataAggregation.values().toList()),
        rangeStartDate = Date().dayStart(),
        rangeEndDate = Date().dayEnd()
      )
    }
  }

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
      when (range) {
        ChartRange.DAY -> state.copy(rangeEndDate = state.rangeStartDate?.dayEnd())
        ChartRange.WEEK,
        ChartRange.MONTH,
        ChartRange.DAYS_90 -> state.changeRange(range, dateProvider.currentDate().dayEnd())

        else -> state
      }.copy(
        ranges = state.ranges?.copy(selected = range),
        sets = state.sets.map { set -> set.copy(entries = emptyList()) }
      ).also {
        triggerMeasurementsLoad(it)
      }
    }
  }

  override fun moveRangeLeft() {
    shiftByRange(forward = false)
  }

  override fun moveRangeRight() {
    shiftByRange(forward = true)
  }

  override fun changeAggregation(aggregation: ChartDataAggregation) {
    updateState { state ->
      state.copy(aggregations = state.aggregations?.copy(selected = aggregation)).also {
        triggerMeasurementsLoad(it)
      }
    }
  }

  private fun shiftByRange(forward: Boolean) {
    updateState { state ->
      val (range) = guardLet(state.ranges?.selected) { return@updateState state }
      val daysCount = if (forward) range.daysCount else -range.daysCount

      when (range) {
        ChartRange.DAY,
        ChartRange.WEEK,
        ChartRange.MONTH,
        ChartRange.DAYS_90 -> state.shiftRange(daysCount, dateProvider.currentDate().dayEnd())

        else -> state
      }.also { triggerMeasurementsLoad(it) }
    }
  }

  private fun triggerDataLoad(remoteId: Int) {
    readChannelWithChildrenUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onSuccess = { handleData(it) },
        onError = defaultErrorHandler("triggerDataLoad")
      )
      .disposeBySelf()
  }

  private fun triggerMeasurementsLoad(state: HistoryDetailViewState) {
    val (start, end) = guardLet(state.rangeStartDate, state.rangeEndDate) { return }
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

  private fun handleData(channel: ChannelWithChildren) {
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
}

sealed class HistoryDetailViewEvent : ViewEvent

data class HistoryDetailViewState(
  val remoteId: Int = 0,
  val downloadConfigured: Boolean = false,
  val initialLoadStarted: Boolean = false,
  val sets: List<HistoryDataSet> = emptyList(),
  val ranges: SelectableList<ChartRange>? = null,
  val aggregations: SelectableList<ChartDataAggregation>? = null,
  val rangeStartDate: Date? = null,
  val rangeEndDate: Date? = null,
  val loading: Boolean = false,
  val downloadState: DownloadEventsManager.State = DownloadEventsManager.State.Idle
) : ViewState() {

  val shiftRightEnabled: Boolean
    get() = Date().dayEnd() != rangeEndDate

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

  fun rangeText(context: Context): String? =
    when (ranges?.selected) {
      ChartRange.DAY -> context.valuesFormatter.getDateString(rangeStartDate)
      ChartRange.WEEK,
      ChartRange.MONTH,
      ChartRange.DAYS_90,
      ChartRange.CUSTOM -> {
        val startString = context.valuesFormatter.getDateString(rangeStartDate)
        val endString = context.valuesFormatter.getDateString(rangeEndDate)
        "$startString - $endString"
      }

      else -> null
    }

  internal fun changeRange(range: ChartRange, endOfToday: Date): HistoryDetailViewState {
    val days = range.daysCount - 1
    val rangeEnd = rangeStartDate?.shift(days)
    return if (rangeEnd != null && rangeEnd.after(endOfToday)) {
      copy(rangeStartDate = endOfToday.shift(-days).dayStart(), rangeEndDate = endOfToday)
    } else {
      copy(rangeEndDate = rangeStartDate?.shift(days)?.dayEnd())
    }
  }

  internal fun shiftRange(days: Int, endOfToday: Date): HistoryDetailViewState {
    val rangeEnd = rangeEndDate?.shift(days)
    val rangeDays = ranges?.selected?.daysCount?.minus(1) ?: 0

    return if (rangeEnd != null && rangeEnd.after(endOfToday)) {
      copy(rangeStartDate = endOfToday.shift(-rangeDays).dayStart(), rangeEndDate = endOfToday)
    } else {
      copy(
        rangeStartDate = rangeStartDate?.shift(days)?.dayStart(),
        rangeEndDate = rangeEndDate?.shift(days)?.dayEnd()
      )
    }
  }

  fun combinedData(resources: Resources): CombinedData? {
    val lineDataSets = mutableListOf<ILineDataSet?>().also { list ->
      sets.forEach {
        if (it.active && it.entries.isNotEmpty()) {
          list.add(lineDataSet(it.entries, it.color, resources))
        }
      }
    }

    return if (lineDataSets.isEmpty()) {
      null
    } else {
      CombinedData().apply { setData(LineData(lineDataSets)) }
    }
  }
}

data class HistoryDataSet(
  val setId: Id,
  val function: Int,
  val iconProvider: BitmapProvider,
  val valueProvider: StringProvider,
  @ColorRes val color: Int,
  val entries: List<Entry> = emptyList(),
  val active: Boolean = true
) {

  fun aggregating(aggregation: ChartDataAggregation): HistoryDataSet {
    if (aggregation == ChartDataAggregation.MINUTES) {
      // For minutes is not needed
      return this
    }
    val entries = entries.groupBy { item -> item.x.toLong().div(aggregation.timeInSec) }
      .map { group ->
        Entry(group.key.times(aggregation.timeInSec).toFloat(), group.value.map { it.y }.average().toFloat())
      }

    return copy(entries = entries)
  }

  data class Id(
    val remoteId: Int,
    val type: Type
  )

  enum class Type {
    TEMPERATURE, HUMIDITY
  }
}

private fun lineDataSet(set: List<Entry>, @ColorRes colorRes: Int, resources: Resources) =
  LineDataSet(set, "Test").apply {
    setDrawValues(false)
    mode = LineDataSet.Mode.HORIZONTAL_BEZIER
    cubicIntensity = 0.05f
    color = ResourcesCompat.getColor(resources, colorRes, null)
    circleColors = listOf(ResourcesCompat.getColor(resources, colorRes, null))
    setDrawCircleHole(false)
    circleRadius = 0.5.dp.toPx()
  }
