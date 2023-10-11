package org.supla.android.ui.views

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.ValueFormatter
import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.thermostatdetail.history.data.ChartRange
import java.util.Date

const val HUMIDITY_SCALE_FACTOR = 2

@Composable
fun ThermostatChart(data: CombinedData?, range: ChartRange?, emptyChartMessage: String, modifier: Modifier = Modifier) {
  val valuesFormatter = LocalContext.current.valuesFormatter
  val xAxisFormatter by remember { mutableStateOf(AxisXFormatter(range, valuesFormatter)) }
  xAxisFormatter.range = range

  AndroidView(
    modifier = modifier.fillMaxWidth(),
    factory = { context ->
      com.github.mikephil.charting.charts.CombinedChart(context).also {
        it.data = data
        it.background = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.background, null))
        it.xAxis.setDrawGridLines(false)
        it.xAxis.setDrawAxisLine(false)
        it.legend.isEnabled = false
        it.axisLeft.setDrawAxisLine(false)
        it.axisLeft.valueFormatter = object : ValueFormatter() {
          override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return context.valuesFormatter.getTemperatureString(value)
          }
        }
        it.axisRight.setDrawAxisLine(false)
        it.axisRight.valueFormatter = object : ValueFormatter() {
          override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return context.valuesFormatter.getHumidityString(value.times(HUMIDITY_SCALE_FACTOR).toDouble(), withPercentage = true)
          }
        }
        it.xAxis.position = XAxis.XAxisPosition.BOTTOM
        it.xAxis.valueFormatter = xAxisFormatter
        it.description.isEnabled = false
        it.onChartGestureListener
        it.setNoDataTextColor(ResourcesCompat.getColor(context.resources, R.color.on_background, null))
      }
    },
    update = {
      it.data = data
      it.xAxis.setLabelCount(axisCount(range), true)
      it.notifyDataSetChanged()
      it.setNoDataText(emptyChartMessage)
      it.invalidate()
    }
  )
}

private fun axisCount(range: ChartRange?) =
  when(range) {
    ChartRange.DAY -> 6
    ChartRange.WEEK,
    ChartRange.MONTH -> 4
    ChartRange.DAYS_90 -> 5
    else -> 0
  }

private class AxisXFormatter(var range: ChartRange?, val valuesFormatter: ValuesFormatter): ValueFormatter() {

  override fun getAxisLabel(value: Float, axis: AxisBase?): String {
    return when (range) {
      ChartRange.DAY -> {
        valuesFormatter.getHourString(Date(value.times(1000).toLong())) ?: ""
      }
      ChartRange.WEEK,
      ChartRange.MONTH -> {
        valuesFormatter.getDateString(Date(value.times(1000).toLong())) ?: ""
      }
      ChartRange.DAYS_90 -> {
        valuesFormatter.getMonthString(Date(value.times(1000).toLong())) ?: ""
      }
      else -> {
        ""
      }
    }
  }
}