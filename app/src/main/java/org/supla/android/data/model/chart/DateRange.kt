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

import org.supla.android.extensions.dayEnd
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.monthEnd
import org.supla.android.extensions.monthStart
import org.supla.android.extensions.nextMonth
import org.supla.android.extensions.previousMonth
import org.supla.android.extensions.quarterEnd
import org.supla.android.extensions.quarterStart
import org.supla.android.extensions.shift
import org.supla.android.extensions.shiftByHour
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.extensions.yearEnd
import org.supla.android.extensions.yearStart
import java.util.Date
import kotlin.math.abs

private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000
data class DateRange(
  val start: Date,
  val end: Date
) {

  val daysCount: Int
    get() = abs(end.time - start.time).div(DAY_IN_MILLIS).toInt()

  val minAggregation: ChartDataAggregation
    get() = when {
      daysCount <= 31 -> ChartDataAggregation.MINUTES
      daysCount <= 92 -> ChartDataAggregation.HOURS
      else -> ChartDataAggregation.DAYS
    }

  val maxAggregation: ChartDataAggregation
    get() = when {
      daysCount <= 1 -> ChartDataAggregation.HOURS
      daysCount <= 31 -> ChartDataAggregation.DAYS
      daysCount <= 420 -> ChartDataAggregation.MONTHS
      else -> ChartDataAggregation.YEARS
    }

  fun shift(range: ChartRange, forward: Boolean): DateRange =
    when (range) {
      ChartRange.LAST_DAY,
      ChartRange.LAST_WEEK,
      ChartRange.LAST_MONTH,
      ChartRange.LAST_QUARTER -> shift(if (forward) range.roundedDaysCount else -range.roundedDaysCount)

      ChartRange.DAY -> if (forward) nextDay() else previousDay()
      ChartRange.WEEK -> if (forward) nextWeek() else previousWeek()
      ChartRange.MONTH -> if (forward) nextMonth() else previousMonth()
      ChartRange.QUARTER -> if (forward) nextQuarter() else previousQuarter()
      ChartRange.YEAR -> if (forward) nextYear() else previousYear()
      ChartRange.CUSTOM -> customRangeShift(forward)
    }

  private fun customRangeShift(forward: Boolean): DateRange {
    return shift(if (forward) daysCount else -daysCount)
  }

  private fun shift(days: Int): DateRange {
    return copy(
      start = start.shift(days),
      end = end.shift(days)
    )
  }

  private fun previousDay(): DateRange {
    val start = start.dayStart().shiftByHour(-1).dayStart()
    return copy(
      start = start,
      end = start.dayEnd()
    )
  }

  private fun nextDay(): DateRange {
    val start = start.dayEnd().shiftByHour(1).dayStart()
    return copy(
      start = start,
      end = start.dayEnd()
    )
  }

  private fun previousWeek(): DateRange {
    val start = start.weekStart().shift(-1).weekStart()
    return copy(
      start = start,
      end = start.weekEnd()
    )
  }

  private fun nextWeek(): DateRange {
    val start = start.weekEnd().shift(1).weekStart()
    return copy(
      start = start,
      end = start.weekEnd()
    )
  }

  private fun previousMonth(): DateRange {
    val start = start.previousMonth().monthStart()
    return copy(
      start = start,
      end = start.monthEnd()
    )
  }

  private fun nextMonth(): DateRange {
    val start = start.nextMonth().monthStart()
    return copy(
      start = start,
      end = start.monthEnd()
    )
  }

  private fun previousQuarter(): DateRange {
    val start = start.quarterStart().previousMonth().quarterStart()
    return copy(
      start = start,
      end = start.quarterEnd()
    )
  }

  private fun nextQuarter(): DateRange {
    val start = end.quarterEnd().nextMonth().quarterStart()
    return copy(
      start = start,
      end = start.quarterEnd()
    )
  }

  private fun previousYear(): DateRange {
    val start = start.yearStart().previousMonth().yearStart()
    return copy(
      start = start,
      end = start.yearEnd()
    )
  }

  private fun nextYear(): DateRange {
    val start = end.yearEnd().nextMonth().yearStart()
    return copy(
      start = start,
      end = start.yearEnd()
    )
  }
}
