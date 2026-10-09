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

import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.features.details.programinfo.ProgramInfo
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

class RelayProgramInfoBuilder(
  private val weeklyScheduleConfig: SuplaChannelWeeklyScheduleConfig,
  private val dateProvider: DateProvider
) {
  fun build(isAutomaticTimeSyncDisabled: Boolean = false): List<ProgramInfo> {
    val schedule = weeklyScheduleConfig.schedule
    if (schedule.isEmpty()) return emptyList()

    val minute = dateProvider.currentMinute()
    val currentDay = dateProvider.currentDayOfWeek()
    val currentHour = dateProvider.currentHour()
    val currentEntryIndex = schedule.indexOfFirst { entry ->
      entry.dayOfWeek == currentDay &&
        entry.hour == currentHour &&
        entry.quarterOfHour == QuarterOfHour.from(minute)
    }
    if (currentEntryIndex < 0) return emptyList()

    val currentProgram = schedule[currentEntryIndex].program
    val currentDescription = description(currentProgram) ?: return emptyList()
    var nextProgram: SuplaScheduleProgram? = null
    var quartersBeforeNextProgram = 0

    for (offset in 1..schedule.size) {
      val program = schedule[(currentEntryIndex + offset) % schedule.size].program
      if (program != currentProgram) {
        nextProgram = program
        break
      }
      quartersBeforeNextProgram++
    }

    val nextDescription = nextProgram?.let(::description)

    val currentInfo = ProgramInfo(
      type = ProgramInfo.Type.CURRENT,
      time = nextDescription?.let {
        val minutesToNextProgram = quartersBeforeNextProgram * 15 + (15 - minute % 15)
        localizedString(R.string.program_info_time, ValuesFormatter.getHourWithMinutes(minutesToNextProgram))
      },
      description = currentDescription
    )
    if (isAutomaticTimeSyncDisabled) {
      return listOf(currentInfo.copy(time = null))
    }

    val nextInfo = nextDescription?.let { ProgramInfo(type = ProgramInfo.Type.NEXT, description = it) }

    return listOfNotNull(currentInfo, nextInfo)
  }

  private fun description(program: SuplaScheduleProgram): LocalizedString? =
    if (program == SuplaScheduleProgram.OFF) {
      RelayScheduleProgram.DEFAULT.label
    } else {
      weeklyScheduleConfig.programConfigurations
        .firstOrNull { it.program == program }
        ?.let { RelayScheduleProgram(it) }
        ?.label
    }
}
