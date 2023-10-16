package org.supla.android.features.thermostatdetail.history.data
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

import org.supla.android.R

enum class ChartRange(val stringRes: Int, val roundedDaysCount: Int, val maxAggregation: ChartDataAggregation?) {
  LAST_DAY(R.string.history_range_last_day, 1, ChartDataAggregation.HOURS),
  LAST_WEEK(R.string.history_range_last_week, 7, ChartDataAggregation.DAYS),
  LAST_MONTH(R.string.history_range_last_30_days, 30, ChartDataAggregation.DAYS),
  LAST_QUARTER(R.string.history_range_last_90_days, 90, ChartDataAggregation.DAYS),

  DAY(R.string.history_range_current_day, 1, ChartDataAggregation.HOURS),
  WEEK(R.string.history_range_current_week, 7, ChartDataAggregation.DAYS),
  MONTH(R.string.history_range_current_month, 30, ChartDataAggregation.DAYS),
  QUARTER(R.string.history_range_current_quarter, 90, ChartDataAggregation.MONTHS),
  YEAR(R.string.history_range_current_year, 365, ChartDataAggregation.MONTHS),

  CUSTOM(R.string.history_range_custom, -1, null);
}
