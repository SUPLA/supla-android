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
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.marker.ChartEntryDetails
import org.supla.android.features.details.electricitymeterdetail.history.ElectricityMeterChartType
import org.supla.android.usecases.channel.measurementsprovider.electricity.ElectricityChartFilters
import org.supla.android.usecases.channel.measurementsprovider.electricity.PhaseItem
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ElectricityMeterValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.forChartMarker
import javax.inject.Inject

data class ElectricityMarkerCustomData(
  val filters: ElectricityChartFilters?,
  val price: Float?,
  val currency: String?
) {

  private val showPrice: Boolean
    get() = when (filters?.type) {
      ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY,
      ElectricityMeterChartType.BALANCE_VECTOR,
      ElectricityMeterChartType.BALANCE_ARITHMETIC,
      ElectricityMeterChartType.BALANCE_HOURLY -> true

      else -> false
    }

  fun priceString(units: Float): String =
    if (price != null && price != 0f && currency != null && showPrice) {
      String.format("%.2f $currency", units.times(price))
    } else {
      EMPTY
    }

  companion object {
    const val EMPTY = ""
  }
}

@AndroidEntryPoint
class ElectricityMarkerView(context: Context) : BaseMarkerView(context) {

  private val formatter = ElectricityMeterValueFormatter()
  private val tableId: Int = R.id.chart_marker_table_id
  private lateinit var firstRow: Row
  private lateinit var secondRow: Row
  private lateinit var thirdRow: Row
  private lateinit var fourthRow: Row

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
    thirdRow.hide()
    fourthRow.hide()

    val (customData) = guardLet(details.customData as? ElectricityMarkerCustomData) { return }
    val (filters) = guardLet(customData.filters) { return }

    if (details.aggregation.isRank) {
      val (pieEntry) = guardLet(entry as? PieEntry) { return }
      showRank(pieEntry, details.aggregation, customData, highlight?.x)
    } else {
      val (barEntry) = guardLet(entry as? BarEntry) { return }

      when (filters.type) {
        ElectricityMeterChartType.REVERSED_ACTIVE_ENERGY,
        ElectricityMeterChartType.FORWARDED_ACTIVE_ENERGY,
        ElectricityMeterChartType.REVERSED_REACTIVE_ENERGY,
        ElectricityMeterChartType.FORWARDED_REACTIVE_ENERGY ->
          showPhases(filters.selectedPhases, highlight, barEntry, customData)

        ElectricityMeterChartType.BALANCE_HOURLY,
        ElectricityMeterChartType.BALANCE_VECTOR,
        ElectricityMeterChartType.BALANCE_ARITHMETIC -> showBalanceTwoValues(highlight, barEntry, customData)

        ElectricityMeterChartType.BALANCE_CHART_AGGREGATED -> showBalanceThreeValues(highlight, barEntry)

        ElectricityMeterChartType.VOLTAGE,
        ElectricityMeterChartType.CURRENT,
        ElectricityMeterChartType.POWER_ACTIVE -> {} // nothing to do, as here is line chart used
      }
    }
  }

  private fun showPhases(
    selectedPhases: Set<PhaseItem>,
    highlight: Highlight?,
    barEntry: BarEntry,
    customData: ElectricityMarkerCustomData
  ) {
    val rows = arrayOf(firstRow, secondRow, thirdRow, fourthRow)
    var yIdx = 0
    var sum = 0f

    PhaseItem.entries.forEach { phase ->
      if (selectedPhases.contains(phase)) {
        rows[yIdx].icon.setImageResource(R.drawable.ic_phase_point_color)
        rows[yIdx].icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, phase.color, null))
        rows[yIdx].label.text = if (selectedPhases.size > 1) context.getText(phase.label) else ""
        if (highlight?.stackIndex == yIdx || selectedPhases.size == 1) rows[yIdx].bold() else rows[yIdx].regular()
        sum += barEntry.yVals[yIdx]
        rows[yIdx].value.text = formatter.format(barEntry.yVals[yIdx], forChartMarker())
        rows[yIdx].cost.text = customData.priceString(barEntry.yVals[yIdx])
        rows[yIdx].show()

        yIdx++
      }
    }

    if (selectedPhases.size > 1) {
      rows[yIdx].label.text = context.getText(R.string.details_em_sum)
      rows[yIdx].value.text = formatter.format(sum, forChartMarker())
      rows[yIdx].cost.text = customData.priceString(sum)
      rows[yIdx].show(withIcon = false)
    }
  }

  private fun showBalanceTwoValues(highlight: Highlight?, barEntry: BarEntry, customData: ElectricityMarkerCustomData) {
    firstRow.icon.setImageResource(R.drawable.ic_forward_energy)
    firstRow.icon.imageTintList = null
    firstRow.value.text = formatter.format(barEntry.yVals[0], forChartMarker())
    firstRow.cost.text = customData.priceString(barEntry.yVals[0])
    if (highlight?.stackIndex == 0) firstRow.bold() else firstRow.regular()
    firstRow.show(withLabel = false)

    secondRow.icon.setImageResource(R.drawable.ic_reversed_energy)
    secondRow.icon.imageTintList = null
    secondRow.value.text = formatter.format(barEntry.yVals[1], forChartMarker())
    secondRow.cost.text = ElectricityMarkerCustomData.EMPTY
    if (highlight?.stackIndex == 1) secondRow.bold() else secondRow.regular()
    secondRow.show(withLabel = false)
  }

  private fun showBalanceThreeValues(highlight: Highlight?, barEntry: BarEntry) {
    firstRow.icon.setImageResource(R.drawable.ic_phase_point_color)
    firstRow.icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.on_surface_variant, null))
    firstRow.value.text = formatter.format(barEntry.yVals[3], forChartMarker())
    firstRow.cost.text = ElectricityMarkerCustomData.EMPTY
    if (highlight?.stackIndex == 3) firstRow.bold() else firstRow.regular()
    firstRow.show(withLabel = false)

    secondRow.icon.setImageResource(R.drawable.ic_forward_energy)
    secondRow.icon.imageTintList = null
    secondRow.value.text = formatter.format(barEntry.yVals[1], forChartMarker())
    secondRow.cost.text = ElectricityMarkerCustomData.EMPTY
    if (highlight?.stackIndex == 1) secondRow.bold() else secondRow.regular()
    secondRow.show(withLabel = false)

    thirdRow.icon.setImageResource(R.drawable.ic_reversed_energy)
    thirdRow.icon.imageTintList = null
    thirdRow.value.text = formatter.format(barEntry.yVals[2], forChartMarker())
    thirdRow.cost.text = ElectricityMarkerCustomData.EMPTY
    if (highlight?.stackIndex == 2) thirdRow.bold() else thirdRow.regular()
    thirdRow.show(withLabel = false)

    fourthRow.icon.setImageResource(R.drawable.ic_phase_point_color)
    fourthRow.icon.imageTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.on_surface_variant, null))
    fourthRow.value.text = formatter.format(barEntry.yVals[0], forChartMarker())
    fourthRow.cost.text = ElectricityMarkerCustomData.EMPTY
    if (highlight?.stackIndex == 0) fourthRow.bold() else fourthRow.regular()
    fourthRow.show(withLabel = false)
  }

  private fun showRank(pieEntry: PieEntry, aggregation: ChartDataAggregation, customData: ElectricityMarkerCustomData, idx: Float?) {
    idx?.let {
      aggregation.colors(context)?.get(idx.toInt())?.let { color ->
        firstRow.icon.setImageResource(R.drawable.ic_phase_point_color)
        firstRow.icon.imageTintList = ColorStateList.valueOf(color)
      }
    }
    firstRow.value.text = formatter.format(pieEntry.value, forChartMarker())
    firstRow.cost.text = customData.priceString(pieEntry.value)
    firstRow.regular()
    firstRow.show(withLabel = false)
  }

  private fun createTableLayout(): TableLayout {
    firstRow = Row(iconView(), textView(), textView(alignment = TEXT_ALIGNMENT_VIEW_END), textView(alignment = TEXT_ALIGNMENT_VIEW_END))
    secondRow = Row(iconView(), textView(), textView(alignment = TEXT_ALIGNMENT_VIEW_END), textView(alignment = TEXT_ALIGNMENT_VIEW_END))
    thirdRow = Row(iconView(), textView(), textView(alignment = TEXT_ALIGNMENT_VIEW_END), textView(alignment = TEXT_ALIGNMENT_VIEW_END))
    fourthRow = Row(iconView(), textView(), textView(alignment = TEXT_ALIGNMENT_VIEW_END), textView(alignment = TEXT_ALIGNMENT_VIEW_END))

    return TableLayoutBuilder()
      .addRow(context, firstRow)
      .addRow(context, secondRow)
      .addRow(context, thirdRow)
      .addRow(context, fourthRow)
      .build(context, tableLayoutParams())
      .also {
        it.id = tableId
        content.addView(it)
      }
  }
}
