package org.supla.android.ui.views

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.ValueFormatter
import org.supla.android.R
import org.supla.android.extensions.valuesFormatter

@Composable
fun ThermostatChart(data: CombinedData?, modifier: Modifier = Modifier) {
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
            val humidity = value.times(10).toInt()
            if (humidity > 100) {
              return ""
            }
            return "$humidity%"
          }
        }
        it.xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        it.description.isEnabled = false
        it.onChartGestureListener
      }
    }
  )
}
