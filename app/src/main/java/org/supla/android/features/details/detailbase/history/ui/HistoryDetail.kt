@file:OptIn(ExperimentalMaterial3Api::class)

package org.supla.android.features.details.detailbase.history.ui
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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalDateFormatter
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartFilters
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.chart.datatype.CombinedChartData
import org.supla.android.data.model.chart.datatype.LineChartData
import org.supla.android.data.model.chart.datatype.PieChartData
import org.supla.android.data.model.chart.singleLabel
import org.supla.android.data.model.general.RangeValueType
import org.supla.android.data.model.general.SingleSelectionList
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.extensions.date
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.features.details.detailbase.history.HistoryDetailViewState
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.dialogs.DatePickerDialog
import org.supla.android.ui.dialogs.TimePickerDialog
import org.supla.android.ui.extensions.orientated
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.charts.CombinedChart
import org.supla.android.ui.views.charts.PieChart
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.android.ui.views.spinner.TextSpinner
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.formatters.HumidityValueFormatter
import java.util.Date

interface HistoryDetailScope {
  fun refresh()
  fun showSelection(remoteId: Int, type: ChartEntryType)
  fun confirmSelection(spinnerItem: SpinnerItem?, checkboxItems: Set<CheckboxItem>?)
  fun hideSelection()
  fun changeFilter(spinnerItem: SpinnerItem)
  fun moveRangeLeft()
  fun moveRangeRight()
  fun moveToDataBegin()
  fun moveToDataEnd()
  fun updateChartPosition(scaleX: Float, scaleY: Float, x: Float, y: Float)
  fun customRangeEditDate(type: RangeValueType)
  fun customRangeEditHour(type: RangeValueType)
  fun customRangeEditDateDismiss()
  fun customRangeEditHourDismiss()
  fun customRangeEditDateSave(date: Date)
  fun customRangeEditHourSave(hour: Hour)
}

@Composable
fun HistoryDetailScope.View(viewState: HistoryDetailViewState) {
  if (viewState.editDate != null) {
    DatePickerDialog(
      selectedDate = viewState.editDateValue,
      onConfirmTap = { customRangeEditDateSave(it) },
      onDismissTap = { customRangeEditDateDismiss() },
      dateValidator = { viewState.editDayValidator(it) },
      yearRange = viewState.yearRange
    )
  }
  if (viewState.editHour != null) {
    TimePickerDialog(
      selectedHour = viewState.editHourValue,
      onConfirmTap = { customRangeEditHourSave(it) },
      onDismissTap = { customRangeEditHourDismiss() }
    )
  }
  viewState.chartDataSelectionDialogState?.let {
    ChartDataSelectionDialog(
      state = it,
      onDismiss = { hideSelection() },
      onPositiveClick = { spinnerItem, checkboxItem -> confirmSelection(spinnerItem, checkboxItem) }
    )
  }

  if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    ViewLandscape(viewState)
  } else {
    ViewPortrait(viewState)
  }
}

@Composable
private fun HistoryDetailScope.ViewPortrait(viewState: HistoryDetailViewState) {
  Column(modifier = Modifier.fillMaxSize()) {
    DataSetsAndFiltersPortrait(viewState)

    if (viewState.showHistory) {
      Charts(viewState, Modifier.weight(1f))
      if (viewState.filters.selectedRange == ChartRange.CUSTOM) {
        RangeSelection(viewState)
      } else {
        BottomPagination(viewState = viewState)
      }
    } else {
      NoHistoryText()
    }
  }
}

@Composable
private fun HistoryDetailScope.ViewLandscape(viewState: HistoryDetailViewState) {
  Row(modifier = Modifier.fillMaxSize()) {
    DataSetsLandscape(viewState)
    Column(modifier = Modifier.weight(1f)) {
      if (viewState.showHistory) {
        Charts(
          viewState = viewState,
          modifier = Modifier.weight(1f)
        )
        if (viewState.filters.selectedRange == ChartRange.CUSTOM) {
          RangeSelection(viewState)
        } else {
          BottomPagination(viewState = viewState)
        }
      } else {
        NoHistoryText()
      }
    }
    FiltersColumn(viewState)
  }
}

@Composable
private fun NoHistoryText() =
  Text(
    text = stringResource(id = R.string.history_disabled),
    textAlign = TextAlign.Center,
    style = MaterialTheme.typography.bodyLarge,
    modifier = Modifier
      .fillMaxWidth()
      .padding(all = Distance.default)
  )

@Composable
private fun HistoryDetailScope.Charts(viewState: HistoryDetailViewState, modifier: Modifier = Modifier) {
  when (val data = viewState.chartData) {
    is CombinedChartData ->
      CombinedChart(
        data = data,
        emptyChartMessage = viewState.emptyChartMessage(LocalContext.current),
        withRightAxis = viewState.withRightAxis,
        withLeftAxis = viewState.withLeftAxis,
        maxLeftAxis = viewState.maxLeftAxis,
        maxRightAxis = viewState.maxRightAxis,
        chartParametersProvider = { viewState.chartParameters?.getOptional() },
        positionEvents = { scaleX, scaleY, x, y -> updateChartPosition(scaleX, scaleY, x, y) },
        chartStyle = viewState.chartStyle,
        modifier = modifier.padding(horizontal = Distance.tiny)
      )

    is PieChartData ->
      PieChart(
        data = data,
        emptyChartMessage = viewState.emptyChartMessage(LocalContext.current),
        chartStyle = viewState.chartStyle,
        modifier = modifier.padding(horizontal = Distance.tiny)
      )
  }
}

@Composable
private fun HistoryDetailScope.DataSetsAndFiltersPortrait(viewState: HistoryDetailViewState) {
  val pullToRefreshState = rememberPullToRefreshState()
  Box(
    modifier = Modifier
      .pullToRefresh(
        isRefreshing = viewState.loading,
        onRefresh = { refresh() },
        state = pullToRefreshState,
        threshold = 58.dp
      )
      .fillMaxWidth()
  ) {
    // Vertical scroll is needed to make pull refresh working
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      DataSetsRow(viewState)
      if (viewState.showHistory) {
        FiltersRow(viewState)
      }
    }

    PullToRefreshDefaults.Indicator(
      state = pullToRefreshState,
      isRefreshing = viewState.loading,
      modifier = Modifier.align(Alignment.TopCenter),
      color = MaterialTheme.colorScheme.primary,
      maxDistance = 58.dp
    )
  }
}

@Composable
fun HistoryDetailScope.DataSetsLandscape(viewState: HistoryDetailViewState) {
  val pullToRefreshState = rememberPullToRefreshState()
  Row {
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .width(IntrinsicSize.Max)
        .widthIn(min = 140.dp)
        .background(color = MaterialTheme.colorScheme.surface)
        .pullToRefresh(
          isRefreshing = viewState.loading,
          onRefresh = { refresh() },
          state = pullToRefreshState,
          threshold = 58.dp
        )
        .verticalScroll(rememberScrollState())
    ) {
      DataSetsColumn(viewState)

      PullToRefreshDefaults.Indicator(
        state = pullToRefreshState,
        isRefreshing = viewState.loading,
        modifier = Modifier.align(Alignment.TopCenter),
        color = MaterialTheme.colorScheme.primary,
        maxDistance = 58.dp
      )
    }
    Shadow(orientation = ShadowOrientation.STARTING_LEFT)
  }
}

@Composable
private fun HistoryDetailScope.DataSetsRow(viewState: HistoryDetailViewState) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .background(color = MaterialTheme.colorScheme.surface)
      .height(80.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    viewState.chartData.sets.forEach { data ->
      DataSetContainerRow(data, viewState.showHistory) { remoteId, type -> showSelection(remoteId, type) }
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(1.dp)
          .background(MaterialTheme.colorScheme.background)
      )
    }
  }
  Shadow(orientation = ShadowOrientation.STARTING_TOP)
}

@Composable
private fun HistoryDetailScope.DataSetsColumn(viewState: HistoryDetailViewState, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(top = Distance.default),
    horizontalAlignment = Alignment.Start,
    verticalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    viewState.chartData.sets.forEach { data ->
      DataSetContainerColumn(data, viewState.showHistory) { remoteId, type -> showSelection(remoteId, type) }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(1.dp)
          .background(MaterialTheme.colorScheme.background)
      )
    }
  }
}

@Composable
private fun HistoryDetailScope.FiltersRow(viewState: HistoryDetailViewState) =
  Row(modifier = Modifier.padding(top = Distance.tiny)) {
    val lastIdx = viewState.filters.values.size - 1
    viewState.filters.values.forEachIndexed { index, selectableList ->
      TextSpinner(
        options = selectableList,
        onOptionSelected = { changeFilter(it) },
        enabled = viewState.loading.not(),
        modifier = Modifier
          .padding(
            start = if (index == 0) Distance.small else 0.dp,
            end = if (index == lastIdx) 4.dp else 0.dp
          )
      )

      if (viewState.filters.count() == 2 && index == 0) {
        Spacer(
          modifier = Modifier
            .weight(1f)
            .defaultMinSize(minWidth = dimensionResource(id = R.dimen.distance_small))
        )
      }
    }
  }

@Composable
private fun HistoryDetailScope.FiltersColumn(viewState: HistoryDetailViewState) =
  Column(
    modifier = Modifier.padding(top = Distance.tiny).widthIn(max = 140.dp),
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    viewState.filters.values.forEachIndexed { index, selectableList ->
      TextSpinner(
        options = selectableList,
        onOptionSelected = { changeFilter(it) },
        enabled = viewState.loading.not()
      )
    }
  }

@Composable
private fun HistoryDetailScope.BottomPagination(viewState: HistoryDetailViewState) =
  viewState.rangeText?.let {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .padding(horizontal = dimensionResource(id = R.dimen.distance_tiny))
        .heightIn(min = LocalConfiguration.current.orientated(80.dp, 50.dp))
    ) {
      if (viewState.allowNavigation) {
        PaginationIcon(
          onClick = { moveToDataBegin() },
          icon = R.drawable.ic_double_arrow_right,
          enabled = viewState.shiftLeftEnabled,
          rotate = true
        )
        PaginationIcon(
          onClick = { moveRangeLeft() },
          enabled = viewState.shiftLeftEnabled,
          rotate = true
        )
      }
      Text(
        text = it,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f)
      )
      if (viewState.allowNavigation) {
        PaginationIcon(
          onClick = { moveRangeRight() },
          enabled = viewState.shiftRightEnabled
        )
        PaginationIcon(
          onClick = { moveToDataEnd() },
          enabled = viewState.shiftRightEnabled,
          icon = R.drawable.ic_double_arrow_right
        )
      }
    }
  }

@Composable
private fun PaginationIcon(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: Int = R.drawable.ic_arrow_right,
  enabled: Boolean = true,
  rotate: Boolean = false
) =
  IconButton(icon = icon, onClick = onClick, modifier = modifier, enabled = enabled, rotate = rotate)

@Composable
private fun HistoryDetailScope.RangeSelection(viewState: HistoryDetailViewState) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .padding(horizontal = dimensionResource(id = R.dimen.distance_default))
      .heightIn(min = LocalConfiguration.current.orientated(80.dp, 50.dp))
  ) {
    Spacer(modifier = Modifier.weight(0.01f))
    DateTextField(date = viewState.range?.start) { customRangeEditDate(RangeValueType.START) }
    HourTextField(date = viewState.range?.start) { customRangeEditHour(RangeValueType.START) }
    Text(text = "-", modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.distance_tiny)))
    DateTextField(date = viewState.range?.end) { customRangeEditDate(RangeValueType.END) }
    HourTextField(date = viewState.range?.end) { customRangeEditHour(RangeValueType.END) }
    Spacer(modifier = Modifier.weight(0.01f))
  }
}

@Composable
fun RowScope.DateTextField(date: Date?, onClick: () -> Unit) {
  TextField(
    value = LocalDateFormatter.current.getDateString(date) ?: "",
    modifier = Modifier
      .weight(0.3f)
      .height(36.dp),
    contentPadding = PaddingValues(8.dp),
    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
    readOnly = true,
    onClicked = onClick
  )
}

@Composable
fun RowScope.HourTextField(date: Date?, onClick: () -> Unit) {
  TextField(
    value = LocalDateFormatter.current.getHourString(date) ?: "",
    modifier = Modifier
      .weight(0.18f)
      .height(36.dp),
    contentPadding = PaddingValues(8.dp),
    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
    readOnly = true,
    onClicked = onClick
  )
}

val previewScope = object : HistoryDetailScope {
  override fun refresh() {}
  override fun showSelection(remoteId: Int, type: ChartEntryType) {}
  override fun confirmSelection(spinnerItem: SpinnerItem?, checkboxItems: Set<CheckboxItem>?) {}
  override fun hideSelection() {}
  override fun changeFilter(spinnerItem: SpinnerItem) {}
  override fun moveRangeLeft() {}
  override fun moveRangeRight() {}
  override fun moveToDataBegin() {}
  override fun moveToDataEnd() {}
  override fun updateChartPosition(scaleX: Float, scaleY: Float, x: Float, y: Float) {}
  override fun customRangeEditDate(type: RangeValueType) {}
  override fun customRangeEditHour(type: RangeValueType) {}
  override fun customRangeEditDateDismiss() {}
  override fun customRangeEditHourDismiss() {}
  override fun customRangeEditDateSave(date: Date) {}
  override fun customRangeEditHourSave(hour: Hour) {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Preview(
  name = "Phone - Landscape",
  device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
  showSystemUi = true,
  backgroundColor = 0x00CCCCCC
)
@Composable
private fun Preview() {
  val set = HistoryDataSet(
    type = ChartEntryType.HUMIDITY,
    label = singleLabel(
      imageId = ImageId(R.drawable.fnc_electricity_meter),
      value = "Label",
      color = R.color.light_red
    ),
    valueFormatter = HumidityValueFormatter,
  )

  SuplaTheme {
    previewScope.View(
      viewState =
      HistoryDetailViewState(
        chartData = LineChartData(
          dateRange = DateRange(Date().weekStart(), Date().weekEnd()),
          chartRange = ChartRange.WEEK,
          aggregation = ChartDataAggregation.MINUTES,
          sets = listOf(
            ChannelChartSets(
              1,
              SuplaFunction.UNKNOWN,
              LocalizedString.Empty,
              ChartDataAggregation.MINUTES,
              listOf(set, set.copy(active = false)),
              null,
              localizedString(R.string.details_em_reverse_reactive_energy)
            )
          )
        ),
        filters = ChartFilters()
          .putRanges(SingleSelectionList(ChartRange.DAY, ChartRange.entries, R.string.history_range_label))
          .putAggregations(SingleSelectionList(ChartDataAggregation.MINUTES, ChartDataAggregation.entries, R.string.history_data_type)),
        range = DateRange(start = date(2025, 10, 30), end = date(2025, 10, 30, 23, 59, 59)),
        showHistory = true
      )
    )
  }
}
