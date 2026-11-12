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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import org.supla.android.extensions.ucFirst
import org.supla.android.features.details.actiontrigger.schedule.data.AtProgramDurationOption
import org.supla.android.features.details.actiontrigger.schedule.data.AtProgramSettingsDialogState
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtons
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.BoxSize
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.forms.TimePicker
import org.supla.android.ui.views.schedule.ScheduleProgramDialogHeader
import org.supla.android.ui.views.spinner.Spinner
import org.supla.android.ui.views.texts.Label

interface AtProgramSettingsScope {
  fun onAtProgramSettingsModeChange(mode: SuplaButtonMode)
  fun onAtProgramSettingsDurationChange(value: Int)
  fun onAtProgramSettingsDefaultDurationChange(option: AtProgramDurationOption)
  fun onAtProgramSettingsDismiss()
  fun onAtProgramSettingsSave()
}

@Composable
fun AtProgramSettingsScope.AtProgramSettingsDialog(state: AtProgramSettingsDialogState) {
  val modeLabels = linkedMapOf(
    SuplaButtonMode.NOT_SET to stringResource(R.string.general_unlocked).ucFirst(),
    SuplaButtonMode.LOCKED to stringResource(R.string.general_locked).ucFirst(),
    SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED to stringResource(R.string.at_locked_temporary_unlock_allowed).ucFirst()
  )

  Dialog(onDismiss = ::onAtProgramSettingsDismiss) {
    ScheduleProgramDialogHeader(program = state.program)
    if (LocalConfiguration.current.isPhoneLandscape) {
      LandscapeSettings(state, modeLabels)
      Buttons(state)
    } else {
      PortraitSettings(state, modeLabels)
      Buttons(state)
    }
  }
}

@Composable
private fun AtProgramSettingsScope.PortraitSettings(
  state: AtProgramSettingsDialogState,
  modeLabels: Map<SuplaButtonMode, String>
) {
  ProgramModeSpinner(
    state,
    modeLabels,
    Modifier
      .fillMaxWidth()
      .padding(horizontal = Distance.default)
  )
  if (state.selectedMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
    Label(
      text = stringResource(R.string.action_trigger_schedule_temporary_unlock_duration),
      modifier = Modifier
        .padding(horizontal = Distance.default + 12.dp)
        .padding(top = Distance.default, bottom = Distance.tiny)
    )
    DurationSegments(
      state = state,
      modifier = Modifier
        .padding(horizontal = Distance.default)
    )
    TemporaryUnlockDurationPicker(
      state = state,
      pickerPadding = PaddingValues(horizontal = Distance.horizontal, vertical = Distance.tiny)
    )
  }
}

@Composable
private fun AtProgramSettingsScope.LandscapeSettings(
  state: AtProgramSettingsDialogState,
  modeLabels: Map<SuplaButtonMode, String>
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    verticalAlignment = Alignment.Top,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = Distance.default)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      ProgramModeSpinner(state, modeLabels)
      Label(
        text = stringResource(R.string.action_trigger_schedule_temporary_unlock_duration),
        modifier = Modifier
          .padding(horizontal = 12.dp)
          .padding(top = Distance.small)
          .padding(bottom = Distance.tiny)
      )
      DurationSegments(state = state)
    }
    if (state.selectedMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED) {
      Column(modifier = Modifier.weight(1f)) {
        TemporaryUnlockDurationPicker(state = state)
      }
    }
  }
}

@Composable
private fun AtProgramSettingsScope.ProgramModeSpinner(
  state: AtProgramSettingsDialogState,
  modeLabels: Map<SuplaButtonMode, String>,
  modifier: Modifier = Modifier
) {
  Spinner(
    label = stringResource(R.string.action_trigger_schedule_button_mode),
    options = modeLabels,
    selectedOption = state.selectedMode,
    onOptionSelected = ::onAtProgramSettingsModeChange,
    modifier = modifier
  )
}

@Composable
private fun AtProgramSettingsScope.DurationSegments(
  state: AtProgramSettingsDialogState,
  modifier: Modifier = Modifier
) {
  SegmentedComponent(
    items = state.durationOptions,
    activeItem = state.activeDuration,
    boxSize = BoxSize.Identical,
    modifier = modifier,
    onClick = { onAtProgramSettingsDefaultDurationChange(it) }
  )
}

@Composable
private fun AtProgramSettingsScope.TemporaryUnlockDurationPicker(
  state: AtProgramSettingsDialogState,
  pickerPadding: PaddingValues = PaddingValues.Zero
) {
  TimePicker(
    timeInSeconds = state.currentTemporaryUnlockDurationS?.coerceIn(0, 64_800) ?: 0,
    maxTimeInSeconds = 64_800,
    enabled = state.timePickerEnabled,
    onTimeChanged = { onAtProgramSettingsDurationChange(it.coerceIn(0, 64_800)) },
    modifier = Modifier.fillMaxWidth().padding(pickerPadding)
  )
}

@Composable
private fun AtProgramSettingsScope.Buttons(state: AtProgramSettingsDialogState) {
  DialogButtons(
    onNegativeClick = ::onAtProgramSettingsDismiss,
    onPositiveClick = ::onAtProgramSettingsSave,
    positiveEnabled = state.saveEnabled,
    padding = PaddingValues(start = Distance.default, top = Distance.tiny, end = Distance.default, bottom = Distance.default),
    negativeTextRes = R.string.cancel,
    positiveTextRes = R.string.save
  )
}

private val previewScope = object : AtProgramSettingsScope {
  override fun onAtProgramSettingsModeChange(mode: SuplaButtonMode) {}
  override fun onAtProgramSettingsDurationChange(value: Int) {}
  override fun onAtProgramSettingsDefaultDurationChange(option: AtProgramDurationOption) {}
  override fun onAtProgramSettingsDismiss() {}
  override fun onAtProgramSettingsSave() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.AtProgramSettingsDialog(
      state = AtProgramSettingsDialogState(
        program = SuplaScheduleProgram.PROGRAM_1,
        selectedMode = SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED,
        currentTemporaryUnlockDurationS = 3600,
        durationOptions = AtProgramDurationOption.entries,
        activeDuration = AtProgramDurationOption.CUSTOM
      )
    )
  }
}
