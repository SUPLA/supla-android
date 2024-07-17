package org.supla.android.data.source.local.calendar

import androidx.annotation.StringRes
import org.supla.android.R

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

enum class DayOfWeek(val day: Int, @StringRes val fullText: Int, @StringRes val shortText: Int) {
  MONDAY(1, R.string.monday, R.string.monday_short),
  TUESDAY(2, R.string.tuesday, R.string.tuesday_short),
  WEDNESDAY(3, R.string.wednesday, R.string.wednesday_short),
  THURSDAY(4, R.string.thursday, R.string.thursday_short),
  FRIDAY(5, R.string.friday, R.string.friday_short),
  SATURDAY(6, R.string.saturday, R.string.saturday_short),
  SUNDAY(0, R.string.sunday, R.string.sunday_short);

  companion object {
    fun from(day: Int): DayOfWeek {
      for (dayOfWeek in entries) {
        if (dayOfWeek.day == day) {
          return dayOfWeek
        }
      }

      throw IllegalArgumentException("Could not find DayOfWeek for day '$day'")
    }
  }
}
