package org.supla.android.features.details.thermostatdetail.schedule.extensions
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

import androidx.annotation.ColorRes
import org.supla.android.R
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram

@ColorRes
fun SuplaScheduleProgram.colorRes(): Int = when (this) {
  SuplaScheduleProgram.OFF -> R.color.disabled
  SuplaScheduleProgram.PROGRAM_1 -> R.color.light_blue
  SuplaScheduleProgram.PROGRAM_2 -> R.color.light_green
  SuplaScheduleProgram.PROGRAM_3 -> R.color.light_orange
  SuplaScheduleProgram.PROGRAM_4 -> R.color.light_red
}

fun SuplaScheduleProgram.number(): Int = when (this) {
  SuplaScheduleProgram.PROGRAM_1 -> 1
  SuplaScheduleProgram.PROGRAM_2 -> 2
  SuplaScheduleProgram.PROGRAM_3 -> 3
  SuplaScheduleProgram.PROGRAM_4 -> 4
  else -> 0
}
