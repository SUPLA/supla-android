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

import org.supla.android.R
import org.supla.android.ui.views.SpinnerItem

enum class ChartRange(val stringRes: Int, val roundedDaysCount: Int) : SpinnerItem {
  LAST_DAY(R.string.history_range_last_day, 1),
  LAST_WEEK(R.string.history_range_last_week, 7),
  LAST_MONTH(R.string.history_range_last_30_days, 30),
  LAST_QUARTER(R.string.history_range_last_90_days, 90),
  LAST_YEAR(R.string.history_range_last_365_days, 365),

  DAY(R.string.history_range_current_day, 1),
  WEEK(R.string.history_range_current_week, 7),
  MONTH(R.string.history_range_current_month, 30),
  QUARTER(R.string.history_range_current_quarter, 90),
  YEAR(R.string.history_range_current_year, 365),

  CUSTOM(R.string.history_range_custom, -1),
  ALL_HISTORY(R.string.all_available_history, -1);

  override val labelRes: Int
    get() = stringRes
}
