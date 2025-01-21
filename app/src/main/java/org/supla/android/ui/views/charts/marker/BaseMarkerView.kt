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

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import org.supla.android.R
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartDataAggregation.RANK_HOURS
import org.supla.android.data.model.chart.ChartDataAggregation.RANK_MONTHS
import org.supla.android.data.model.chart.ChartDataAggregation.RANK_WEEKDAYS
import org.supla.android.data.model.chart.marker.ChartEntryDetails
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.toPx
import org.supla.android.extensions.ucFirst
import java.time.DayOfWeek
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

abstract class BaseMarkerView(context: Context) : MarkerView(context, R.layout.view_chart_marker) {

  protected val content: ConstraintLayout = findViewById(R.id.chart_marker_content)
  protected val title: TextView = findViewById(R.id.chart_marker_title)
  protected val icon: AppCompatImageView = findViewById(R.id.chart_marker_icon)
  protected val text: TextView = findViewById(R.id.chart_marker_text)
  protected val range: TextView = findViewById(R.id.chart_marker_range)

  abstract val dateFormatter: DateFormatter

  protected abstract fun refreshContent(entry: Entry, highlight: Highlight?, details: ChartEntryDetails)

  @Suppress("NAME_SHADOWING")
  override fun refreshContent(entry: Entry?, highlight: Highlight?) {
    val (entry) = guardLet(entry) {
      super.refreshContent(entry, highlight)
      return
    }
    val (details) = guardLet(entry.data as? ChartEntryDetails) {
      super.refreshContent(entry, highlight)
      return
    }

    refreshContent(entry, highlight, details)

    super.refreshContent(entry, highlight)
  }

  override fun getOffset(): MPPointF {
    return MPPointF(-width.div(2).toFloat(), -height.toFloat().plus(20.dp.toPx()))
  }

  fun getFormattedDate(details: ChartEntryDetails) =
    when (details.aggregation) {
      ChartDataAggregation.HOURS -> "${dateFormatter.getFullDateString(details.date())?.let { it.substring(0, it.length - 2) }}00"
      ChartDataAggregation.DAYS -> dateFormatter.getFullDateString(details.date())?.let { it.substring(0, it.length - 5) }
      ChartDataAggregation.MONTHS -> dateFormatter.getMonthAndYearString(details.date())?.ucFirst()
      ChartDataAggregation.YEARS -> dateFormatter.getYearString(details.date())
      RANK_HOURS, RANK_WEEKDAYS, RANK_MONTHS -> details.aggregation.label(details.date)
      else -> dateFormatter.getFullDateString(details.date())
    }

  protected fun iconView(): ImageView =
    ImageView(context)

  protected fun textView(text: String? = null, alignment: Int = View.TEXT_ALIGNMENT_VIEW_START): TextView {
    return TextView(context).apply {
      this.text = text
      textAlignment = alignment
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
      typeface = ResourcesCompat.getFont(context, R.font.open_sans_regular)
      setTextColor(ContextCompat.getColor(context, R.color.on_background))
    }
  }

  protected fun tableLayoutParams() =
    ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).also {
      it.topToBottom = R.id.chart_marker_text
      it.topMargin = resources.getDimension(R.dimen.distance_micro).toInt()
    }

  data class Row(
    val icon: ImageView,
    val label: TextView,
    val value: TextView,
    val cost: TextView
  ) {
    fun hide() {
      icon.visibility = GONE
      (icon.layoutParams as? LinearLayout.LayoutParams)?.marginEnd = 0
      label.visibility = GONE
      value.visibility = GONE
      cost.visibility = GONE
    }

    fun show(withIcon: Boolean = true, withLabel: Boolean = true) {
      if (withIcon) {
        icon.visibility = VISIBLE
        (icon.layoutParams as? LinearLayout.LayoutParams)?.marginEnd = 4.dp.toPx().toInt()
      }
      if (withLabel) {
        label.visibility = VISIBLE
      }
      value.visibility = VISIBLE
      if (cost.text.isNotEmpty()) {
        cost.visibility = VISIBLE
      }
    }
  }

  protected fun Row.bold() {
    val typeface = ResourcesCompat.getFont(context, R.font.open_sans_bold)
    label.typeface = typeface
    value.typeface = typeface
    cost.typeface = typeface
  }

  protected fun Row.regular() {
    val typeface = ResourcesCompat.getFont(context, R.font.open_sans_regular)
    label.typeface = typeface
    value.typeface = typeface
    cost.typeface = typeface
  }

  private fun ChartDataAggregation.label(value: Long): String =
    when (this) {
      RANK_HOURS -> resources.getString(R.string.details_em_hour_marker_title, value.toInt())
      RANK_WEEKDAYS -> DayOfWeek.of(value.toInt()).getDisplayName(TextStyle.FULL, Locale.getDefault()).ucFirst()
      RANK_MONTHS -> Month.of(value.toInt()).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()).ucFirst()
      else -> ""
    }
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

  fun addRow(context: Context, row: BaseMarkerView.Row): TableLayoutBuilder {
    addRow()
    tableStructure.last().apply {
      val layout = LinearLayout(context)
      layout.gravity = Gravity.CENTER_VERTICAL
      val iconSize = context.resources.getDimension(R.dimen.icon_small_size).toInt()
      row.icon.layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
      row.icon.scaleType = ImageView.ScaleType.CENTER_INSIDE
      row.label.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
      layout.addView(row.icon)
      layout.addView(row.label)

      add(layout)
      add(row.value)
      add(row.cost)
    }
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
        cell.layoutParams = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
          marginEnd = 4.dp.toPx().toInt()
          gravity = Gravity.CENTER_VERTICAL
        }
        tableRow.addView(cell)
      }

      tableLayout.addView(tableRow)
    }

    return tableLayout
  }
}
