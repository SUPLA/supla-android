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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.extensions.DAY_IN_SEC
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.toTimestamp
import kotlin.math.abs

class BarChartData(
  dateRange: DateRange,
  chartRange: ChartRange,
  aggregation: ChartDataAggregation,
  sets: List<ChannelChartSets>
) : ChartData(dateRange, chartRange, aggregation, sets) {

  override val divider: Long
    get() = aggregation?.timeInSec ?: 1

  override val xMin: Float
    get() {
      val (expectedEntitiesCount) = guardLet(expectedEntitiesCount()) { return -1f }
      return if (xMax >= expectedEntitiesCount) {
        -1f
      } else {
        xMax - expectedEntitiesCount - 1
      }
    }

  override val xMax: Float
    get() = toCoordinate(dateRange!!.end.toTimestamp().minus(minDate).toFloat()) + 1f

  override fun combinedData(resources: Resources): CombinedData? {
    val barDataSets = mutableListOf<IBarDataSet?>().also { list ->
      sets
        .forEach { channelSet ->
          channelSet.dataSets.forEach { dataSet ->
            dataSet.asBarChartData(
              aggregation = aggregation!!,
              customData = channelSet.customData,
              timeToCoordinateConverter = { toCoordinate(it - minDate) },
              toSetConverter = { set -> barDataSet(set, dataSet.label, resources) }
            )
              ?.let { data -> list.addAll(data) }
          }
        }
    }

    return if (barDataSets.isEmpty()) {
      null
    } else {
      CombinedData().apply {
        setData(
          BarData(barDataSets).also {
            it.barWidth = 0.7f
          }
        )
      }
    }
  }

  override fun newInstance(sets: List<ChannelChartSets>): ChartData = BarChartData(dateRange!!, chartRange!!, aggregation!!, sets)

  override fun fromCoordinate(x: Float): Float {
    return super.fromCoordinate(x) + minDate
  }

  override fun getAxisMaxValue(filter: (ChartEntryType) -> Boolean): Float? {
    val maxValue = super.getAxisMaxValue(filter)

    if (maxValue != null) {
      if (maxValue <= 0) {
        val minValue = getAxisMinValueRaw(filter)
        if (minValue != null) {
          return abs(minValue).times(CHART_TOP_MARGIN)
        }
      }
    }

    return maxValue
  }

  private fun barDataSet(set: List<BarEntry>, label: HistoryDataSet.Label, resources: Resources) =
    BarDataSet(set, "").apply {
      setDrawValues(false)

      when (label) {
        is HistoryDataSet.Label.Single -> color = ResourcesCompat.getColor(resources, label.value.color, null)
        is HistoryDataSet.Label.Multiple -> colors = label.colors(resources)
      }

      this.barShadowColor = ResourcesCompat.getColor(resources, android.R.color.transparent, null)
    }

  private fun expectedEntitiesCount(): Int? {
    val (daysCount, timeInSec) = guardLet(dateRange?.daysCount, aggregation?.timeInSec?.toInt()) {
      return null
    }
    return daysCount.times(DAY_IN_SEC).div(timeInSec)
  }
}
