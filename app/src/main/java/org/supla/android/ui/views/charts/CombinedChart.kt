package org.supla.android.ui.views.charts
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

import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.ViewPortHandler
import org.supla.android.R
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.formatting.LocalDateFormatter
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartParameters
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.datatype.CombinedChartData
import org.supla.android.data.model.chart.style.ChartStyle
import org.supla.android.data.model.chart.style.HumidityChartStyle
import org.supla.android.data.model.chart.style.TemperatureChartStyle
import org.supla.android.extensions.toPx
import org.supla.core.shared.usecase.channel.valueformatter.formatters.HumidityValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValuePrecision.Companion.exact
import org.supla.core.shared.usecase.channel.valueformatter.types.custom
import java.util.Date

@Composable
fun CombinedChart(
  data: CombinedChartData,
  emptyChartMessage: String,
  withRightAxis: Boolean,
  withLeftAxis: Boolean,
  maxLeftAxis: Float?,
  maxRightAxis: Float?,
  chartParametersProvider: () -> ChartParameters?,
  positionEvents: (scaleX: Float, scaleY: Float, x: Float, y: Float) -> Unit,
  chartStyle: ChartStyle,
  modifier: Modifier = Modifier
) {
  val dateFormatter = LocalDateFormatter.current
  val combinedData = data.combinedData(LocalResources.current)
  val xAxisFormatter by remember { mutableStateOf(CombinedChartAxisXFormatter(dateFormatter)) }
  var rememberedChartStyle by remember { mutableStateOf(chartStyle) }
  xAxisFormatter.converter = data

  AndroidView(
    modifier = modifier.fillMaxWidth(),
    factory = { context ->
      CombinedChart(context).also {
        val onBackgroundColor = ResourcesCompat.getColor(context.resources, R.color.on_background, null)

        // Left axis
        it.axisLeft.setDrawAxisLine(false)
        it.axisLeft.enableGridDashedLine(3.dp.toPx(), 3.dp.toPx(), 6.dp.toPx())
        it.axisLeft.textColor = ResourcesCompat.getColor(context.resources, chartStyle.leftAxisColor, null)
        it.axisLeft.gridColor = it.axisLeft.textColor
        it.axisLeft.zeroLineColor = onBackgroundColor
        // Right axis
        it.axisRight.setDrawAxisLine(false)
        it.axisRight.enableGridDashedLine(3.dp.toPx(), 3.dp.toPx(), 6.dp.toPx())
        it.axisRight.textColor = ResourcesCompat.getColor(context.resources, chartStyle.rightAxisColor, null)
        it.axisRight.gridColor = it.axisRight.textColor
        it.axisRight.zeroLineColor = onBackgroundColor
        it.axisRight.axisMinimum = 0f
        it.axisRight.axisMaximum = 100f
        // X axis
        it.xAxis.setDrawGridLines(false)
        it.xAxis.setDrawAxisLine(false)
        it.xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxisFormatter.chart = it
        xAxisFormatter.handler = it.viewPortHandler
        it.xAxis.valueFormatter = xAxisFormatter
        it.xAxis.labelCount = 5
        it.xAxis.textColor = onBackgroundColor
        // Others
        it.data = combinedData
        it.background = ResourcesCompat.getColor(context.resources, R.color.background, null).toDrawable()
        it.legend.isEnabled = false
        it.description.isEnabled = false
        it.onChartGestureListener = ChartObserver(positionEvents, it)
        it.setNoDataTextColor(onBackgroundColor)
        it.marker = chartStyle.markerViewProvider(context).apply { chartView = it }
        it.setDrawMarkers(true)
        it.setDrawBarShadow(chartStyle.drawBarShadow)

        val chartParameters = if (combinedData != null) chartParametersProvider() else null
        chartParameters?.apply {
          if (scaleX == 1f && scaleY == 1f && x == 0f && y == 0f) {
            it.fitScreen() // reset scale
          } else {
            it.zoom(scaleX, scaleY, x, y, AxisDependency.LEFT)
          }
        }
      }
    },
    update = { chart ->
      chart.data = null
      chart.data = combinedData
      if (combinedData == null || data.isEmpty) {
        chart.highlightValue(null)
      }
      data.xMin?.let { chart.xAxis.axisMinimum = it }
      data.xMax?.let { chart.xAxis.axisMaximum = it }

      // Left axis
      chart.axisLeft.textColor = ResourcesCompat.getColor(chart.context.resources, chartStyle.leftAxisColor, null)
      chart.axisLeft.gridColor = chart.axisLeft.textColor
      chart.axisLeft.valueFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
          val format = when (chartStyle) {
            is TemperatureChartStyle -> ValueFormat.TemperatureWithDegree
            is HumidityChartStyle -> ValueFormat.WithUnit
            else -> ValueFormat.WithUnit
          }

          return data.leftAxisFormatter.format(
            value = value.toDouble(),
            format = format.copy(
              precision = custom(exact(chart.axisLeft.mDecimals)),
              showNoValueText = false
            )
          )
        }
      }
      chart.axisLeft.isEnabled = withLeftAxis
      if (chartStyle.setMinValue) {
        combinedData?.allData?.minOfOrNull { entry -> entry.yMin }?.let { yMin ->
          chart.axisLeft.axisMinimum = if (yMin > 0) 0f else yMin
        }
      }
      if (chartStyle.setMaxValue) {
        maxLeftAxis?.let {
          chart.axisLeft.axisMaximum = it
        }
      }

      // Right axis
      chart.axisRight.textColor = ResourcesCompat.getColor(chart.context.resources, chartStyle.rightAxisColor, null)
      chart.axisRight.gridColor = chart.axisRight.textColor
      chart.axisRight.valueFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
          val withUnit = data.rightAxisFormatter is HumidityValueFormatter
          return data.rightAxisFormatter.format(
            value = value.toDouble(),
            format = ValueFormat(
              withUnit = withUnit,
              precision = custom(exact(chart.axisRight.mDecimals)),
              showNoValueText = false
            )
          )
        }
      }
      chart.axisRight.isEnabled = withRightAxis
      maxRightAxis?.let {
        chart.axisRight.axisMaximum = if (it > 100) it else 100f
      }

      chart.notifyDataSetChanged()
      chart.setNoDataText(emptyChartMessage)
      chart.invalidate()

      val chartParameters = if (combinedData != null) chartParametersProvider() else null
      chartParameters?.apply {
        if (scaleX == 1f && scaleY == 1f && x == 0f && y == 0f) {
          chart.fitScreen() // reset scale
        } else if (chart.scaleX != scaleX || chart.scaleY != scaleY) {
          chart.zoom(scaleX, scaleY, x, y, AxisDependency.LEFT)
        }
      }

      if (rememberedChartStyle != chartStyle) {
        chart.marker = chartStyle.markerViewProvider(chart.context).apply { chartView = chart }
        chart.setDrawBarShadow(chartStyle.drawBarShadow)

        if (!chartStyle.setMaxValue) {
          chart.axisLeft.resetAxisMaximum()
        }
        if (!chartStyle.setMinValue) {
          chart.axisLeft.resetAxisMinimum()
        }

        rememberedChartStyle = chartStyle
      }
    }
  )
}

private class ChartObserver(
  private val positionEvents: (scaleX: Float, scaleY: Float, x: Float, y: Float) -> Unit,
  private val chart: CombinedChart
) : OnChartGestureListener {
  override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}

  override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}

  override fun onChartLongPressed(me: MotionEvent?) {}

  override fun onChartDoubleTapped(me: MotionEvent?) {}

  override fun onChartSingleTapped(me: MotionEvent?) {}

  override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}

  override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
    val centerPoint = chart.viewPortHandler.contentCenter
    val centerPosition = chart.getValuesByTouchPoint(centerPoint.x, centerPoint.y, AxisDependency.LEFT)
    positionEvents(chart.viewPortHandler.scaleX, chart.viewPortHandler.scaleY, centerPosition.x.toFloat(), centerPosition.y.toFloat())
  }

  override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
    val centerPoint = chart.viewPortHandler.contentCenter
    val centerPosition = chart.getValuesByTouchPoint(centerPoint.x, centerPoint.y, AxisDependency.LEFT)
    positionEvents(chart.viewPortHandler.scaleX, chart.viewPortHandler.scaleY, centerPosition.x.toFloat(), centerPosition.y.toFloat())
  }
}

private class CombinedChartAxisXFormatter(
  private val dateFormatter: DateFormatter
) : ValueFormatter() {

  lateinit var converter: ChartData
  lateinit var chart: CombinedChart
  lateinit var handler: ViewPortHandler

  override fun getAxisLabel(value: Float, axis: AxisBase?): String {
    val left = chart.getValuesByTouchPoint(handler.contentLeft(), handler.contentTop(), AxisDependency.LEFT)
    val right = chart.getValuesByTouchPoint(handler.contentRight(), handler.contentTop(), AxisDependency.LEFT)

    val distanceInDaysFromChart = converter.distanceInDays(left.x.toFloat(), right.x.toFloat())
    val distanceInDays = converter.distanceInDays ?: 1
    return when {
      converter.aggregation == ChartDataAggregation.YEARS ->
        dateFormatter.getYearString(Date(converter.fromCoordinate(value).toLong().times(1000))) ?: ""

      distanceInDays > 1.1 && distanceInDaysFromChart <= 2 ->
        dateFormatter.getDayAndHourShortDateString(Date(converter.fromCoordinate(value).toLong().times(1000))) ?: ""

      distanceInDaysFromChart <= 1.1 ->
        dateFormatter.getHourString(Date(converter.fromCoordinate(value).toLong().times(1000))) ?: ""

      else -> dateFormatter.getMonthString(Date(converter.fromCoordinate(value).toLong().times(1000))) ?: ""
    }
  }
}
