package org.supla.android.features.details.relayschedule.extensions
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
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.features.details.relayschedule.RelayScheduleViewState
import org.supla.android.features.details.relayschedule.data.RelayScheduleProgram
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.editor.ScheduleTableBox
import org.supla.android.ui.views.schedule.editor.ScheduleTableState
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import org.supla.core.shared.infrastructure.LocalizedString

class RelayScheduleViewStateExtensionsTest {

  @Test
  fun `should create weekly schedule change without synthetic default program`() {
    // given
    val state = RelayScheduleViewState(
      editorState = WeeklyScheduleEditorState(
        programs = listOf(
          RelayScheduleProgram(
            program = SuplaScheduleProgram.PROGRAM_1,
            relayMode = SuplaRelayMode.START_ON,
            relayModeDurationS = 20,
            relayOppositeModeDurationS = 30,
            label = LocalizedString.Constant("program")
          ),
          RelayScheduleProgram.DEFAULT
        ),
        scheduleTableState = ScheduleTableState(
          schedule = mapOf(
            ScheduleDetailEntryBoxKey(DayOfWeek.FRIDAY, 18) to ScheduleTableBox(SuplaScheduleProgram.PROGRAM_1)
          )
        )
      )
    )

    // when
    val result = state.toWeeklyScheduleConfigChange(remoteId = 123)

    // then
    assertThat(result.remoteId).isEqualTo(123)
    assertThat(result.programConfigurations).containsExactly(
      SuplaWeeklyScheduleProgram(
        program = SuplaScheduleProgram.PROGRAM_1,
        relayMode = SuplaRelayMode.START_ON,
        relayModeDurationS = 20,
        relayOppositeModeDurationS = 30
      )
    )
    assertThat(result.schedule).containsExactly(
      SuplaWeeklyScheduleEntry(DayOfWeek.FRIDAY, 18, QuarterOfHour.FIRST, SuplaScheduleProgram.PROGRAM_1),
      SuplaWeeklyScheduleEntry(DayOfWeek.FRIDAY, 18, QuarterOfHour.SECOND, SuplaScheduleProgram.PROGRAM_1),
      SuplaWeeklyScheduleEntry(DayOfWeek.FRIDAY, 18, QuarterOfHour.THIRD, SuplaScheduleProgram.PROGRAM_1),
      SuplaWeeklyScheduleEntry(DayOfWeek.FRIDAY, 18, QuarterOfHour.FOURTH, SuplaScheduleProgram.PROGRAM_1)
    )
  }
}
