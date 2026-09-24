package org.supla.android.features.details.thermostatdetail.schedule
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
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.ui.components.ScheduleInfo
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.ProgramDialog
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.ProgramSettingsScope
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.QuartersDialog
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.QuartersSelectionDialogScope
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.editor.ScheduleTableBox
import org.supla.android.ui.views.schedule.editor.ScheduleTableState
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditor
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorScope
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString

interface ScheduleDetailViewScope : WeeklyScheduleEditorScope, QuartersSelectionDialogScope, ProgramSettingsScope {
  fun onHelpClosed() {}
}

@Composable
fun ScheduleDetailViewScope.View(viewState: ScheduleDetailViewState) {
  WeeklyScheduleEditor(
    state = viewState.editorState,
    loading = viewState.loadingState.loading,
    dialogs = {
      viewState.quarterSelection?.let { QuartersDialog(it, viewState.editorState.programs) }
      viewState.programSettings?.let { ProgramDialog(data = it) }
    },
    overlay = { boxSize ->
      if (viewState.showHelp) {
        ScheduleInfo(boxSize = boxSize) { onHelpClosed() }
      }
    }
  )
}

private val previewScope = object : ScheduleDetailViewScope {
  override fun onScheduleProgramClick(program: SuplaScheduleProgram) {}
  override fun onScheduleProgramLongClick(program: SuplaScheduleProgram) {}
  override fun onQuartersSelectionProgramChange(program: SuplaScheduleProgram) {}
  override fun onQuartersSelectionQuarterChange(quarterOfHour: QuarterOfHour) {}
  override fun onQuartersSelectionDismiss() {}
  override fun onQuartersSelectionFinish() {}
  override fun onScheduleTableLongPress(key: ScheduleDetailEntryBoxKey?) {}
  override fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey) {}
  override fun onScheduleTableReload() {}
  override fun onScheduleTableInvalidate() {}
  override fun onProgramSettingsTemperatureClickChange(forMode: SuplaHvacMode, correction: TemperatureCorrection) {}
  override fun onProgramSettingsTemperatureManualChange(forMode: SuplaHvacMode, value: String) {}
  override fun onProgramSettingsDismiss() {}
  override fun onProgramSettingsSave() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview() {
  val schedule = mapOf(
    ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ScheduleTableBox(SuplaScheduleProgram.PROGRAM_1),
    ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ScheduleTableBox(
      SuplaScheduleProgram.PROGRAM_1,
      SuplaScheduleProgram.PROGRAM_2,
      SuplaScheduleProgram.OFF,
      SuplaScheduleProgram.PROGRAM_3
    )
  )
  SuplaTheme {
    Box(modifier = Modifier.systemBarsPadding()) {
      previewScope.View(
        viewState = ScheduleDetailViewState(
          editorState = WeeklyScheduleEditorState(
            programs = listOf(
              mockProgramBox(SuplaScheduleProgram.PROGRAM_1, "19.0°"),
              mockProgramBox(SuplaScheduleProgram.PROGRAM_2, "21.0°"),
              mockProgramBox(SuplaScheduleProgram.PROGRAM_3, "18.0°"),
              mockProgramBox(SuplaScheduleProgram.PROGRAM_4, "24.0°"),
              mockProgramBox(SuplaScheduleProgram.OFF, "")
            ),
            activeProgram = SuplaScheduleProgram.PROGRAM_1,
            scheduleTableState = ScheduleTableState(schedule = schedule)
          ),
          loadingState = LoadingTimeoutManager.LoadingState(initialLoading = false, loading = false)
        )
      )
    }
  }
}

private fun mockProgramBox(program: SuplaScheduleProgram, label: String): ScheduleDetailProgramBox =
  ScheduleDetailProgramBox(
    channelFunction = SuplaFunction.HVAC_THERMOSTAT.value,
    thermostatFunction = ThermostatSubfunction.HEAT,
    program = program,
    mode = SuplaHvacMode.HEAT,
    setpointTemperatureCool = null,
    setpointTemperatureHeat = null,
    label = LocalizedString.Constant(label)
  )
