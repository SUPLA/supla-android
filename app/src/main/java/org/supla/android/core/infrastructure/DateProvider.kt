package org.supla.android.core.infrastructure
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

import androidx.compose.runtime.compositionLocalOf
import org.supla.android.data.source.local.calendar.DayOfWeek
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateProvider @Inject constructor() {

  fun currentDate() = Date()

  fun currentTimestamp() = currentDate().time

  fun currentDayOfWeek(): DayOfWeek {
    val calendar = Calendar.getInstance()
    return DayOfWeek.from(calendar.get(Calendar.DAY_OF_WEEK) - 1)
  }

  fun currentHour(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY)
  }

  fun currentMinute(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.MINUTE)
  }
}

val LocalDateProvider = compositionLocalOf { DateProvider() }
