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

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.marker.ChartEntryDetails
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.toPx
import javax.inject.Inject

@AndroidEntryPoint
class ChartMarkerView(context: Context) : MarkerView(context, R.layout.view_chart_marker) {

  private val content: ConstraintLayout = findViewById(R.id.chart_marker_content)
  private val title: TextView = findViewById(R.id.chart_marker_title)
  private val text: TextView = findViewById(R.id.chart_marker_text)
  private val range: TextView = findViewById(R.id.chart_marker_range)

  private val tableId: Int = View.generateViewId()
  private lateinit var openingValueView: TextView
  private lateinit var closingValueView: TextView

  @Inject
  lateinit var dateFormatter: DateFormatter

  @Suppress("NAME_SHADOWING")
  @SuppressLint("SetTextI18n")
  override fun refreshContent(entry: Entry?, highlight: Highlight?) {
    val (entry) = guardLet(entry) {
      super.refreshContent(entry, highlight)
      return
    }
    val (details) = guardLet(entry.data as? ChartEntryDetails) {
      super.refreshContent(entry, highlight)
      return
    }

    title.text = when (details.aggregation) {
      ChartDataAggregation.HOURS -> "${dateFormatter.getFullDateString(details.date())?.let { it.substring(0, it.length - 2) }}00"
      ChartDataAggregation.DAYS -> dateFormatter.getFullDateString(details.date())?.let { it.substring(0, it.length - 5) }
      ChartDataAggregation.MONTHS -> dateFormatter.getMonthAndYearString(details.date())?.capitalize(Locale.current)
      ChartDataAggregation.YEARS -> dateFormatter.getYearString(details.date())
      else -> dateFormatter.getFullDateString(details.date())
    }
    text.text = details.valueFormatter.format(entry.y.toDouble(), withUnit = showValueUnit(details.type), precision = 2)

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

    super.refreshContent(entry, highlight)
  }

  override fun getOffset(): MPPointF {
    return MPPointF(-width.div(2).toFloat(), -height.toFloat().plus(20.dp.toPx()))
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

  private fun tableLayoutParams() =
    ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).also {
      it.topToBottom = R.id.chart_marker_text
      it.topMargin = resources.getDimension(R.dimen.distance_micro).toInt()
    }

  private fun textView(text: String? = null, alignment: Int = View.TEXT_ALIGNMENT_VIEW_START): TextView {
    return TextView(context).apply {
      this.text = text
      textAlignment = alignment
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
      typeface = ResourcesCompat.getFont(context, R.font.open_sans_regular)
      setTextColor(ContextCompat.getColor(context, R.color.on_background))
    }
  }

  private fun showValueUnit(type: ChartEntryType) =
    type == ChartEntryType.HUMIDITY || type == ChartEntryType.GENERAL_PURPOSE_METER
}

class TableLayoutBuilder {

  private val tableStructure = mutableListOf<MutableList<View>>()

  fun addRow(): TableLayoutBuilder {
    tableStructure.add(mutableListOf())
    return this
  }

  fun addCell(view: View): TableLayoutBuilder {
    if (tableStructure.isEmpty()) {
      addRow()
    }
    tableStructure.last().add(view)
    return this
  }

  fun build(context: Context, layoutParams: ViewGroup.LayoutParams): TableLayout {
    val tableLayout = TableLayout(context)
    tableLayout.layoutParams = layoutParams
    tableLayout.isStretchAllColumns = true

    tableStructure.forEach { row ->
      val tableRow = TableRow(context)
      tableRow.layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT)

      row.forEach { cell ->
        cell.layoutParams = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)
        tableRow.addView(cell)
      }

      tableLayout.addView(tableRow)
    }

    return tableLayout
  }
}
