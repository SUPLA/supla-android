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

import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.SuplaDeviceConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.isAutomaticTimeSyncDisabled
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.core.shared.extensions.forFalse

fun SuplaChannelWeeklyScheduleConfig.viewScheduleTableState(
  deviceConfig: SuplaDeviceConfig?,
  dateProvider: DateProvider
) = ScheduleTableState(
  schedule = viewScheduleBoxesMap(),
  currentDayOfWeek = deviceConfig.isAutomaticTimeSyncDisabled().forFalse(dateProvider.currentDayOfWeek()),
  currentHour = deviceConfig.isAutomaticTimeSyncDisabled().forFalse(dateProvider.currentHour())
)

fun SuplaChannelWeeklyScheduleConfig.viewScheduleBoxesMap() =
  mutableMapOf<ScheduleDetailEntryBoxKey, ScheduleTableBox>().apply {
    for (entry in schedule) {
      val key = ScheduleDetailEntryBoxKey(entry.dayOfWeek, entry.hour.toShort())
      val value = this[key] ?: ScheduleTableBox(SuplaScheduleProgram.OFF)
      this[key] = when (entry.quarterOfHour) {
        QuarterOfHour.FIRST -> value.copy(firstQuarterProgram = entry.program)
        QuarterOfHour.SECOND -> value.copy(secondQuarterProgram = entry.program)
        QuarterOfHour.THIRD -> value.copy(thirdQuarterProgram = entry.program)
        QuarterOfHour.FOURTH -> value.copy(fourthQuarterProgram = entry.program)
      }
    }
  }
