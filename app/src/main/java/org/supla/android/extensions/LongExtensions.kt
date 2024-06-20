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

import java.util.Date

fun Long.asDate(): Date = Date(this)

val Long.days: Int
  get() = this.div(DAY_IN_SEC).toInt()

val Long.hours: Int
  get() = this.div(HOUR_IN_SEC).toInt()

val Long.hoursInDay: Int
  get() = this.mod(DAY_IN_SEC).div(HOUR_IN_SEC)

val Long.minutesInHour: Int
  get() = this.mod(HOUR_IN_SEC).div(MINUTE_IN_SEC)

val Long.secondsInMinute: Int
  get() = this.mod(MINUTE_IN_SEC)
