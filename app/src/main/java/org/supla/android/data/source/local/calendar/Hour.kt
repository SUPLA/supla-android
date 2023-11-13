package org.supla.android.data.source.local.calendar

import android.content.Context
import org.supla.android.extensions.valuesFormatter

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

data class Hour(
  val hour: Int,
  val minute: Int
) {

  fun toString(context: Context) = context.valuesFormatter.getHourString(this)

  companion object {
    fun from(string: String): Hour? {
      val regex = Regex("(?<hour>[0-9]{1,2}):(?<minute>[0-9]{1,2})")
      val matchResult = regex.find(string)

      if (matchResult?.groups?.size == 3) {
        return Hour(
          minute = matchResult.groups["minute"]!!.value.toInt(),
          hour = matchResult.groups["hour"]!!.value.toInt()
        )
      }

      return null
    }
  }
}
