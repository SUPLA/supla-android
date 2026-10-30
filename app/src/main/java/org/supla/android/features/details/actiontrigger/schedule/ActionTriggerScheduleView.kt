package org.supla.android.features.details.actiontrigger.schedule
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.label
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.actiontrigger.schedule.data.ActionTriggerScheduleProgram
import org.supla.android.features.details.actiontrigger.schedule.ui.dialogs.AtProgramSettingsDialog
import org.supla.android.features.details.actiontrigger.schedule.ui.dialogs.AtProgramSettingsScope
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.editor.QuartersDialog
import org.supla.android.ui.views.schedule.editor.QuartersSelectionDialogScope
import org.supla.android.ui.views.schedule.editor.ScheduleTableBox
import org.supla.android.ui.views.schedule.editor.ScheduleTableState
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditor
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorScope
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState

interface ActionTriggerScheduleScope : WeeklyScheduleEditorScope, QuartersSelectionDialogScope, AtProgramSettingsScope

@Composable
fun ActionTriggerScheduleScope.ActionTriggerScheduleView(state: ActionTriggerScheduleViewState) {
  WeeklyScheduleEditor(
    state = state.editorState,
    loading = state.loadingState.loading,
    dialogs = {
      state.quarterSelection?.let { QuartersDialog(it, state.editorState.programs) }
      state.programSettings?.let { AtProgramSettingsDialog(it) }
    }
  )
}

private val previewScope = object : ActionTriggerScheduleScope {
  override fun onScheduleProgramClick(program: SuplaScheduleProgram) {}
  override fun onScheduleProgramLongClick(program: SuplaScheduleProgram) {}
  override fun onQuartersSelectionProgramChange(program: SuplaScheduleProgram) {}
  override fun onQuartersSelectionQuarterChange(quarterOfHour: QuarterOfHour) {}
  override fun onQuartersSelectionDismiss() {}
  override fun onQuartersSelectionFinish() {}
  override fun onAtProgramSettingsModeChange(mode: SuplaButtonMode) {}
  override fun onAtProgramSettingsDurationChange(value: Int) {}
  override fun onAtProgramSettingsDismiss() {}
  override fun onAtProgramSettingsSave() {}
  override fun onScheduleTableLongPress(key: ScheduleDetailEntryBoxKey?) {}
  override fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey) {}
  override fun onScheduleTableReload() {}
  override fun onScheduleTableInvalidate() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview() {
  SuplaTheme {
    Box(modifier = Modifier.systemBarsPadding()) {
      previewScope.ActionTriggerScheduleView(
        ActionTriggerScheduleViewState(
          loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false),
          editorState = WeeklyScheduleEditorState(
            programs = listOf(
              previewProgram(SuplaScheduleProgram.PROGRAM_1, SuplaButtonMode.NOT_SET),
              previewProgram(SuplaScheduleProgram.PROGRAM_2, SuplaButtonMode.LOCKED),
              previewProgram(SuplaScheduleProgram.PROGRAM_3, SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED, 3600),
              ActionTriggerScheduleProgram.DEFAULT
            ),
            activeProgram = SuplaScheduleProgram.PROGRAM_2,
            scheduleTableState = ScheduleTableState(
              schedule = mapOf(
                ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ScheduleTableBox(SuplaScheduleProgram.PROGRAM_2),
                ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ScheduleTableBox(
                  firstQuarterProgram = SuplaScheduleProgram.PROGRAM_1,
                  secondQuarterProgram = SuplaScheduleProgram.PROGRAM_2,
                  thirdQuarterProgram = SuplaScheduleProgram.PROGRAM_3,
                  fourthQuarterProgram = SuplaScheduleProgram.PROGRAM_4
                )
              )
            )
          )
        )
      )
    }
  }
}

private fun previewProgram(
  program: SuplaScheduleProgram,
  mode: SuplaButtonMode,
  durationS: Int = 0
) = ActionTriggerScheduleProgram(
  program = program,
  buttonMode = mode,
  buttonTemporaryUnlockDurationS = durationS,
  label = mode.label
)
