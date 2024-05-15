package org.supla.android.ui.views.charts

import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.chart.ChartParameters
import org.supla.android.data.model.chart.datatype.ChartData
import org.supla.android.data.model.chart.style.ChartStyle
import org.supla.android.extensions.toPx
import org.supla.android.extensions.valuesFormatter
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.channel.valueformatter.HumidityValueFormatter
import java.util.Date

@Composable
fun CombinedChart(
  data: ChartData,
  emptyChartMessage: String,
  withRightAxis: Boolean,
  withLeftAxis: Boolean,
  maxLeftAxis: Float?,
  maxRightAxis: Float?,
  chartParametersProvider: () -> ChartParameters?,
  positionEvents: (scaleX: Float, scaleY: Float, x: Float, y: Float) -> Unit,
  chartStyle: ChartStyle,
  channelFunction: Int,
  modifier: Modifier = Modifier
) {
  val valuesFormatter = LocalContext.current.valuesFormatter
  val combinedData = data.combinedData(LocalContext.current.resources)
  val chartParameters = if (combinedData != null) chartParametersProvider() else null
  val xAxisFormatter by remember { mutableStateOf(AxisXFormatter(valuesFormatter)) }
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
        it.xAxis.valueFormatter = xAxisFormatter
        it.xAxis.labelCount = 6
        it.xAxis.textColor = onBackgroundColor
        // Others
        it.data = combinedData
        it.background = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.background, null))
        it.legend.isEnabled = false
        it.description.isEnabled = false
        it.onChartGestureListener = ChartObserver(positionEvents, it)
        it.setNoDataTextColor(onBackgroundColor)
        it.marker = ChartMarkerView(context).apply { chartView = it }
        it.setDrawMarkers(true)
        it.setDrawBarShadow(chartStyle.drawBarShadow)

        chartParameters?.apply {
          if (scaleX == 1f && scaleY == 1f && x == 0f && y == 0f) {
            it.fitScreen() // reset scale
          } else {
            it.zoom(scaleX, scaleY, x, y, YAxis.AxisDependency.LEFT)
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
      chart.axisLeft.valueFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
          return data.leftAxisFormatter.format(value.toDouble(), withUnit = false, 1)
        }
      }
      chart.axisLeft.isEnabled = withLeftAxis
      if (isNotGpm(channelFunction)) {
        combinedData?.allData?.minOfOrNull { entry -> entry.yMin }?.let { yMin ->
          chart.axisLeft.axisMinimum = if (yMin > 0) 0f else yMin
        }
      }
      maxLeftAxis?.let {
        chart.axisLeft.axisMaximum = it
      }

      // Right axis
      chart.axisRight.valueFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
          val withUnit = data.rightAxisFormatter is HumidityValueFormatter
          return data.rightAxisFormatter.format(value.toDouble(), withUnit = withUnit, 1)
        }
      }
      chart.axisRight.isEnabled = withRightAxis
      maxRightAxis?.let {
        chart.axisRight.axisMaximum = if (it > 100) it else 100f
      }

      chart.notifyDataSetChanged()
      chart.setNoDataText(emptyChartMessage)
      chart.invalidate()

      chartParameters?.apply {
        if (scaleX == 1f && scaleY == 1f && x == 0f && y == 0f) {
          chart.fitScreen() // reset scale
        } else if (chart.scaleX != scaleX || chart.scaleY != scaleY) {
          chart.zoom(scaleX, scaleY, x, y, YAxis.AxisDependency.LEFT)
        }
      }
    }
  )
}

private class AxisXFormatter(
  private val valuesFormatter: ValuesFormatter
) : ValueFormatter() {

  lateinit var converter: ChartData

  override fun getAxisLabel(value: Float, axis: AxisBase?): String {
    val distanceInDays = converter.distanceInDays ?: 1
    return when {
      distanceInDays <= 1 ->
        valuesFormatter.getHourString(Date(converter.fromCoordinate(value).times(1000).toLong())) ?: ""

      else -> {
        valuesFormatter.getMonthString(Date(converter.fromCoordinate(value).times(1000).toLong())) ?: ""
      }
    }
  }
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
    val centerPosition = chart.getValuesByTouchPoint(centerPoint.x, centerPoint.y, YAxis.AxisDependency.LEFT)
    positionEvents(chart.viewPortHandler.scaleX, chart.viewPortHandler.scaleY, centerPosition.x.toFloat(), centerPosition.y.toFloat())
  }

  override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
    val centerPoint = chart.viewPortHandler.contentCenter
    val centerPosition = chart.getValuesByTouchPoint(centerPoint.x, centerPoint.y, YAxis.AxisDependency.LEFT)
    positionEvents(chart.viewPortHandler.scaleX, chart.viewPortHandler.scaleY, centerPosition.x.toFloat(), centerPosition.y.toFloat())
  }
}

private fun isNotGpm(function: Int): Boolean =
  function != SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER &&
    function != SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
