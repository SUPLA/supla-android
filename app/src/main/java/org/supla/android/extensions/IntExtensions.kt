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

import android.content.res.Resources
import androidx.compose.ui.unit.sp
import java.util.Locale

const val DAY_IN_SEC = 24 * 60 * 60
const val HOUR_IN_SEC = 60 * 60
const val MINUTE_IN_SEC = 60

val Int.nonScaledSp
  get() = (this / Resources.getSystem().configuration.fontScale).sp

val Int.days
  get() = this.div(DAY_IN_SEC)

val Int.hours
  get() = this.div(HOUR_IN_SEC)

val Int.hoursInDay
  get() = this.mod(DAY_IN_SEC).div(HOUR_IN_SEC)

val Int.minutesInHour
  get() = this.mod(HOUR_IN_SEC).div(MINUTE_IN_SEC)

val Int.secondsInMinute
  get() = this.mod(MINUTE_IN_SEC)

val Int.ipV4String: String
  get() =
    String.format(
      Locale.getDefault(),
      "%d.%d.%d.%d",
      (this and 0xff),
      (this shr 8 and 0xff),
      (this shr 16 and 0xff),
      (this shr 24 and 0xff)
    )
