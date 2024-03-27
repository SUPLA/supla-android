@file:OptIn(ExperimentalMaterialApi::class)

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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.data.model.chart.style.ChartStyle
import org.supla.android.data.model.chart.style.ThermometerChartStyle
import org.supla.android.data.model.general.RangeValueType
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.extensions.customOuterShadow
import org.supla.android.extensions.toPx
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.details.detailbase.history.HistoryDetailViewState
import org.supla.android.ui.dialogs.DatePickerDialog
import org.supla.android.ui.dialogs.TimePickerDialog
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.TextSpinner
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.charts.CombinedChart
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import java.util.Date

interface HistoryDetailProxy : BaseViewProxy<HistoryDetailViewState> {
  fun refresh() {}
  fun changeSetActive(setId: HistoryDataSet.Id) {}
  fun changeRange(range: ChartRange) {}
  fun changeAggregation(aggregation: ChartDataAggregation) {}
  fun moveRangeLeft() {}
  fun moveRangeRight() {}
  fun moveToDataBegin() {}
  fun moveToDataEnd() {}
  fun updateChartPosition(scaleX: Float, scaleY: Float, x: Float, y: Float) {}
  fun customRangeEditDate(type: RangeValueType) {}
  fun customRangeEditHour(type: RangeValueType) {}
  fun customRangeEditDateDismiss() {}
  fun customRangeEditHourDismiss() {}
  fun customRangeEditDateSave(date: Date) {}
  fun customRangeEditHourSave(hour: Hour) {}
  fun chartStyle(): ChartStyle = ThermometerChartStyle
}

@Composable
fun HistoryDetail(viewModel: HistoryDetailProxy) {
  val viewState by viewModel.getViewState().collectAsState()

  if (viewState.editDate != null) {
    DatePickerDialog(
      selectedDate = viewState.editDateValue,
      onConfirmTap = { viewModel.customRangeEditDateSave(it) },
      onDismissTap = { viewModel.customRangeEditDateDismiss() },
      dateValidator = { viewState.editDayValidator(it) },
      yearRange = viewState.yearRange
    )
  }
  if (viewState.editHour != null) {
    TimePickerDialog(
      selectedHour = viewState.editHourValue,
      onConfirmTap = { viewModel.customRangeEditHourSave(it) },
      onDismissTap = { viewModel.customRangeEditHourDismiss() }
    )
  }

  Column(
    modifier = Modifier.fillMaxSize()
  ) {
    DataSetsAndFilters(viewState = viewState, viewModel = viewModel)

    if (viewState.showHistory) {
      CombinedChart(
        data = viewState.chartData,
        channelFunction = viewState.channelFunction,
        emptyChartMessage = viewState.emptyChartMessage(LocalContext.current),
        withRightAxis = viewState.withRightAxis,
        withLeftAxis = viewState.withLeftAxis,
        maxLeftAxis = viewState.maxLeftAxis,
        maxRightAxis = viewState.maxRightAxis,
        chartParametersProvider = { viewState.chartParameters?.getOptional() },
        positionEvents = viewModel::updateChartPosition,
        chartStyle = viewModel.chartStyle(),
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = Distance.tiny)
      )

      if (viewState.ranges?.selected == ChartRange.CUSTOM) {
        RangeSelection(viewState, viewModel)
      } else if (viewState.showBottomBar) {
        BottomPagination(viewState = viewState, viewModel = viewModel)
      } else {
        Spacer(
          modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
        )
      }
    } else {
      Text(
        text = stringResource(id = R.string.history_disabled),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.body1,
        modifier = Modifier
          .fillMaxWidth()
          .padding(all = Distance.default)
      )
    }
  }
}

@Composable
private fun DataSetsAndFilters(viewState: HistoryDetailViewState, viewModel: HistoryDetailProxy) {
  val pullRefreshState = rememberPullRefreshState(
    refreshing = viewState.loading,
    onRefresh = { viewModel.refresh() },
    refreshThreshold = 58.dp
  )

  Box(
    modifier = Modifier
      .pullRefresh(pullRefreshState)
      .fillMaxWidth()
  ) {
    // Vertical scroll is needed to make pull refresh working
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      DataSetsRow {
        viewState.chartData.sets.forEach { data ->
          DataSetItem(dataSet = data, historyEnabled = viewState.showHistory, onClick = { viewModel.changeSetActive(data.setId) })
        }
      }
      Shadow(orientation = ShadowOrientation.STARTING_TOP)
      if (viewState.showHistory) {
        FiltersRow(viewState, viewModel)
      }
    }

    PullRefreshIndicator(
      refreshing = viewState.loading,
      state = pullRefreshState,
      modifier = Modifier.align(Alignment.TopCenter),
      contentColor = MaterialTheme.colors.primary
    )
  }
}

@Composable
private fun FiltersRow(viewState: HistoryDetailViewState, viewModel: HistoryDetailProxy) =
  Row(modifier = Modifier.padding(top = Distance.tiny)) {
    TextSpinner(
      label = stringResource(id = R.string.history_range_label),
      options = viewState.rangesMap(LocalContext.current.resources),
      onOptionSelected = { viewModel.changeRange(it) },
      selectedOption = viewState.ranges?.selected,
      modifier = Modifier.padding(start = Distance.default)
    )
    Spacer(
      modifier = Modifier
        .weight(1f)
        .defaultMinSize(minWidth = dimensionResource(id = R.dimen.distance_small))
    )
    TextSpinner(
      label = stringResource(id = R.string.history_aggregation_label),
      options = viewState.aggregationsMap(LocalContext.current.resources),
      onOptionSelected = { viewModel.changeAggregation(it) },
      selectedOption = viewState.aggregations?.selected,
      modifier = Modifier.padding(end = Distance.default)
    )
  }

@Composable
private fun DataSetsRow(content: @Composable RowScope.() -> Unit) =
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .background(color = MaterialTheme.colors.surface)
      .height(80.dp)
      .padding(
        start = dimensionResource(id = R.dimen.distance_default),
        top = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      ),
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_small)),
    content = content
  )

@Composable
private fun DataSetItem(
  dataSet: HistoryDataSet,
  historyEnabled: Boolean,
  onClick: () -> Unit = { }
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    dataSet.iconProvider(LocalContext.current)?.let {
      DataSetIcon(bitmap = it.asImageBitmap())
    }

    DataSetButton(
      text = dataSet.value,
      borderColor = colorResource(id = dataSet.color),
      onClick = onClick,
      colors = if (historyEnabled && dataSet.active) {
        ButtonDefaults.buttonColors(
          backgroundColor = colorResource(id = dataSet.color),
          contentColor = colorResource(id = R.color.on_primary)
        )
      } else {
        ButtonDefaults.buttonColors(
          backgroundColor = colorResource(id = R.color.background),
          contentColor = colorResource(id = dataSet.color)
        )
      }
    )
  }
}

@Composable
private fun DataSetIcon(bitmap: ImageBitmap) =
  Image(
    bitmap = bitmap,
    contentDescription = null,
    alignment = Alignment.Center,
    modifier = Modifier.size(dimensionResource(id = R.dimen.button_small_height))
  )

@Composable
private fun DataSetButton(text: String, colors: ButtonColors, borderColor: Color, onClick: () -> Unit) {
  val height = dimensionResource(id = R.dimen.button_small_height)
  val radius = height.div(2)

  Button(
    onClick = onClick,
    modifier = Modifier
      .height(height)
      .customOuterShadow(
        borderRadius = radius,
        height = height
          .plus(4.dp)
          .toPx()
      ),
    contentPadding = PaddingValues(horizontal = dimensionResource(id = R.dimen.distance_small)),
    colors = colors,
    shape = RoundedCornerShape(radius),
    border = BorderStroke(2.dp, borderColor)
  ) {
    Text(
      text = text,
      fontSize = 14.sp,
      fontFamily = FontFamily(Font(R.font.open_sans_bold)),
      color = MaterialTheme.colors.onPrimary
    )
  }
}

@Composable
private fun BottomPagination(viewState: HistoryDetailViewState, viewModel: HistoryDetailProxy) =
  viewState.rangeText(LocalContext.current)?.let {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(0.dp),
      modifier = Modifier.padding(
        vertical = dimensionResource(id = R.dimen.distance_default),
        horizontal = dimensionResource(id = R.dimen.distance_tiny)
      )
    ) {
      if (viewState.allowNavigation) {
        PaginationIcon(
          onClick = { viewModel.moveToDataBegin() },
          icon = R.drawable.ic_double_arrow_right,
          enabled = viewState.shiftLeftEnabled,
          rotate = true
        )
        PaginationIcon(
          onClick = { viewModel.moveRangeLeft() },
          enabled = viewState.shiftLeftEnabled,
          rotate = true
        )
      }
      Text(
        text = it,
        style = MaterialTheme.typography.caption,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f)
      )
      if (viewState.allowNavigation) {
        PaginationIcon(
          onClick = { viewModel.moveRangeRight() },
          enabled = viewState.shiftRightEnabled
        )
        PaginationIcon(
          onClick = { viewModel.moveToDataEnd() },
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
private fun RangeSelection(viewState: HistoryDetailViewState, viewModel: HistoryDetailProxy) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .padding(horizontal = dimensionResource(id = R.dimen.distance_default))
      .height(96.dp)
  ) {
    Spacer(modifier = Modifier.weight(0.01f))
    DateTextField(date = viewState.range?.start) { viewModel.customRangeEditDate(RangeValueType.START) }
    HourTextField(date = viewState.range?.start) { viewModel.customRangeEditHour(RangeValueType.START) }
    Text(text = "-", modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.distance_tiny)))
    DateTextField(date = viewState.range?.end) { viewModel.customRangeEditDate(RangeValueType.END) }
    HourTextField(date = viewState.range?.end) { viewModel.customRangeEditHour(RangeValueType.END) }
    Spacer(modifier = Modifier.weight(0.01f))
  }
}

context(RowScope)
@Composable
fun DateTextField(date: Date?, onClick: () -> Unit) {
  TextField(
    value = LocalContext.current.valuesFormatter.getDateString(date) ?: "",
    modifier = Modifier
      .weight(0.3f)
      .height(36.dp),
    contentPadding = PaddingValues(8.dp),
    textStyle = MaterialTheme.typography.body2.copy(textAlign = TextAlign.Center),
    readOnly = true,
    onClicked = onClick
  )
}

context(RowScope)
@Composable
fun HourTextField(date: Date?, onClick: () -> Unit) {
  TextField(
    value = LocalContext.current.valuesFormatter.getHourString(date) ?: "",
    modifier = Modifier
      .weight(0.18f)
      .height(36.dp),
    contentPadding = PaddingValues(8.dp),
    textStyle = MaterialTheme.typography.body2.copy(textAlign = TextAlign.Center),
    readOnly = true,
    onClicked = onClick
  )
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
      HistoryDetail(PreviewProxy())
    }
  }
}

private class PreviewProxy : HistoryDetailProxy {
  override fun getViewState(): StateFlow<HistoryDetailViewState> =
    MutableStateFlow(
      value = HistoryDetailViewState()
    )
}
