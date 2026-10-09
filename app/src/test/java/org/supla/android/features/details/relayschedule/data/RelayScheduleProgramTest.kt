package org.supla.android.features.details.relayschedule.data
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
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT

class RelayScheduleProgramTest {

  @Test
  fun `should create labels for relay modes`() {
    assertThat(program(SuplaRelayMode.NOT_SET).label)
      .isEqualTo(localizedString(R.string.schedule_program_default))
    assertThat(program(SuplaRelayMode.START_ON).label)
      .isEqualTo(localizedString(R.string.turn_on))
    assertThat(program(SuplaRelayMode.START_OFF, durationS = 200).label)
      .isEqualTo(localizedString(R.string.schedule_program_turn_off_seconds, 200))
    assertThat(program(SuplaRelayMode.START_ON, durationS = 20, oppositeDurationS = 10).label)
      .isEqualTo(localizedString(R.string.schedule_program_cycle))
    assertThat(program(SuplaRelayMode.FORCED_ON).label)
      .isEqualTo(localizedString(R.string.schedule_program_force_on))
    assertThat(program(SuplaRelayMode.FORCED_OFF).label)
      .isEqualTo(localizedString(R.string.schedule_program_force_off))
    assertThat(program(SuplaRelayMode.AUTOMATIC).label)
      .isEqualTo(localizedString(R.string.auto))
    assertThat(program(SuplaRelayMode.CMD_WEEKLY_SCHEDULE).label)
      .isEqualTo(LocalizedString.Constant(NO_VALUE_TEXT))
    assertThat(program(SuplaRelayMode.CMD_SWITCH_TO_MANUAL).label)
      .isEqualTo(LocalizedString.Constant(NO_VALUE_TEXT))
  }

  @Test
  fun `should create valid default program from off`() {
    val program = RelayScheduleProgram.DEFAULT

    assertThat(program.program).isEqualTo(SuplaScheduleProgram.OFF)
    assertThat(program.relayMode).isEqualTo(SuplaRelayMode.NOT_SET)
    assertThat(program.label).isEqualTo(localizedString(R.string.schedule_program_default))
    assertThat(program.iconRes).isNull()
    assertThat(program.isValid).isTrue()
  }

  @Test
  fun `should mark not set and command modes as invalid`() {
    assertThat(program(SuplaRelayMode.NOT_SET).isValid).isFalse()
    assertThat(program(SuplaRelayMode.CMD_WEEKLY_SCHEDULE).isValid).isFalse()
    assertThat(program(SuplaRelayMode.CMD_SWITCH_TO_MANUAL).isValid).isFalse()
    assertThat(program(SuplaRelayMode.START_ON).isValid).isTrue()
  }

  @Test
  fun `should preserve relay configuration`() {
    val program = program(SuplaRelayMode.START_ON, durationS = 200, oppositeDurationS = 30)

    assertThat(program.relayMode).isEqualTo(SuplaRelayMode.START_ON)
    assertThat(program.relayModeDurationS).isEqualTo(200)
    assertThat(program.relayOppositeModeDurationS).isEqualTo(30)
  }

  private fun program(
    mode: SuplaRelayMode,
    durationS: Int = 0,
    oppositeDurationS: Int = 0
  ): RelayScheduleProgram = RelayScheduleProgram(
    SuplaWeeklyScheduleProgram(
      program = SuplaScheduleProgram.PROGRAM_1,
      relayMode = mode,
      relayModeDurationS = durationS,
      relayOppositeModeDurationS = oppositeDurationS
    )
  )
}
