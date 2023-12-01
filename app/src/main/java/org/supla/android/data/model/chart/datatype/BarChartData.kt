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
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.chart.HistoryDataSet
import org.supla.android.extensions.DAY_IN_SEC
import org.supla.android.extensions.guardLet

class BarChartData(
  dateRange: DateRange,
  chartRange: ChartRange,
  aggregation: ChartDataAggregation,
  sets: List<HistoryDataSet>
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
    val lineDataSets = mutableListOf<IBarDataSet?>().also { list ->
      sets.forEach { set ->
        if (set.active && set.entities.isNotEmpty()) {
          set.entities.forEach { entries ->
            list.add(
              barDataSet(
                entries.map { BarEntry(toCoordinate(it.date.toFloat() - minDate), it.value, set.toDetails(it)) },
                set.color,
                resources
              )
            )
          }
        }
      }
    }

    return if (lineDataSets.isEmpty()) {
      null
    } else {
      CombinedData().apply {
        setData(
          BarData(lineDataSets).also {
            it.barWidth = 0.7f
          }
        )
      }
    }
  }

  override fun newInstance(sets: List<HistoryDataSet>): ChartData = BarChartData(dateRange!!, chartRange!!, aggregation!!, sets)

  override fun fromCoordinate(x: Float): Float {
    return super.fromCoordinate(x) + minDate
  }

  private fun barDataSet(set: List<BarEntry>, @ColorRes colorRes: Int, resources: Resources) =
    BarDataSet(set, "").apply {
      setDrawValues(false)
      color = ResourcesCompat.getColor(resources, colorRes, null)

      this.barShadowColor = ResourcesCompat.getColor(resources, android.R.color.transparent, null)
    }

  private fun expectedEntitiesCount(): Int? {
    val (daysCount, timeInSec) = guardLet(dateRange?.daysCount, aggregation?.timeInSec?.toInt()) {
      return null
    }
    return daysCount.times(DAY_IN_SEC).div(timeInSec)
  }
}
