package org.supla.android.data.model.chart.datatype
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

import android.content.res.Resources
import com.github.mikephil.charting.data.CombinedData
import org.supla.android.data.model.chart.ChannelChartSets
import org.supla.android.data.model.chart.ChartDataAggregation
import org.supla.android.data.model.chart.ChartEntryType
import org.supla.android.data.model.chart.ChartRange
import org.supla.android.data.model.chart.ChartState
import org.supla.android.data.model.chart.DateRange
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifLet
import org.supla.android.extensions.toTimestamp
import org.supla.android.usecases.channel.valueformatter.ChannelValueFormatter
import kotlin.math.roundToInt

const val CHART_TOP_MARGIN = 0.2f

/**
 * For bar chart we need to place all values next to each other
 * (distance between values must be equal to 1: x2-x1 = 1).
 * Otherwise bar chart is not displayed correctly.
 */
interface CoordinatesConverter {

  val divider: Long

  /**
   * Converts from coordinate value to real value.
   *
   * @param x - x to convert
   * @return x multiplied by [divider]
   */
  fun fromCoordinate(x: Float): Float

  /**
   * Converts from real value to coordinate value.
   *
   * @param x - x to convert
   * @return x divided by [divider]
   */
  fun toCoordinate(x: Float): Float
}

abstract class CombinedChartData(
  dateRange: DateRange?,
  chartRange: ChartRange?,
  aggregation: ChartDataAggregation?,
  sets: List<ChannelChartSets>
) : ChartData(dateRange, chartRange, aggregation, sets) {
  abstract fun combinedData(resources: Resources): CombinedData?
}

abstract class ChartData(
  val dateRange: DateRange?,
  val chartRange: ChartRange?,
  val aggregation: ChartDataAggregation?,
  val sets: List<ChannelChartSets>
) : CoordinatesConverter {

  override val divider: Long = 1

  open val xMin: Float?
    get() {
      if (chartMarginNotNeeded()) {
        return toCoordinate(dateRange?.start?.toTimestamp()?.toFloat())
      }
      val (daysCount) = guardLet(dateRange?.daysCount) { return dateRange?.start?.toTimestamp()?.toFloat() }
      return toCoordinate(dateRange?.start?.toTimestamp()?.minus(chartRangeMargin(daysCount))?.toFloat())
    }

  open val xMax: Float?
    get() {
      if (chartMarginNotNeeded()) {
        return toCoordinate(dateRange?.end?.toTimestamp()?.toFloat())
      }
      val (daysCount) = guardLet(dateRange?.daysCount) { return dateRange?.end?.toTimestamp()?.toFloat() }
      return toCoordinate(dateRange?.end?.toTimestamp()?.plus(chartRangeMargin(daysCount))?.toFloat())
    }

  val leftAxisFormatter: ChannelValueFormatter
    get() = sets.map { it.dataSets.firstOrNull { set -> set.type.leftAxis() }?.valueFormatter }.firstOrNull() ?: DefaultValueFormatter()

  val rightAxisFormatter: ChannelValueFormatter
    get() = sets.map { it.dataSets.firstOrNull { set -> set.type.rightAxis() }?.valueFormatter }.firstOrNull() ?: DefaultValueFormatter()

  val distanceInDays: Int? = dateRange?.daysCount

  val isEmpty: Boolean
    get() {
      var result = true
      sets
        .flatMap { it.dataSets }
        .forEach {
          if (it.isEmpty.not()) {
            result = false
          }
        }
      return result
    }

  val noActiveSet: Boolean
    get() = sets.flatMap { it.dataSets }.firstOrNull { it.active } == null

  val onlyOneSetAndActive: Boolean
    get() = sets.size == 1 && sets.first().dataSets.size == 1 && sets.first().active

  val visibleSets: List<ChartState.VisibleSet>
    get() =
      sets.mapNotNull { channelSets ->
        if (channelSets.active) {
          channelSets.dataSets.filter { it.active }.map { ChartState.VisibleSet(channelSets.remoteId, it.type) }
        } else {
          null
        }
      }.flatten()

  protected val minDate: Long
    get() = sets.minOfOrNull { it.dataSets.minOf { set -> set.minDate } } ?: 0L

  protected val maxDate: Long
    get() = sets.maxOfOrNull { it.dataSets.maxOf { set -> set.maxDate } } ?: 0L

  fun empty(): ChartData = newInstance(emptySets())

  fun toggleActive(remoteId: Int, type: ChartEntryType): ChartData =
    newInstance(sets.map { if (it.remoteId == remoteId) it.toggleActive(type) else it })

  fun activateSets(visibleSets: List<ChartState.VisibleSet>?): ChartData =
    newInstance(
      sets.map { set ->
        visibleSets?.let { list ->
          if (list.map { it.remoteId }.contains(set.remoteId)) {
            set.setActive(visibleSets.map { it.type })
          } else {
            set.deactivate()
          }
        } ?: set
      }
    )

  open fun getAxisMaxValue(filter: (ChartEntryType) -> Boolean): Float? {
    val maxValue = getAxisMaxValueRaw(filter)
    val minValue = getAxisMinValueRaw(filter)

    ifLet(minValue, maxValue) { (min, max) ->
      return if (max == min) {
        if (max == 0f) {
          2f
        } else {
          max.minus(max.times(CHART_TOP_MARGIN))
        }
      } else {
        max.times(CHART_TOP_MARGIN.plus(1)).minus(min.times(CHART_TOP_MARGIN))
      }
    }

    return null
  }

  override fun fromCoordinate(x: Float): Float =
    x.times(divider)

  override fun toCoordinate(x: Float): Float = toCoordinate(x as Float?)!!

  protected abstract fun newInstance(sets: List<ChannelChartSets>): ChartData

  protected fun getAxisMinValueRaw(filter: (ChartEntryType) -> Boolean): Float? =
    sets
      .flatMap { it.dataSets }
      .filter { filter(it.type) }
      .mapNotNull { it.min }
      .minOfOrNull { it }

  private fun getAxisMaxValueRaw(filter: (ChartEntryType) -> Boolean): Float? =
    sets
      .flatMap { it.dataSets }
      .filter { filter(it.type) }
      .mapNotNull { it.max }
      .maxOfOrNull { it }

  private fun toCoordinate(x: Float?): Float? = x?.div(divider)?.roundToInt()?.toFloat()

  private fun emptySets(): List<ChannelChartSets> = sets.map { it.empty() }

  private fun chartMarginNotNeeded() = when (chartRange) {
    ChartRange.LAST_DAY,
    ChartRange.LAST_WEEK,
    ChartRange.LAST_MONTH,
    ChartRange.LAST_QUARTER,
    ChartRange.LAST_YEAR,
    ChartRange.CUSTOM,
    ChartRange.ALL_HISTORY -> false

    else -> true
  }

  private fun chartRangeMargin(daysCount: Int): Int {
    val (aggregation) = guardLet(aggregation) {
      return when {
        daysCount <= 1 -> 60 * 60 // 1 hour in seconds
        else -> 24 * 60 * 60 // 1 day in seconds
      }
    }

    return aggregation.timeInSec.times(0.6).toInt()
  }
}

private class DefaultValueFormatter : ChannelValueFormatter {
  override fun handle(function: Int): Boolean = true

  override fun format(value: Any, withUnit: Boolean, precision: ChannelValueFormatter.Precision, custom: Any?): String {
    (value as? Double)?.let {
      return String.format("%.2f", it)
    }
    (value as? Float)?.let {
      return String.format("%.2f", it)
    }
    (value as? Int)?.let {
      return "$it"
    }

    return value.toString()
  }
}
