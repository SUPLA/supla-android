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

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import org.supla.android.R
import org.supla.android.extensions.dayNoon
import org.supla.android.extensions.inHalfOfHour
import org.supla.android.extensions.monthHalf
import org.supla.android.extensions.toTimestamp
import org.supla.android.extensions.yearHalf
import org.supla.android.ui.views.SpinnerItem
import java.text.SimpleDateFormat
import java.util.Date

enum class ChartDataAggregation(
  @StringRes val stringRes: Int,
  val timeInSec: Long,
  val aggregator: (Date, Formatter) -> Long,
  val groupTimeProvider: (Date) -> Long // In seconds
) : SpinnerItem {
  MINUTES(
    R.string.minutes,
    600,
    { date, formatter -> date.getAggregationString(formatter).substring(0, 12).toLong() },
    { it.toTimestamp() }
  ),
  HOURS(
    R.string.hours,
    3600,
    { date, formatter -> date.getAggregationString(formatter).substring(0, 10).toLong() },
    { it.inHalfOfHour().toTimestamp() }
  ),
  DAYS(
    R.string.days,
    86400,
    { date, formatter -> date.getAggregationString(formatter).substring(0, 8).toLong() },
    { it.dayNoon().toTimestamp() }
  ),
  MONTHS(
    R.string.months,
    2592000,
    { date, formatter -> date.getAggregationString(formatter).substring(0, 6).toLong() },
    { it.monthHalf().toTimestamp() }
  ),
  YEARS(
    R.string.years,
    31536000,
    { date, formatter -> date.getAggregationString(formatter).substring(0, 4).toLong() },
    { it.yearHalf().toTimestamp() }
  ),
  RANK_HOURS(
    R.string.ranking_of_hours,
    3600,
    { date, formatter -> date.getAggregationString(formatter).substring(8, 10).toLong() },
    { it.inHalfOfHour().toTimestamp() }
  ),
  RANK_WEEKDAYS(
    R.string.ranking_of_weekdays,
    86400,
    { date, formatter -> date.getAggregationString(formatter).substring(12, 13).toLong() },
    { it.dayNoon().toTimestamp() }
  ),
  RANK_MONTHS(
    R.string.ranking_of_months,
    2592000,
    { date, formatter -> date.getAggregationString(formatter).substring(4, 6).toLong() },
    { it.monthHalf().toTimestamp() }
  );

  override val labelRes: Int
    get() = stringRes

  val isRank: Boolean
    get() = when (this) {
      RANK_HOURS, RANK_WEEKDAYS, RANK_MONTHS -> true
      else -> false
    }

  fun colors(context: Context): List<Int>? =
    when (this) {
      RANK_HOURS -> RankingColors.hours
      RANK_WEEKDAYS -> RankingColors.days
      RANK_MONTHS -> RankingColors.months
      else -> null
    }?.map { ContextCompat.getColor(context, it) }

  fun between(min: ChartDataAggregation, max: ChartDataAggregation): Boolean =
    if (isRank) {
      timeInSec <= max.timeInSec
    } else {
      timeInSec >= min.timeInSec && timeInSec <= max.timeInSec
    }

  @SuppressLint("SimpleDateFormat")
  class Formatter : SimpleDateFormat("yyyyMMddHHmmu")

  companion object {
    val defaultEntries = listOf(
      MINUTES,
      HOURS,
      DAYS,
      MONTHS,
      YEARS
    )
  }
}

private fun Date.getAggregationString(formatter: ChartDataAggregation.Formatter): String {
  return formatter.format(this)
}

private object RankingColors {

  val months = listOf(
    R.color.chart_pie_1,
    R.color.chart_pie_2,
    R.color.chart_pie_3,
    R.color.chart_pie_4,
    R.color.chart_pie_5,
    R.color.chart_pie_6,
    R.color.chart_pie_7,
    R.color.chart_pie_8,
    R.color.chart_pie_9,
    R.color.chart_pie_10,
    R.color.chart_pie_11,
    R.color.chart_pie_12
  )
  val hours = listOf(
    R.color.chart_pie_1,
    R.color.chart_pie_2,
    R.color.chart_pie_3,
    R.color.chart_pie_4,
    R.color.chart_pie_5,
    R.color.chart_pie_6,
    R.color.chart_pie_7,
    R.color.chart_pie_8,
    R.color.chart_pie_9,
    R.color.chart_pie_10,
    R.color.chart_pie_11,
    R.color.chart_pie_12,
    R.color.chart_pie_13,
    R.color.chart_pie_14,
    R.color.chart_pie_15,
    R.color.chart_pie_16,
    R.color.chart_pie_17,
    R.color.chart_pie_18,
    R.color.chart_pie_19,
    R.color.chart_pie_20,
    R.color.chart_pie_21,
    R.color.chart_pie_22,
    R.color.chart_pie_23,
    R.color.chart_pie_24
  )

  val days = listOf(
    R.color.chart_pie_1,
    R.color.chart_pie_2,
    R.color.chart_pie_3,
    R.color.chart_pie_4,
    R.color.chart_pie_5,
    R.color.chart_pie_6,
    R.color.chart_pie_7,
  )
}
