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
import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.android.extensions.dayNoon
import org.supla.android.extensions.inHalfOfHour
import org.supla.android.extensions.monthHalf
import org.supla.android.extensions.toTimestamp
import org.supla.android.extensions.yearHalf
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
private val formatter = SimpleDateFormat("yyyyMMddHHmm")

enum class ChartDataAggregation(
  @StringRes val stringRes: Int,
  val timeInSec: Long,
  val aggregator: (Date) -> Long,
  val groupTimeProvider: (Date) -> Long // In seconds
) {
  MINUTES(R.string.minutes, 60, { it.getAggregationString().toLong() }, { it.toTimestamp() }),
  HOURS(R.string.hours, 3600, { it.getAggregationString().substring(0, 10).toLong() }, { it.inHalfOfHour().toTimestamp() }),
  DAYS(R.string.days, 86400, { it.getAggregationString().substring(0, 8).toLong() }, { it.dayNoon().toTimestamp() }),
  MONTHS(
    R.string.months,
    2592000,
    { it.getAggregationString().substring(0, 6).toLong() },
    { it.monthHalf().toTimestamp() }
  ),
  YEARS(
    R.string.years,
    31536000,
    { it.getAggregationString().substring(0, 4).toLong() },
    { it.yearHalf().toTimestamp() }
  );

  fun between(min: ChartDataAggregation, max: ChartDataAggregation): Boolean =
    timeInSec >= min.timeInSec && timeInSec <= max.timeInSec
}

private fun Date.getAggregationString(): String =
  formatter.format(this)
