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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.data.ThermostatScheduleDetailEntryBoxValue
import org.supla.android.features.details.thermostatdetail.schedule.ui.components.ScheduleInfo
import org.supla.android.features.details.thermostatdetail.schedule.ui.components.ScheduleProgramButton
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.ProgramDialog
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.ProgramSettingsScope
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.QuartersDialog
import org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs.QuartersSelectionDialogScope
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.ScheduleTable
import org.supla.android.ui.views.schedule.ScheduleTableScope
import org.supla.android.ui.views.schedule.ScheduleTableState
import org.supla.android.ui.views.tools.Shadow
import org.supla.android.ui.views.tools.ShadowOrientation
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString

interface ScheduleDetailViewScope : QuartersSelectionDialogScope, ScheduleTableScope, ProgramSettingsScope {
  fun changeProgram(program: SuplaScheduleProgram)
  fun startProgramDialog(program: SuplaScheduleProgram)
  fun onHelpClosed() {}
}

@Composable
fun ScheduleDetailViewScope.View(viewState: ScheduleDetailViewState) {
  var boxSize by remember { mutableStateOf(Size(0f, 0f)) }

  Box {
    viewState.quarterSelection?.let { QuartersDialog(it, viewState.programs) }
    viewState.programSettings?.let { ProgramDialog(data = it) }

    ScheduleDetailContainer {
      Shadow(orientation = ShadowOrientation.STARTING_TOP)
      if (LocalConfiguration.current.isPhoneLandscape) {
        ScheduleDetailLandscape(viewState) { boxSize = it }
      } else {
        ScheduleDetailPortrait(viewState, this@View) { boxSize = it }
      }
    }

    viewState.showHelp.ifTrue { ScheduleInfo(boxSize = boxSize) { onHelpClosed() } }
    viewState.loadingState.loading.ifTrue { LoadingScrim() }
  }
}

@Composable
private fun ScheduleDetailViewScope.ScheduleDetailLandscape(
  viewState: ScheduleDetailViewState,
  onBoxSizeChanged: (Size) -> Unit
) =
  Row(horizontalArrangement = Arrangement.spacedBy(Distance.tiny)) {
    ScheduleProgramsColumn {
      for (programOption in viewState.programs) {
        ScheduleProgramButton(
          programBox = programOption,
          active = programOption.program == viewState.activeProgram,
          onClick = { changeProgram(programOption.program) },
          onLongClick = { startProgramDialog(programOption.program) }
        )
      }
    }
    ScheduleTable(
      state = viewState.scheduleTableState,
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f)
        .padding(
          top = Distance.small,
          end = Distance.default,
          bottom = Distance.small
        ),
      onBoxSizeChanged = onBoxSizeChanged
    )
  }

@Composable
private fun ColumnScope.ScheduleDetailPortrait(
  viewState: ScheduleDetailViewState,
  viewScope: ScheduleDetailViewScope,
  onBoxSizeChanged: (Size) -> Unit
) {
  ScheduleProgramsRow {
    for (programOption in viewState.programs) {
      ScheduleProgramButton(
        programBox = programOption,
        active = programOption.program == viewState.activeProgram,
        onClick = { viewScope.changeProgram(programOption.program) },
        onLongClick = { viewScope.startProgramDialog(programOption.program) }
      )
    }
  }
  viewScope.ScheduleTable(
    state = viewState.scheduleTableState,
    modifier = Modifier
      .fillMaxWidth()
      .weight(1f)
      .padding(
        bottom = dimensionResource(id = R.dimen.distance_small),
        start = dimensionResource(id = R.dimen.distance_default),
        end = dimensionResource(id = R.dimen.distance_default)
      ),
    onBoxSizeChanged = onBoxSizeChanged
  )
}

@Composable
private fun ScheduleDetailContainer(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(color = MaterialTheme.colorScheme.background),
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

@Composable
private fun ScheduleProgramsColumn(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier
      .fillMaxHeight()
      .verticalScroll(rememberScrollState())
      .padding(
        start = Distance.default,
        top = Distance.small,
        bottom = Distance.small
      ),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    content = content
  )

val previewScope = object : ScheduleDetailViewScope {
  override fun changeProgram(program: SuplaScheduleProgram) {}
  override fun startProgramDialog(program: SuplaScheduleProgram) {}
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
    ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ThermostatScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_1),
    ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ThermostatScheduleDetailEntryBoxValue(
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
          programs = listOf(
            mockProgramBox(SuplaScheduleProgram.PROGRAM_1, "19.0°"),
            mockProgramBox(SuplaScheduleProgram.PROGRAM_2, "21.0°"),
            mockProgramBox(SuplaScheduleProgram.PROGRAM_3, "18.0°"),
            mockProgramBox(SuplaScheduleProgram.PROGRAM_4, "24.0°"),
            mockProgramBox(SuplaScheduleProgram.OFF, "")
          ),
          scheduleTableState = ScheduleTableState(schedule = schedule),
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
