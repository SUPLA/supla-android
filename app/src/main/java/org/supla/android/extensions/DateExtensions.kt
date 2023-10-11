package org.supla.android.extensions
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

import java.util.Calendar
import java.util.Date

fun Date.dayStart(): Date {
  val calendar = Calendar.getInstance()
  calendar.time = this
  calendar.set(Calendar.HOUR_OF_DAY, 0)
  calendar.set(Calendar.MINUTE, 0)
  calendar.set(Calendar.SECOND, 0)
  calendar.set(Calendar.MILLISECOND, 0)

  return calendar.time
}

fun Date.dayEnd(): Date {
  val calendar = Calendar.getInstance()
  calendar.time = this
  calendar.set(Calendar.HOUR_OF_DAY, 23)
  calendar.set(Calendar.MINUTE, 59)
  calendar.set(Calendar.SECOND, 59)
  calendar.set(Calendar.MILLISECOND, 999)

  return calendar.time
}

fun Date.shift(days: Int): Date =
  Date(time + days.toLong().times(24).times(60).times(60).times(1000))
