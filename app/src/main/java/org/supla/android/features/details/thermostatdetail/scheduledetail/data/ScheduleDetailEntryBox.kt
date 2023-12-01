package org.supla.android.features.details.thermostatdetail.scheduledetail.data
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

import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram

@JvmInline
value class ScheduleDetailEntryBoxKey private constructor(private val packed: Int) {
  constructor(dayOfWeek: DayOfWeek, hour: Short) : this(dayOfWeek.day.times(100).plus(hour))

  val dayOfWeek: DayOfWeek
    get() = DayOfWeek.from(packed.div(100))

  val hour: Short
    get() = packed.mod(100).toShort()

  fun copy(): ScheduleDetailEntryBoxKey = ScheduleDetailEntryBoxKey(packed)
}

data class ScheduleDetailEntryBoxValue(
  val firstQuarterProgram: SuplaScheduleProgram,
  val secondQuarterProgram: SuplaScheduleProgram,
  val thirdQuarterProgram: SuplaScheduleProgram,
  val fourthQuarterProgram: SuplaScheduleProgram
) {

  constructor(singleProgram: SuplaScheduleProgram) : this(singleProgram, singleProgram, singleProgram, singleProgram)

  fun singleProgram(): SuplaScheduleProgram? =
    if (hasSingleProgram()) {
      firstQuarterProgram
    } else {
      null
    }

  fun copy(singleProgram: SuplaScheduleProgram): ScheduleDetailEntryBoxValue = copy(
    firstQuarterProgram = singleProgram,
    secondQuarterProgram = singleProgram,
    thirdQuarterProgram = singleProgram,
    fourthQuarterProgram = singleProgram
  )

  private fun hasSingleProgram(): Boolean =
    firstQuarterProgram == secondQuarterProgram &&
      secondQuarterProgram == thirdQuarterProgram &&
      thirdQuarterProgram == fourthQuarterProgram
}
