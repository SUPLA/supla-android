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

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import org.supla.android.R
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.DateRange

class PieChartData(
  dateRange: DateRange,
  chartRange: ChartRange,
  aggregation: ChartDataAggregation,
  sets: List<ChannelChartSets>
) : ChartData(dateRange, chartRange, aggregation, sets) {

  fun pieData(context: Context): PieData? {
    val typeFace = ResourcesCompat.getFont(context, R.font.open_sans_regular)

    val dataSets = mutableSetOf<IPieDataSet>().also { list ->
      sets.forEach { channelSet ->
        channelSet.dataSets.forEach { set ->
          set.asPieChartData(
            aggregation = aggregation!!,
            customData = channelSet.customData,
            toSetConverter = { lineDataSet(it, typeFace, aggregation, set.valueFormatter, context) }
          )
            ?.let { list.addAll(it) }
        }
      }
    }
    return if (dataSets.isEmpty()) {
      return null
    } else {
      PieData().apply {
        dataSets.forEach { addDataSet(it) }
      }
    }
  }

  override fun newInstance(sets: List<ChannelChartSets>): ChartData = PieChartData(dateRange!!, chartRange!!, aggregation!!, sets)

  private fun lineDataSet(
    set: List<PieEntry>,
    typeface: Typeface?,
    aggregation: ChartDataAggregation,
    formatter: org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter,
    context: Context
  ) =
    PieDataSet(set, "").apply {
      colors = aggregation.colors(context)
      valueTypeface = typeface
      valueTextSize = 8f
      setValueTextColors(listOf(ContextCompat.getColor(context, R.color.on_background)))
      valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String =
          formatter.format(value)
      }
    }
}
