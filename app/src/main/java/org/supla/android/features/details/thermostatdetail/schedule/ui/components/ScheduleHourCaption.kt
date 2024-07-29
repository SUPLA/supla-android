package org.supla.android.features.details.thermostatdetail.schedule.ui.components
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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import org.supla.android.data.source.local.calendar.QuarterOfHour

@Composable
fun ScheduleHourCaption(hour: Short, withQuarter: QuarterOfHour? = null) {
  ScheduleHourCaption(hour = hour.toInt(), withQuarter = withQuarter)
}

@Composable
fun ScheduleHourCaption(hour: Int, withQuarter: QuarterOfHour? = null) {
  Text(
    text = hour.toHour(withQuarter),
    style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (withQuarter != null) 14.sp else 11.sp),
    textAlign = TextAlign.Left
  )
}

fun Int.toHour(withQuarter: QuarterOfHour? = null): String =
  (if (this < 10) "0$this" else "$this").let first@{ string ->
    return withQuarter?.let second@{
      return@second "$string:${withQuarter.startingMinuteString}"
    } ?: string
  }

fun Short.toHour(withQuarter: QuarterOfHour?): String =
  this.toInt().toHour(withQuarter = withQuarter)
