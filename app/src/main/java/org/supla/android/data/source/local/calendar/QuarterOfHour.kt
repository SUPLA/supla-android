package org.supla.android.data.source.local.calendar
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

enum class QuarterOfHour(val startingMinute: Int) {
  FIRST(0),
  SECOND(15),
  THIRD(30),
  FOURTH(45);

  val startingMinuteString: String
    get() = if (startingMinute < 10) "0$startingMinute" else "$startingMinute"
}
