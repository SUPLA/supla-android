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

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import org.supla.android.R
import org.supla.android.data.model.chart.datatype.PieChartData
import org.supla.android.data.model.chart.style.ChartStyle

@Composable
fun PieChart(
  data: PieChartData,
  emptyChartMessage: String,
  chartStyle: ChartStyle,
  modifier: Modifier = Modifier
) {
  val combinedData = data.pieData(LocalContext.current)

  AndroidView(
    modifier = modifier.fillMaxWidth(),
    factory = { context ->
      com.github.mikephil.charting.charts.PieChart(context).also {
        val onBackgroundColor = ResourcesCompat.getColor(context.resources, R.color.on_background, null)

        // Others
        it.data = combinedData
        it.background = ColorDrawable(ResourcesCompat.getColor(context.resources, R.color.background, null))
        it.legend.isEnabled = false
        it.description.isEnabled = false
        it.setNoDataTextColor(onBackgroundColor)
        it.marker = chartStyle.markerViewProvider(context).apply { chartView = it }
        it.setDrawMarkers(true)
        val typeFace = ResourcesCompat.getFont(context, R.font.open_sans_regular)
        it.setEntryLabelTypeface(typeFace)
        it.setEntryLabelColor(ContextCompat.getColor(context, R.color.on_background))
        it.setEntryLabelTextSize(11f)
        it.setHoleColor(ContextCompat.getColor(context, R.color.background))
      }
    },
    update = { chart ->
      chart.data = null
      chart.data = combinedData
      if (combinedData == null || data.isEmpty) {
        chart.highlightValue(null)
      }

      chart.notifyDataSetChanged()
      chart.setNoDataText(emptyChartMessage)
      chart.invalidate()
    }
  )
}
