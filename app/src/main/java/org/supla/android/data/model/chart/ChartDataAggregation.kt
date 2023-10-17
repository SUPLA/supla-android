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

import androidx.annotation.StringRes
import org.supla.android.R

enum class ChartDataAggregation(@StringRes val stringRes: Int, val timeInSec: Long) {
  MINUTES(R.string.minutes, 60),
  HOURS(R.string.hours, 3600),
  DAYS(R.string.days, 86400),
  MONTHS(R.string.months, 2592000),
  YEARS(R.string.years, 946080000);

  fun between(min: ChartDataAggregation, max: ChartDataAggregation): Boolean =
    timeInSec >= min.timeInSec && timeInSec <= max.timeInSec
}
