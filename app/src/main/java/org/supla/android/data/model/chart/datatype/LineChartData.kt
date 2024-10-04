package org.supla.android.data.model.chart.datatype
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

import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.supla.android.R
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet

class LineChartData(
  dateRange: DateRange,
  chartRange: ChartRange,
  aggregation: ChartDataAggregation,
  sets: List<ChannelChartSets>
) : CombinedChartData(dateRange, chartRange, aggregation, sets) {
  override fun combinedData(resources: Resources): CombinedData? {
    val lineDataSets = mutableListOf<ILineDataSet?>().also { list ->
      sets.flatMap { it.dataSets }.forEach {
        it.asLineChartData(aggregation!!) { set -> lineDataSet(set, it.label, it.type, resources) }
          ?.let { data -> list.addAll(data) }
      }
    }

    return if (lineDataSets.isEmpty()) {
      null
    } else {
      CombinedData().apply {
        setData(LineData(lineDataSets))
      }
    }
  }

  override fun newInstance(sets: List<ChannelChartSets>): ChartData = LineChartData(dateRange!!, chartRange!!, aggregation!!, sets)

  private fun lineDataSet(set: List<Entry>, label: HistoryDataSet.Label, type: ChartEntryType, resources: Resources) =
    LineDataSet(set, "").apply {
      setDrawValues(false)
      mode = LineDataSet.Mode.HORIZONTAL_BEZIER
      cubicIntensity = 0.05f
      when (label) {
        is HistoryDataSet.Label.Single -> {
          color = ResourcesCompat.getColor(resources, label.value.color, null)
          circleColors = listOf(color)
        }
        is HistoryDataSet.Label.Multiple -> {
          colors = label.colors(resources)
          circleColors = colors
        }
      }
      setDrawCircleHole(false)
      setDrawCircles(false)
      lineWidth = 2f
      axisDependency = when (type) {
        ChartEntryType.HUMIDITY -> YAxis.AxisDependency.RIGHT
        else -> YAxis.AxisDependency.LEFT
      }
      highLightColor = ResourcesCompat.getColor(resources, R.color.primary, null)

      setDrawFilled(true)
      fillColor = color
      fillAlpha = 15
    }
}
