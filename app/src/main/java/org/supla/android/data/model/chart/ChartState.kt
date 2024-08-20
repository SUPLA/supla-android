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

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.supla.android.Trace
import org.supla.android.extensions.TAG
import org.supla.android.usecases.channel.measurementsprovider.electricity.ElectricityChartFilters

sealed interface ChartState {
  val aggregation: ChartDataAggregation
  val chartRange: ChartRange
  val dateRange: DateRange?
  val chartParameters: ChartParameters?
  val visibleSets: List<VisibleSet>?

  fun toJson(): String

  @Serializable
  data class VisibleSet(
    val remoteId: Int,
    val type: ChartEntryType
  )
}

@Serializable
data class DefaultChartState(
  override val aggregation: ChartDataAggregation,
  override val chartRange: ChartRange,
  override val dateRange: DateRange? = null,
  override val chartParameters: ChartParameters? = null,
  override val visibleSets: List<ChartState.VisibleSet>? = null
) : ChartState {

  override fun toJson(): String = Json.encodeToString(this)

  companion object {
    fun default(): DefaultChartState =
      DefaultChartState(
        aggregation = ChartDataAggregation.MINUTES,
        chartRange = ChartRange.LAST_WEEK,
        dateRange = null,
        chartParameters = null,
        visibleSets = null
      )

    fun from(text: String): DefaultChartState? =
      try {
        Json.decodeFromString<DefaultChartState>(text)
      } catch (ex: SerializationException) {
        Trace.w(TAG, "Could not restore chart state!", ex)
        null
      }
  }
}

@Serializable
data class ElectricityChartState(
  override val aggregation: ChartDataAggregation,
  override val chartRange: ChartRange,
  override val dateRange: DateRange? = null,
  override val chartParameters: ChartParameters? = null,
  override val visibleSets: List<ChartState.VisibleSet>? = null,
  val customFilters: ElectricityChartFilters? = null
) : ChartState {

  override fun toJson(): String = Json.encodeToString(this)

  companion object {
    fun default(): ElectricityChartState =
      ElectricityChartState(
        aggregation = ChartDataAggregation.MINUTES,
        chartRange = ChartRange.LAST_WEEK,
        dateRange = null,
        chartParameters = null,
        visibleSets = null,
        customFilters = null
      )

    fun from(text: String): ElectricityChartState? =
      try {
        Json.decodeFromString<ElectricityChartState>(text)
      } catch (ex: SerializationException) {
        Trace.w(TAG, "Could not restore chart state!", ex)
        null
      }
  }
}
