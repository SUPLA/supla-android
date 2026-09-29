package org.supla.android.ui.views.schedule.editor
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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey

class ScheduleTableStateExtensionsTest {

  @Test
  fun `should convert schedule table boxes to weekly schedule entries`() {
    // given
    val state = ScheduleTableState(
      schedule = mapOf(
        ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 7) to ScheduleTableBox(
          firstQuarterProgram = SuplaScheduleProgram.PROGRAM_1,
          secondQuarterProgram = SuplaScheduleProgram.PROGRAM_2,
          thirdQuarterProgram = SuplaScheduleProgram.PROGRAM_3,
          fourthQuarterProgram = SuplaScheduleProgram.PROGRAM_4
        )
      )
    )

    // when
    val result = state.toSuplaScheduleEntries()

    // then
    assertThat(result).containsExactly(
      SuplaWeeklyScheduleEntry(DayOfWeek.TUESDAY, 7, QuarterOfHour.FIRST, SuplaScheduleProgram.PROGRAM_1),
      SuplaWeeklyScheduleEntry(DayOfWeek.TUESDAY, 7, QuarterOfHour.SECOND, SuplaScheduleProgram.PROGRAM_2),
      SuplaWeeklyScheduleEntry(DayOfWeek.TUESDAY, 7, QuarterOfHour.THIRD, SuplaScheduleProgram.PROGRAM_3),
      SuplaWeeklyScheduleEntry(DayOfWeek.TUESDAY, 7, QuarterOfHour.FOURTH, SuplaScheduleProgram.PROGRAM_4)
    )
  }
}
