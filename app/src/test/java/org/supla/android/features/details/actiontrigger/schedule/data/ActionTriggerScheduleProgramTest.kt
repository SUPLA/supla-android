package org.supla.android.features.details.actiontrigger.schedule.data
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
import org.supla.android.R
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT

class ActionTriggerScheduleProgramTest {

  @Test
  fun `should use labels for supported action trigger modes`() {
    assertThat(
      program(SuplaButtonMode.NOT_SET).label
    ).isEqualTo(LocalizedString.WithResourceAndArguments(R.string.schedule_program_default))
    assertThat(program(SuplaButtonMode.LOCKED).label).isEqualTo(LocalizedString.WithResourceAndArguments(R.string.general_locked))
    assertThat(program(SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED).label)
      .isEqualTo(LocalizedString.WithFormat("%s ⏱️", listOf(LocalizedString.WithResourceAndArguments(R.string.general_locked))))
  }

  @Test
  fun `should use no value label for other modes`() {
    assertThat(program(SuplaButtonMode.CMD_WEEKLY_SCHEDULE).label)
      .isEqualTo(LocalizedString.Constant(NO_VALUE_TEXT))
    assertThat(program(SuplaButtonMode.CMD_SWITCH_TO_MANUAL).label)
      .isEqualTo(LocalizedString.Constant(NO_VALUE_TEXT))
  }

  @Test
  fun `should create from weekly schedule program including temporary lock mode`() {
    // when
    val scheduleProgram = ActionTriggerScheduleProgram(
      SuplaWeeklyScheduleProgram(
        program = SuplaScheduleProgram.PROGRAM_1,
        buttonMode = SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED
      )
    )

    // then
    assertThat(scheduleProgram.program).isEqualTo(SuplaScheduleProgram.PROGRAM_1)
    assertThat(scheduleProgram.buttonMode).isEqualTo(SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED)
    assertThat(scheduleProgram.label)
      .isEqualTo(LocalizedString.WithFormat("%s ⏱️", listOf(LocalizedString.WithResourceAndArguments(R.string.general_locked))))
  }

  private fun program(mode: SuplaButtonMode) = ActionTriggerScheduleProgram(
    SuplaWeeklyScheduleProgram(
      program = SuplaScheduleProgram.PROGRAM_1,
      buttonMode = mode
    )
  )
}
