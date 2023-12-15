package org.supla.android.ui.views

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
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.utils.ViewPortHandler
import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.chart.ChartParameters
import org.supla.android.extensions.toPx
import org.supla.android.extensions.valuesFormatter
import org.supla.android.ui.views.charts.ChartMarkerView
import java.util.Date

@Composable
fun TemperaturesChart(
  data: CombinedData?,
  rangeStart: Float?,
  rangeEnd: Float?,
  emptyChartMessage: String,
  withHumidity: Boolean,
  withTemperature: Boolean,
  maxTemperature: Float?,
  maxHumidity: Float?,
  chartParameters: ChartParameters?,
  positionEvents: (scaleX: Float, scaleY: Float, x: Float, y: Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val valuesFormatter = LocalContext.current.valuesFormatter
  val xAxisFormatter by remember { mutableStateOf(AxisXFormatter(valuesFormatter)) }

  AndroidView(
    modifier = modifier.fillMaxWidth(),
    factory = { context ->
      CombinedChart(context).also {
        xAxisFormatter.chart = it
        xAxisFormatter.handler = it.viewPortHandler

        val colorBlack = ResourcesCompat.getColor(context.resources, R.color.on_background, null)

        // Left axis
        it.axisLeft.setDrawAxisLine(false)
        it.axisLeft.enableGridDashedLine(3.dp.toPx(), 3.dp.toPx(), 6.dp.toPx())
        it.axisLeft.textColor = ResourcesCompat.getColor(context.resources, R.color.dark_red, null)
        it.axisLeft.gridColor = it.axisLeft.textColor
        it.axisLeft.zeroLineColor = colorBlack
        it.axisLeft.valueFormatter = object : ValueFormatter() {
          override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return context.valuesFormatter.getTemperatureString(value)
          }
        }
        // Right axis
        it.axisRight.setDrawAxisLine(false)
        it.axisRight.enableGridDashedLine(3.dp.toPx(), 3.dp.toPx(), 6.dp.toPx())
        it.axisRight.textColor = ResourcesCompat.getColor(context.resources, R.color.dark_blue, null)
        it.axisRight.gridColor = it.axisRight.textColor
        it.axisRight.zeroLineColor = colorBlack
        it.axisRight.valueFormatter = object : ValueFormatter() {
          override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val originalValue = value.toDouble()
            if (originalValue > 100) {
              return ""
            }
            return context.valuesFormatter.getHumidityString(originalValue, withPercentage = true)
          }
        }
        it.axisRight.axisMinimum = 0f
        it.axisRight.axisMaximum = 100f
        // X axis
        it.xAxis.setDrawGridLines(false)
        it.xAxis.setDrawAxisLine(false)
        it.xAxis.position = XAxis.XAxisPosition.BOTTOM
        it.xAxis.valueFormatter = xAxisFormatter
        it.xAxis.labelCount = 6
        // Others
        it.data = data
        it.background = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.background, null))
        it.legend.isEnabled = false
        it.description.isEnabled = false
        it.onChartGestureListener = ChartObserver(positionEvents, it)
        it.setNoDataTextColor(colorBlack)
        it.marker = ChartMarkerView(context).apply { chartView = it }
        it.setDrawMarkers(true)

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
      chart.data = data
      chart.highlightValue(null)
      rangeStart?.let { chart.xAxis.axisMinimum = it }
      rangeEnd?.let { chart.xAxis.axisMaximum = it }
      chart.axisRight.isEnabled = withHumidity
      chart.axisLeft.isEnabled = withTemperature

      data?.allData?.minOfOrNull { entry -> entry.yMin }?.let { yMin ->
        chart.axisLeft.axisMinimum = if (yMin > 0) {
          0f
        } else {
          yMin
        }
      }
      maxTemperature?.let {
        chart.axisLeft.axisMaximum = it
      }
      maxHumidity?.let {
        chart.axisRight.axisMaximum = if (it > 100) it else 100f
      }

      chart.notifyDataSetChanged()
      chart.setNoDataText(emptyChartMessage)
      chart.invalidate()

      chartParameters?.apply {
        if (scaleX == 1f && scaleY == 1f && x == 0f && y == 0f) {
          chart.fitScreen() // reset scale
        } else {
          chart.zoom(scaleX, scaleY, x, y, YAxis.AxisDependency.LEFT)
        }
      }
    }
  )
}

private class AxisXFormatter(
  private val valuesFormatter: ValuesFormatter
) : ValueFormatter() {

  lateinit var chart: CombinedChart
  lateinit var handler: ViewPortHandler

  override fun getAxisLabel(value: Float, axis: AxisBase?): String {
    val left = chart.getValuesByTouchPoint(handler.contentLeft(), handler.contentBottom(), YAxis.AxisDependency.LEFT)
    val right = chart.getValuesByTouchPoint(handler.contentRight(), handler.contentBottom(), YAxis.AxisDependency.LEFT)

    val distanceInDays = (right.x - left.x).div(60).div(60).div(24).toInt()
    return when {
      distanceInDays <= 1 ->
        valuesFormatter.getHourString(Date(value.times(1000).toLong())) ?: ""

      else -> {
        valuesFormatter.getMonthString(Date(value.times(1000).toLong())) ?: ""
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
