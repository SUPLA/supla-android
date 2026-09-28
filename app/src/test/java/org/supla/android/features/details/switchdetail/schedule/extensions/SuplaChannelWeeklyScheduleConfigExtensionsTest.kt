package org.supla.android.features.details.switchdetail.schedule.extensions
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
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.features.details.switchdetail.schedule.data.SwitchScheduleProgram

class SuplaChannelWeeklyScheduleConfigExtensionsTest {

  @Test
  fun `should sort configured programs and append default program`() {
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 1,
      func = null,
      crc32 = 0,
      programConfigurations = listOf(
        program(SuplaScheduleProgram.PROGRAM_3),
        program(SuplaScheduleProgram.PROGRAM_1),
        program(SuplaScheduleProgram.PROGRAM_4),
        program(SuplaScheduleProgram.PROGRAM_2)
      ),
      schedule = emptyList()
    )

    val result = config.viewProgramsList()

    assertThat(result.map { it.program }).containsExactly(
      SuplaScheduleProgram.PROGRAM_1,
      SuplaScheduleProgram.PROGRAM_2,
      SuplaScheduleProgram.PROGRAM_3,
      SuplaScheduleProgram.PROGRAM_4,
      SuplaScheduleProgram.OFF
    )
    assertThat(result.last()).isEqualTo(SwitchScheduleProgram.DEFAULT)
  }

  private fun program(program: SuplaScheduleProgram) = SuplaWeeklyScheduleProgram(
    program = program,
    relayMode = SuplaRelayMode.NOT_SET,
    relayModeDurationS = 0,
    relayOppositeModeDurationS = 0
  )
}
