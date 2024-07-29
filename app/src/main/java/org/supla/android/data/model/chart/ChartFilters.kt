package org.supla.android.data.model.chart
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

import org.supla.android.data.model.general.SelectableList
import org.supla.android.ui.views.SpinnerItem

class ChartFilters private constructor(private val filters: Map<FilterableKey, SelectableList<SpinnerItem>>) {

  val selectedAggregation: ChartDataAggregation?
    get() = selected(FilterableKey.AGGREGATION)

  val selectedRange: ChartRange?
    get() = selected(FilterableKey.RANGE_DATE)

  val values = filters.values

  fun count() = filters.keys.size

  @Suppress("UNCHECKED_CAST")
  fun putRanges(values: SelectableList<ChartRange>): ChartFilters =
    ChartFilters(filters.toMutableMap().apply { put(FilterableKey.RANGE_DATE, values as SelectableList<SpinnerItem>) })

  @Suppress("UNCHECKED_CAST")
  fun putAggregations(values: SelectableList<ChartDataAggregation>): ChartFilters =
    ChartFilters(filters.toMutableMap().apply { put(FilterableKey.AGGREGATION, values as SelectableList<SpinnerItem>) })

  fun select(option: ChartRange): ChartFilters =
    ChartFilters(
      filters.toMutableMap().apply {
        get(FilterableKey.RANGE_DATE)?.let { put(FilterableKey.RANGE_DATE, it.copy(selected = option)) }
      }
    )

  fun select(option: ChartDataAggregation): ChartFilters =
    ChartFilters(
      filters.toMutableMap().apply {
        get(FilterableKey.AGGREGATION)?.let { put(FilterableKey.AGGREGATION, it.copy(selected = option)) }
      }
    )

  @Suppress("UNCHECKED_CAST")
  private fun <T : SpinnerItem> selected(type: FilterableKey): T? = filters[type]?.selected as? T

  private enum class FilterableKey {
    RANGE_DATE, AGGREGATION
  }

  companion object {
    operator fun invoke(): ChartFilters = ChartFilters(emptyMap())
  }
}
