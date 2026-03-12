package org.supla.android.data.formatting
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
import org.supla.android.R
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimeFormatter {
  private val defaultFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")

  fun format(dateTime: LocalDateTime): LocalizedString {
    val now = LocalDateTime.now()
    val duration = Duration.between(dateTime, now)
    val minutes = duration.toMinutes()
    val hours = duration.toHours()

    return when {
      minutes < 1 -> localizedString(R.string.time_just_now)
      minutes < 60 -> LocalizedString.Quantity(R.plurals.time_last_minutes, minutes.toInt())
      hours < 24 -> LocalizedString.Quantity(R.plurals.time_last_hours, hours.toInt())
      else -> LocalizedString.Constant(dateTime.format(defaultFormatter))
    }
  }
}

val LocalDateTimeFormatter = compositionLocalOf { DateTimeFormatter() }
