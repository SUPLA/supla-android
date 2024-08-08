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
import android.graphics.Paint
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet
import org.supla.android.R
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.extensions.DAY_IN_SEC
import org.supla.android.extensions.guardLet

class CandleChartData(
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
    get() = toCoordinate(maxDate.minus(minDate).toFloat()) + 1f

  override fun combinedData(resources: Resources): CombinedData? {
    val candleData = mutableListOf<ICandleDataSet?>().also { list ->
      sets.flatMap { it.dataSets }
        .forEach { dataSet ->
          dataSet.asCandleChartData(
            aggregation = aggregation!!,
            timeToCoordinateConverter = { toCoordinate(it - minDate) },
            toSetConverter = { set -> candleDataSet(set, dataSet.label, dataSet.type, resources) }
          )?.let { data -> list.addAll(data) }
        }
    }

    return if (candleData.isEmpty()) {
      null
    } else {
      CombinedData().apply {
        setData(CandleData(candleData))
      }
    }
  }

  override fun newInstance(sets: List<ChannelChartSets>): ChartData = CandleChartData(dateRange!!, chartRange!!, aggregation!!, sets)

  override fun fromCoordinate(x: Float): Float {
    return super.fromCoordinate(x) + minDate
  }

  private fun candleDataSet(set: List<CandleEntry>, label: HistoryDataSet.Label, type: ChartEntryType, resources: Resources) =
    CandleDataSet(set, "").apply {
      setDrawValues(false)
      when (label) {
        is HistoryDataSet.Label.Single -> color = ResourcesCompat.getColor(resources, label.value.color, null)
        is HistoryDataSet.Label.Multiple -> colors = label.colors(resources)
      }

      axisDependency = when (type) {
        ChartEntryType.HUMIDITY -> YAxis.AxisDependency.RIGHT
        else -> YAxis.AxisDependency.LEFT
      }
      highLightColor = ResourcesCompat.getColor(resources, R.color.primary, null)

      shadowColor = ResourcesCompat.getColor(resources, R.color.on_background, null)
      shadowWidth = 0.7f

      decreasingColor = ResourcesCompat.getColor(resources, R.color.error, null)
      decreasingPaintStyle = Paint.Style.FILL
      increasingColor = ResourcesCompat.getColor(resources, R.color.primary, null)
      increasingPaintStyle = Paint.Style.FILL

      neutralColor = ResourcesCompat.getColor(resources, R.color.blue, null)
    }

  private fun expectedEntitiesCount(): Int? {
    val (daysCount, timeInSec) = guardLet(dateRange?.daysCount, aggregation?.timeInSec?.toInt()) {
      return null
    }
    return daysCount.times(DAY_IN_SEC).div(timeInSec)
  }
}
