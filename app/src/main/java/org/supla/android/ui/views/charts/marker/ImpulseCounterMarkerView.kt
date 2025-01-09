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
import android.content.res.ColorStateList
import android.widget.TableLayout
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.marker.ChartEntryDetails
import org.supla.android.extensions.guardLet
import org.supla.android.usecases.channel.valueformatter.ImpulseCounterValueFormatter
import javax.inject.Inject

data class ImpulseCounterCustomData(
  val unit: String?,
  val price: Float?,
  val currency: String?
) {

  fun priceString(units: Float): String =
    if (price != null && currency != null) {
      String.format("%.2f $currency", units.times(price))
    } else {
      EMPTY
    }

  companion object {
    const val EMPTY = ""
  }
}

@AndroidEntryPoint
class ImpulseCounterMarkerView(context: Context) : BaseMarkerView(context) {

  private val formatter = ImpulseCounterValueFormatter()
  private val tableId: Int = R.id.chart_marker_table_id
  private lateinit var firstRow: Row
  private lateinit var secondRow: Row

  @Inject
  override lateinit var dateFormatter: DateFormatter

  override fun refreshContent(entry: Entry, highlight: Highlight?, details: ChartEntryDetails) {
    title.text = getFormattedDate(details)
    text.visibility = GONE
    range.visibility = GONE

    val table = findViewById(tableId) ?: createTableLayout()
    table.visibility = VISIBLE

    firstRow.hide()
    secondRow.hide()

    val (customData) = guardLet(details.customData as? ImpulseCounterCustomData) { return }

    if (details.aggregation.isRank) {
      val (pieEntry) = guardLet(entry as? PieEntry) { return }
      showRank(pieEntry, details.aggregation, customData, highlight?.x)
    } else {
      showValueWithPrice(entry, customData)
    }
  }

  private fun showValueWithPrice(entry: Entry, customData: ImpulseCounterCustomData) {
    val unit = customData.unit?.let { ImpulseCounterValueFormatter.Data(it) }
    firstRow.icon.setImageResource(R.drawable.ic_phase_point_color)
    firstRow.icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.chart_gpm, null))
    firstRow.value.text = formatter.format(entry.y.toDouble(), custom = unit)
    firstRow.bold()
    firstRow.show(withLabel = false)

    if (customData.price != null && customData.currency != null) {
      secondRow.value.text = customData.priceString(entry.y)
      secondRow.show(withLabel = false)
    }
  }

  private fun showRank(pieEntry: PieEntry, aggregation: ChartDataAggregation, customData: ImpulseCounterCustomData, idx: Float?) {
    val unit = customData.unit?.let { ImpulseCounterValueFormatter.Data(it) }

    idx?.let {
      aggregation.colors(context)?.get(idx.toInt())?.let { color ->
        firstRow.icon.setImageResource(R.drawable.ic_phase_point_color)
        firstRow.icon.imageTintList = ColorStateList.valueOf(color)
      }
    }
    firstRow.icon.setImageResource(R.drawable.ic_phase_point_color)
    firstRow.icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.chart_gpm, null))
    firstRow.value.text = formatter.format(pieEntry.y.toDouble(), custom = unit)
    firstRow.bold()
    firstRow.show(withLabel = false)

    if (customData.price != null && customData.currency != null) {
      secondRow.value.text = customData.priceString(pieEntry.y)
      secondRow.show(withLabel = false)
    }
  }

  private fun createTableLayout(): TableLayout {
    firstRow = Row(iconView(), textView(), textView(alignment = TEXT_ALIGNMENT_VIEW_END), textView(alignment = TEXT_ALIGNMENT_VIEW_END))
    secondRow = Row(iconView(), textView(), textView(alignment = TEXT_ALIGNMENT_VIEW_END), textView(alignment = TEXT_ALIGNMENT_VIEW_END))

    return TableLayoutBuilder()
      .addRow(context, firstRow)
      .addRow(context, secondRow)
      .build(context, tableLayoutParams())
      .also {
        it.id = tableId
        content.addView(it)
      }
  }
}
