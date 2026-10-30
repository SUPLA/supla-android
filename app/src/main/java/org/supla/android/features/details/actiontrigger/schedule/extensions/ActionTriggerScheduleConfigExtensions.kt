package org.supla.android.features.details.actiontrigger.schedule.extensions
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

import org.supla.android.data.source.remote.hvac.SuplaChannelWeeklyScheduleConfig
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.features.details.actiontrigger.schedule.ActionTriggerScheduleViewState
import org.supla.android.features.details.actiontrigger.schedule.data.ActionTriggerScheduleProgram
import org.supla.android.features.details.schedule.WeeklyScheduleConfigChange
import org.supla.android.ui.views.schedule.editor.toSuplaScheduleEntries

fun SuplaChannelWeeklyScheduleConfig.viewActionTriggerProgramsList(): List<ActionTriggerScheduleProgram> =
  programConfigurations
    .sortedBy { it.program.value }
    .map { ActionTriggerScheduleProgram(it) }
    .plus(ActionTriggerScheduleProgram.DEFAULT)

fun ActionTriggerScheduleViewState.toWeeklyScheduleConfigChange(remoteId: Int) = WeeklyScheduleConfigChange(
  remoteId = remoteId,
  programConfigurations = editorState.programs.filter { it.program != SuplaScheduleProgram.OFF }.map {
    SuplaWeeklyScheduleProgram(
      program = it.program,
      buttonMode = it.buttonMode,
      buttonTemporaryUnlockDurationS = it.buttonTemporaryUnlockDurationS ?: 0
    )
  },
  schedule = editorState.scheduleTableState.toSuplaScheduleEntries()
)
