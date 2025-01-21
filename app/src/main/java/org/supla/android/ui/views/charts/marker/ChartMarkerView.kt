package org.supla.android.ui.views.charts.marker
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.marker.ChartEntryDetails
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.extensions.visibleIf
import org.supla.android.usecases.channel.measurementsprovider.electricity.CurrentChartCustomData
import org.supla.android.usecases.channel.measurementsprovider.electricity.PowerActiveChartCustomData
import org.supla.android.usecases.channel.measurementsprovider.electricity.VoltageChartCustomData
import javax.inject.Inject

@AndroidEntryPoint
class ChartMarkerView(context: Context) : BaseMarkerView(context) {

  private val tableId: Int = R.id.chart_marker_table_id
  private lateinit var openingValueView: TextView
  private lateinit var closingValueView: TextView

  @Inject
  override lateinit var dateFormatter: DateFormatter

  @SuppressLint("SetTextI18n")
  override fun refreshContent(entry: Entry, highlight: Highlight?, details: ChartEntryDetails) {
    title.text = getFormattedDate(details)
    text.text = details.valueFormatter.format(
      value = entry.y.toDouble(),
      withUnit = showValueUnit(details.type),
      precision = precision(details.type)
    )

    val color: Int? = getIconColor(highlight, details)
    icon.visibleIf(color != null)
    color?.let { icon.imageTintList = ColorStateList.valueOf(it) }

    if (details.min != null && details.max != null) {
      val minText = details.valueFormatter.format(details.min.toDouble(), withUnit = details.type == ChartEntryType.HUMIDITY)
      val maxText = details.valueFormatter.format(details.max.toDouble(), withUnit = details.type == ChartEntryType.HUMIDITY)

      range.text = "($minText - $maxText)"
    } else {
      range.text = ""
    }

    val showOpenClose = details.type == ChartEntryType.GENERAL_PURPOSE_MEASUREMENT && details.open != null && details.close != null
    if (showOpenClose) {
      val table = findViewById(tableId) ?: createTableLayout()
      table.visibility = VISIBLE
      openingValueView.text = details.valueFormatter.format(details.open!!.toDouble(), withUnit = false)
      closingValueView.text = details.valueFormatter.format(details.close!!.toDouble(), withUnit = false)
    } else {
      findViewById<TableLayout>(tableId)?.visibility = GONE
    }
  }

  private fun createTableLayout(): TableLayout {
    openingValueView = textView(alignment = View.TEXT_ALIGNMENT_VIEW_END)
    closingValueView = textView(alignment = View.TEXT_ALIGNMENT_VIEW_END)

    return TableLayoutBuilder()
      .addCell(textView(text = resources.getString(R.string.chart_marker_opening)))
      .addCell(openingValueView)
      .addRow()
      .addCell(textView(text = resources.getString(R.string.chart_marker_closing)))
      .addCell(closingValueView)
      .build(context, tableLayoutParams())
      .also {
        it.id = tableId
        content.addView(it)
      }
  }

  private fun showValueUnit(type: ChartEntryType) =
    when (type) {
      ChartEntryType.HUMIDITY,
      ChartEntryType.GENERAL_PURPOSE_METER,
      ChartEntryType.IMPULSE_COUNTER,
      ChartEntryType.VOLTAGE,
      ChartEntryType.CURRENT,
      ChartEntryType.POWER_ACTIVE -> true

      else -> false
    }

  private fun precision(type: ChartEntryType) =
    when (type) {
      ChartEntryType.IMPULSE_COUNTER -> 3
      else -> 2
    }

  private fun getIconColor(highlight: Highlight?, details: ChartEntryDetails): Int? {
    (details.customData as? VoltageChartCustomData)?.phases?.let { phases ->
      getColorFromPhases(phases, highlight)?.let {
        return it
      }
    }

    (details.customData as? CurrentChartCustomData)?.phases?.let { phases ->
      getColorFromPhases(phases, highlight)?.let {
        return it
      }
    }

    (details.customData as? PowerActiveChartCustomData)?.phases?.let { phases ->
      getColorFromPhases(phases, highlight)?.let {
        return it
      }
    }

    return null
  }

  private fun getColorFromPhases(phases: List<Phase>, highlight: Highlight?): Int? {
    highlight?.dataSetIndex?.let { dataSetIndex ->
      phases.getOrNull(dataSetIndex)?.color?.let { phaseColor ->
        return ResourcesCompat.getColor(resources, phaseColor, null)
      }
    }

    return null
  }
}
