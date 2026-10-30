package org.supla.android.features.details.detailbase.actiontrigger
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

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.ui.views.channelstate.ChannelProgramData
import org.supla.core.shared.infrastructure.LocalizedString

class ActionTriggerProgramInfoBuilderTest {

  @Test
  fun `should show current and next action trigger modes with remaining time`() {
    // given
    val dateProvider = mockk<DateProvider>()
    every { dateProvider.currentMinute() } returns 2
    every { dateProvider.currentDayOfWeek() } returns DayOfWeek.MONDAY
    every { dateProvider.currentHour() } returns 10
    val config = SuplaChannelWeeklyScheduleConfig(
      remoteId = 1,
      func = null,
      crc32 = 0,
      programConfigurations = listOf(
        SuplaWeeklyScheduleProgram(SuplaScheduleProgram.PROGRAM_1, buttonMode = SuplaButtonMode.LOCKED),
        SuplaWeeklyScheduleProgram(
          SuplaScheduleProgram.PROGRAM_2,
          buttonMode = SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED
        )
      ),
      schedule = listOf(
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 10, QuarterOfHour.FIRST, SuplaScheduleProgram.PROGRAM_1),
        SuplaWeeklyScheduleEntry(DayOfWeek.MONDAY, 10, QuarterOfHour.SECOND, SuplaScheduleProgram.PROGRAM_2)
      )
    )

    // when
    val programInfo = ActionTriggerProgramInfoBuilder(config, dateProvider).build()

    // then
    assertThat(programInfo).hasSize(2)
    assertThat(programInfo[0].type).isEqualTo(ChannelProgramData.Type.CURRENT)
    assertThat(programInfo[0].description).isEqualTo(LocalizedString.WithResourceAndArguments(R.string.general_locked))
    assertThat(programInfo[0].time).isNotNull
    assertThat(programInfo[1].type).isEqualTo(ChannelProgramData.Type.NEXT)
    assertThat(programInfo[1].description)
      .isEqualTo(LocalizedString.WithFormat("%s ⏱️", listOf(LocalizedString.WithResourceAndArguments(R.string.general_locked))))
  }
}
