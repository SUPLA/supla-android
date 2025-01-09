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
import org.supla.android.extensions.visibleIf
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueUnit
import org.supla.core.shared.usecase.channel.valueformatter.types.withUnit
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
      format = ValueFormat(
        withUnit = showValueUnit(details.type),
        customUnit = showCustomUnit(details.type),
        showNoValueText = false
      )
    )

    val color: Int? = getIconColor(details)
    icon.visibleIf(color != null)
    color?.let { icon.imageTintList = ColorStateList.valueOf(it) }

    val min = details.min
    val max = details.max
    if (min != null && max != null) {
      val minText = details.valueFormatter.format(min.toDouble(), withUnit(details.type == ChartEntryType.HUMIDITY))
      val maxText = details.valueFormatter.format(max.toDouble(), withUnit(details.type == ChartEntryType.HUMIDITY))

      range.text = "($minText - $maxText)"
    } else {
      range.text = ""
    }

    val showOpenClose = details.type == ChartEntryType.GENERAL_PURPOSE_MEASUREMENT && details.open != null && details.close != null
    if (showOpenClose) {
      val table = findViewById(tableId) ?: createTableLayout()
      table.visibility = VISIBLE
      openingValueView.text = details.valueFormatter.format(details.open!!.toDouble(), ValueFormat.WithoutUnit)
      closingValueView.text = details.valueFormatter.format(details.close!!.toDouble(), ValueFormat.WithoutUnit)
    } else {
      findViewById<TableLayout>(tableId)?.visibility = GONE
    }
  }

  private fun createTableLayout(): TableLayout {
    openingValueView = textView(alignment = TEXT_ALIGNMENT_VIEW_END)
    closingValueView = textView(alignment = TEXT_ALIGNMENT_VIEW_END)

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
      ChartEntryType.POWER_ACTIVE,
      ChartEntryType.HUMIDITY_ONLY -> true

      else -> false
    }

  private fun showCustomUnit(type: ChartEntryType) =
    when (type) {
      ChartEntryType.HUMIDITY,
      ChartEntryType.HUMIDITY_ONLY -> ValueUnit.HUMIDITY.toString()

      ChartEntryType.TEMPERATURE,
      ChartEntryType.PRESET_TEMPERATURE,
      ChartEntryType.GENERAL_PURPOSE_MEASUREMENT,
      ChartEntryType.GENERAL_PURPOSE_METER,
      ChartEntryType.IMPULSE_COUNTER,
      ChartEntryType.ELECTRICITY,
      ChartEntryType.VOLTAGE,
      ChartEntryType.CURRENT,
      ChartEntryType.POWER_ACTIVE -> null
    }

  private fun getIconColor(details: ChartEntryDetails): Int? {
    (details as? ChartEntryDetails.WithPhase)?.let {
      return ResourcesCompat.getColor(resources, it.phase.color, null)
    }

    return null
  }
}
