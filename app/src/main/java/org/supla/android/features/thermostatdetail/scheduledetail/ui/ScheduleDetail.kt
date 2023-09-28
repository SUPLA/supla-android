package org.supla.android.features.thermostatdetail.scheduledetail.ui
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

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.local.temperature.TemperatureCorrection
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.extensions.ifLet
import org.supla.android.features.thermostatdetail.scheduledetail.ScheduleDetailViewState
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxKey
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxValue
import org.supla.android.features.thermostatdetail.scheduledetail.ui.components.ScheduleProgramButton
import org.supla.android.features.thermostatdetail.scheduledetail.ui.components.ScheduleTable
import org.supla.android.features.thermostatdetail.scheduledetail.ui.dialogs.ProgramSettingsDialog
import org.supla.android.features.thermostatdetail.scheduledetail.ui.dialogs.QuartersSelectionDialog
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation

interface ScheduleDetailViewProxy : BaseViewProxy<ScheduleDetailViewState> {
  fun updateSchedule()
  fun changeScheduleEntry(key: ScheduleDetailEntryBoxKey)
  fun invalidateSchedule()
  fun changeProgram(program: SuplaScheduleProgram)
  fun startQuartersDialog(key: ScheduleDetailEntryBoxKey?)
  fun cancelQuartersDialog()
  fun onQuartersDialogProgramChange(program: SuplaScheduleProgram)
  fun onQueartersDialogQuarterChange(quarterOfHour: QuarterOfHour)
  fun saveQuartersDialogChanges()
  fun startProgramDialog(program: SuplaScheduleProgram)
  fun cancelProgramDialog()
  fun saveProgramDialogChanges()
  fun onProgramDialogTemperatureClickChange(
    programMode: SuplaHvacMode,
    modeForTemperature: SuplaHvacMode,
    correction: TemperatureCorrection
  )

  fun onProgramDialogTemperatureManualChange(programMode: SuplaHvacMode, modeForTemperature: SuplaHvacMode, value: String)
}

@Composable
fun ScheduleDetail(viewProxy: ScheduleDetailViewProxy) {
  val viewState by viewProxy.getViewState().collectAsState()

  Box {
    ifLet(viewState.quarterSelection) { (selection) ->
      QuartersSelectionDialog(
        data = selection,
        programs = viewState.programs,
        viewProxy = viewProxy,
        onDismiss = { viewProxy.cancelQuartersDialog() },
        onNegativeClick = { viewProxy.cancelQuartersDialog() },
        onPositiveClick = { viewProxy.saveQuartersDialogChanges() }
      )
    }
    ifLet(viewState.programSettings) { (settings) ->
      ProgramSettingsDialog(
        data = settings,
        onTemperatureClickChange = { mode, correction ->
          viewProxy.onProgramDialogTemperatureClickChange(
            settings.selectedMode,
            mode,
            correction
          )
        },
        onTemperatureManualChange = { mode, value -> viewProxy.onProgramDialogTemperatureManualChange(settings.selectedMode, mode, value) },
        onDismiss = { viewProxy.cancelProgramDialog() },
        onNegativeClick = { viewProxy.cancelProgramDialog() },
        onPositiveClick = { viewProxy.saveProgramDialogChanges() }
      )
    }

    ScheduleDetailContainer {
      Shadow(orientation = ShadowOrientation.STARTING_TOP)
      ScheduleProgramsRow {
        for (programOption in viewState.programs) {
          ScheduleProgramButton(
            programBox = programOption,
            active = programOption.scheduleProgram.program == viewState.activeProgram,
            onClick = { viewProxy.changeProgram(programOption.scheduleProgram.program) },
            onLongClick = { viewProxy.startProgramDialog(programOption.scheduleProgram.program) }
          )
        }
      }
      ScheduleDetailTable(viewState, viewProxy)
    }

    if (viewState.loadingState.loading) {
      LoadingScrim()
    }
  }
}

@Composable
private fun ScheduleDetailContainer(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(color = MaterialTheme.colors.background),
    content = content
  )

@Composable
private fun ScheduleProgramsRow(content: @Composable RowScope.() -> Unit) =
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(
        horizontal = dimensionResource(id = R.dimen.distance_default),
        vertical = dimensionResource(id = R.dimen.distance_small)
      )
      .height(dimensionResource(id = R.dimen.button_small_height)),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    content = content
  )

context(ColumnScope)
@Composable
private fun ScheduleDetailTable(viewState: ScheduleDetailViewState, viewProxy: ScheduleDetailViewProxy) =
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f)
      .padding(
        bottom = dimensionResource(id = R.dimen.distance_small),
        start = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      )
  ) {
    ScheduleTable(
      modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(),
      viewState = viewState,
      viewProxy = viewProxy
    )
  }

@Preview
@Composable
private fun Preview() {
  val schedule = mapOf(
    ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_1),
    ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ScheduleDetailEntryBoxValue(
      SuplaScheduleProgram.PROGRAM_1,
      SuplaScheduleProgram.PROGRAM_2,
      SuplaScheduleProgram.OFF,
      SuplaScheduleProgram.PROGRAM_3
    )
  )
  SuplaTheme {
    ScheduleDetail(PreviewProxy(programs = schedule))
  }
}

internal class PreviewProxy(private val programs: Map<ScheduleDetailEntryBoxKey, ScheduleDetailEntryBoxValue>) : ScheduleDetailViewProxy {
  override fun updateSchedule() {}
  override fun changeScheduleEntry(key: ScheduleDetailEntryBoxKey) {}
  override fun invalidateSchedule() {}
  override fun changeProgram(program: SuplaScheduleProgram) {}
  override fun startQuartersDialog(key: ScheduleDetailEntryBoxKey?) {}
  override fun cancelQuartersDialog() {}
  override fun onQuartersDialogProgramChange(program: SuplaScheduleProgram) {}
  override fun onQueartersDialogQuarterChange(quarterOfHour: QuarterOfHour) {}
  override fun saveQuartersDialogChanges() {}
  override fun startProgramDialog(program: SuplaScheduleProgram) {}
  override fun cancelProgramDialog() {}
  override fun saveProgramDialogChanges() {}
  override fun onProgramDialogTemperatureClickChange(
    programMode: SuplaHvacMode,
    modeForTemperature: SuplaHvacMode,
    correction: TemperatureCorrection
  ) {
  }

  override fun onProgramDialogTemperatureManualChange(programMode: SuplaHvacMode, modeForTemperature: SuplaHvacMode, value: String) {}

  override fun getViewState(): StateFlow<ScheduleDetailViewState> =
    MutableStateFlow(value = ScheduleDetailViewState(schedule = programs))
}
