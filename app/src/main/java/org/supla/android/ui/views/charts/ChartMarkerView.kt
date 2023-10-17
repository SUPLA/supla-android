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

import android.content.Context
import android.widget.TextView
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import org.supla.android.R
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.extensions.toPx
import org.supla.android.extensions.valuesFormatter
import org.supla.android.extensions.xAsDate

class ChartMarkerView(context: Context) : MarkerView(context, R.layout.view_chart_marker) {

  private val title: TextView = findViewById(R.id.chart_marker_title)
  private val text: TextView = findViewById(R.id.chart_marker_text)

  override fun refreshContent(entry: Entry?, highlight: Highlight?) {
    super.refreshContent(entry, highlight)

    entry?.let {
      title.text = context.valuesFormatter.getFullDateString(it.xAsDate)
      (it.data as? ChartEntryType)?.let { type ->
        text.text = when (type) {
          ChartEntryType.TEMPERATURE -> context.valuesFormatter.getTemperatureString(it.y)
          ChartEntryType.HUMIDITY -> context.valuesFormatter.getHumidityString(it.y.toDouble(), true)
        }
      }
    }
  }

  override fun getOffset(): MPPointF {
    return MPPointF(-width.div(2).toFloat(), -height.toFloat().plus(20.dp.toPx()))
  }
}
