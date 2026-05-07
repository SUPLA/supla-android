package org.supla.android.data.model.settings
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
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

enum class ListValueAggregation(override val label: LocalizedString) : SpinnerItem {
  NO_AGGREGATION(localizedString(R.string.impulse_counter_settings_counter_status)),
  LAST_24_HOURS(localizedString(R.string.history_range_last_day)),
  LAST_7_DAYS(localizedString(R.string.history_range_last_week)),
  LAST_30_DAYS(localizedString(R.string.history_range_last_30_days)),
  LAST_90_DAYS(localizedString(R.string.history_range_last_90_days)),
  LAST_365_DAYS(localizedString(R.string.history_range_last_365_days)),
  CURRENT_HOUR(localizedString(R.string.general_current_hour)),
  CURRENT_DAY(localizedString(R.string.general_current_day)),
  CURRENT_WEEK(localizedString(R.string.general_current_week)),
  CURRENT_MONTH(localizedString(R.string.general_current_month)),
  CURRENT_YEAR(localizedString(R.string.general_current_year));

  fun aggregationStartDate(currentDateTime: ZonedDateTime): ZonedDateTime? =
    when (this) {
      LAST_24_HOURS -> currentDateTime.minusHours(23).withMinute(0).withSecond(0).withNano(0)
      LAST_7_DAYS -> currentDateTime.minusDays(6).startOfDay
      LAST_30_DAYS -> currentDateTime.minusDays(29).startOfDay
      LAST_90_DAYS -> currentDateTime.minusDays(89).startOfDay
      LAST_365_DAYS -> currentDateTime.minusDays(364).startOfDay
      CURRENT_HOUR ->
        currentDateTime
          .withMinute(0)
          .withSecond(0)
          .withNano(0)
      CURRENT_DAY ->
        currentDateTime.startOfDay
      CURRENT_WEEK ->
        currentDateTime
          .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
          .startOfDay
      CURRENT_MONTH ->
        currentDateTime
          .withDayOfMonth(1)
          .startOfDay
      CURRENT_YEAR ->
        currentDateTime
          .withMonth(1)
          .withDayOfMonth(1)
          .startOfDay
      NO_AGGREGATION -> null
    }
}

private val ZonedDateTime.startOfDay
  get() = withHour(0)
    .withMinute(0)
    .withSecond(0)
    .withNano(0)
