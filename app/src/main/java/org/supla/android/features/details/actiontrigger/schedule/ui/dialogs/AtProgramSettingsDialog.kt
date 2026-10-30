package org.supla.android.features.details.actiontrigger.schedule.ui.dialogs
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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.features.details.actiontrigger.schedule.data.AtProgramSettingsData
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsColumn
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.forms.TimePicker
import org.supla.android.ui.views.schedule.ScheduleProgramDialogHeader
import org.supla.android.ui.views.spinner.Spinner
import org.supla.android.ui.views.texts.Label

data class AtProgramSettingsViewState(
  val data: AtProgramSettingsData
)

interface AtProgramSettingsScope {
  fun onAtProgramSettingsModeChange(mode: SuplaButtonMode)
  fun onAtProgramSettingsDurationChange(value: Int)
  fun onAtProgramSettingsDismiss()
  fun onAtProgramSettingsSave()
}

@Composable
fun AtProgramSettingsScope.AtProgramSettingsDialog(state: AtProgramSettingsViewState) {
  val modeLabels = linkedMapOf(
    SuplaButtonMode.NOT_SET to stringResource(R.string.schedule_program_default),
    SuplaButtonMode.LOCKED to stringResource(R.string.general_locked),
    SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED to stringResource(R.string.at_locked_temporary_unlock_allowed)
  )

  val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
  val maxWidth = if (state.data.selectedMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) 580.dp else 320.dp
  Dialog(
    onDismiss = ::onAtProgramSettingsDismiss,
    usePlatformDefaultWidth = !landscape,
    modifier = Modifier.widthIn(max = maxWidth)
  ) {
    ScheduleProgramDialogHeader(program = state.data.program)
    if (landscape) {
      LandscapeSettings(state, modeLabels)
      DialogButtonsRow { Buttons(Modifier.weight(1f)) }
    } else {
      PortraitSettings(state, modeLabels)
      DialogButtonsColumn { Buttons(Modifier.fillMaxWidth()) }
    }
  }
}

@Composable
private fun AtProgramSettingsScope.PortraitSettings(
  state: AtProgramSettingsViewState,
  modeLabels: Map<SuplaButtonMode, String>
) {
  ProgramModeSpinner(
    state,
    modeLabels,
    Modifier
      .fillMaxWidth()
      .padding(horizontal = Distance.default)
  )
  if (state.data.selectedMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
    TemporaryUnlockDurationPicker(
      state = state,
      labelPadding = PaddingValues(start = Distance.default + 12.dp, top = Distance.default, end = Distance.default + 12.dp),
      pickerPadding = PaddingValues(horizontal = Distance.horizontal, vertical = Distance.tiny)
    )
  }
}

@Composable
private fun AtProgramSettingsScope.LandscapeSettings(
  state: AtProgramSettingsViewState,
  modeLabels: Map<SuplaButtonMode, String>
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    verticalAlignment = Alignment.Top,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = Distance.default)
  ) {
    ProgramModeSpinner(state, modeLabels, Modifier.weight(1f))
    if (state.data.selectedMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
      Column(modifier = Modifier.weight(1f)) {
        TemporaryUnlockDurationPicker(state = state)
      }
    }
  }
}

@Composable
private fun AtProgramSettingsScope.ProgramModeSpinner(
  state: AtProgramSettingsViewState,
  modeLabels: Map<SuplaButtonMode, String>,
  modifier: Modifier
) {
  Spinner(
    label = stringResource(R.string.relay_schedule_program_operation_type),
    options = modeLabels,
    selectedOption = state.data.selectedMode,
    onOptionSelected = ::onAtProgramSettingsModeChange,
    modifier = modifier
  )
}

@Composable
private fun AtProgramSettingsScope.TemporaryUnlockDurationPicker(
  state: AtProgramSettingsViewState,
  labelPadding: PaddingValues = PaddingValues.Zero,
  pickerPadding: PaddingValues = PaddingValues.Zero
) {
  Label(
    text = stringResource(R.string.action_trigger_schedule_temporary_unlock_duration),
    modifier = Modifier.padding(labelPadding)
  )
  TimePicker(
    timeInSeconds = state.data.buttonTemporaryUnlockDurationS.coerceIn(0, 64_800),
    maxTimeInSeconds = 64_800,
    onTimeChanged = { onAtProgramSettingsDurationChange(it.coerceIn(0, 64_800)) },
    modifier = Modifier.padding(pickerPadding)
  )
}

@Composable
private fun AtProgramSettingsScope.Buttons(modifier: Modifier) {
  TextButton(
    text = stringResource(R.string.cancel),
    onClick = ::onAtProgramSettingsDismiss,
    singleLine = true,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = modifier
  )
  Button(
    text = stringResource(R.string.save),
    onClick = ::onAtProgramSettingsSave,
    singleLine = true,
    modifier = modifier
  )
}

private val previewScope = object : AtProgramSettingsScope {
  override fun onAtProgramSettingsModeChange(mode: SuplaButtonMode) {}
  override fun onAtProgramSettingsDurationChange(value: Int) {}
  override fun onAtProgramSettingsDismiss() {}
  override fun onAtProgramSettingsSave() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.AtProgramSettingsDialog(
      AtProgramSettingsViewState(
        data = AtProgramSettingsData(
          program = SuplaScheduleProgram.PROGRAM_1,
          selectedMode = SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED,
          buttonTemporaryUnlockDurationS = 3600
        )
      )
    )
  }
}
