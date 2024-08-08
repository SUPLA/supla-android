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

import org.supla.android.usecases.channel.measurementsprovider.ElectricityChartFilters

sealed interface ChartState {
  val aggregation: ChartDataAggregation
  val chartRange: ChartRange
  val dateRange: DateRange?
  val chartParameters: ChartParameters?
  val visibleSets: List<VisibleSet>?

  data class VisibleSet(
    val remoteId: Int,
    val type: ChartEntryType
  )
}

data class DefaultChartState(
  override val aggregation: ChartDataAggregation,
  override val chartRange: ChartRange,
  override val dateRange: DateRange? = null,
  override val chartParameters: ChartParameters? = null,
  override val visibleSets: List<ChartState.VisibleSet>? = null
) : ChartState {
  companion object {
    fun default(): ChartState =
      DefaultChartState(
        aggregation = ChartDataAggregation.MINUTES,
        chartRange = ChartRange.LAST_WEEK,
        dateRange = null,
        chartParameters = null,
        visibleSets = null
      )
  }
}

data class ElectricityChartState(
  override val aggregation: ChartDataAggregation,
  override val chartRange: ChartRange,
  override val dateRange: DateRange? = null,
  override val chartParameters: ChartParameters? = null,
  override val visibleSets: List<ChartState.VisibleSet>? = null,
  val customFilters: ElectricityChartFilters? = null
) : ChartState {
  companion object {
    fun default(): ChartState =
      ElectricityChartState(
        aggregation = ChartDataAggregation.MINUTES,
        chartRange = ChartRange.LAST_WEEK,
        dateRange = null,
        chartParameters = null,
        visibleSets = null,
        customFilters = null
      )
  }
}
