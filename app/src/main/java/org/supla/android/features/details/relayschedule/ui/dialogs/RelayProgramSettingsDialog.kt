package org.supla.android.features.details.relayschedule.ui.dialogs

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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.features.details.relayschedule.data.MAX_PROGRAM_DURATION_S
import org.supla.android.features.details.relayschedule.data.RelayProgramDuration
import org.supla.android.features.details.relayschedule.data.RelayProgramSettingsData
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.MinusIconButton
import org.supla.android.ui.views.buttons.PlusIconButton
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.schedule.ScheduleProgramDialogHeader
import org.supla.android.ui.views.spinner.Spinner
import org.supla.android.ui.views.texts.Label

interface RelayProgramSettingsScope {
  fun onProgramSettingsModeChange(mode: SuplaRelayMode)
  fun onProgramSettingsDurationMinusClick(duration: RelayProgramDuration)
  fun onProgramSettingsDurationPlusClick(duration: RelayProgramDuration)
  fun onProgramSettingsDurationManualChange(duration: RelayProgramDuration, value: String)
  fun onProgramSettingsDismiss()
  fun onProgramSettingsSave()
}

@Composable
fun RelayProgramSettingsScope.RelayProgramDialog(data: RelayProgramSettingsData) {
  Dialog(onDismiss = { onProgramSettingsDismiss() }) {
    ScheduleProgramDialogHeader(program = data.program)
    Spinner(
      label = stringResource(id = R.string.relay_schedule_program_operation_type),
      options = data.spinnerModes(),
      onOptionSelected = { onProgramSettingsModeChange(it) },
      modifier = Modifier
        .padding(horizontal = dimensionResource(id = R.dimen.distance_default))
        .fillMaxWidth()
    )
    if (data.selectedMode == SuplaRelayMode.START_ON || data.selectedMode == SuplaRelayMode.START_OFF) {
      DurationControlRow(
        headerText = stringResource(
          id = R.string.relay_schedule_program_first_duration,
          data.selectedMode.relayModeStateLabel()
        ),
        duration = data.relayModeDurationSString,
        minusAllowed = data.relayModeDurationS > 0,
        plusAllowed = data.relayModeDurationS < MAX_PROGRAM_DURATION_S,
        onMinusClicked = { onProgramSettingsDurationMinusClick(RelayProgramDuration.RELAY_MODE) },
        onPlusClicked = { onProgramSettingsDurationPlusClick(RelayProgramDuration.RELAY_MODE) },
        onValueChanged = { onProgramSettingsDurationManualChange(RelayProgramDuration.RELAY_MODE, it) }
      )
      DurationControlRow(
        headerText = stringResource(
          id = R.string.relay_schedule_program_second_duration,
          data.selectedMode.oppositeModeStateLabel()
        ),
        duration = data.relayOppositeModeDurationSString,
        minusAllowed = data.relayOppositeModeDurationS > 0,
        plusAllowed = data.relayOppositeModeDurationS < MAX_PROGRAM_DURATION_S,
        onMinusClicked = { onProgramSettingsDurationMinusClick(RelayProgramDuration.OPPOSITE_MODE) },
        onPlusClicked = { onProgramSettingsDurationPlusClick(RelayProgramDuration.OPPOSITE_MODE) },
        onValueChanged = { onProgramSettingsDurationManualChange(RelayProgramDuration.OPPOSITE_MODE, it) }
      )
    }
    DialogButtonsRow {
      TextButton(
        text = stringResource(id = R.string.cancel),
        onClick = { onProgramSettingsDismiss() },
        singleLine = true,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f)
      )
      Button(
        text = stringResource(id = R.string.save),
        onClick = { onProgramSettingsSave() },
        singleLine = true,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun DurationControlRow(
  headerText: String,
  duration: String,
  minusAllowed: Boolean,
  plusAllowed: Boolean,
  onMinusClicked: () -> Unit,
  onPlusClicked: () -> Unit,
  onValueChanged: (String) -> Unit
) {
  Label(
    text = headerText,
    color = colorResource(id = R.color.on_surface_variant),
    modifier = Modifier.padding(
      start = dimensionResource(id = R.dimen.distance_default) + 12.dp,
      top = dimensionResource(id = R.dimen.distance_default),
      end = dimensionResource(id = R.dimen.distance_default) + 12.dp
    )
  )
  Row(
    modifier = Modifier.padding(
      start = dimensionResource(id = R.dimen.distance_default),
      top = dimensionResource(id = R.dimen.distance_tiny),
      end = dimensionResource(id = R.dimen.distance_default)
    ),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    MinusIconButton(disabled = minusAllowed.not(), onClick = onMinusClicked)
    TextField(
      value = duration,
      modifier = Modifier.width(120.dp),
      keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
      singleLine = true,
      onValueChange = onValueChanged,
      trailingIcon = { Text(text = "s") }
    )
    PlusIconButton(disabled = plusAllowed.not(), onClick = onPlusClicked)
  }
}

@Composable
private fun RelayProgramSettingsData.spinnerModes(): Map<SuplaRelayMode, String> =
  linkedMapOf<SuplaRelayMode, String>().apply {
    listOf(selectedMode).plus(modes).distinct().forEach { mode ->
      this[mode] = stringResource(id = mode.labelRes())
    }
  }

@StringRes
private fun SuplaRelayMode.labelRes(): Int = when (this) {
  SuplaRelayMode.START_ON -> R.string.relay_schedule_program_mode_start_on
  SuplaRelayMode.START_OFF -> R.string.relay_schedule_program_mode_start_off
  SuplaRelayMode.FORCED_ON -> R.string.relay_schedule_program_mode_forced_on
  SuplaRelayMode.FORCED_OFF -> R.string.relay_schedule_program_mode_forced_off
  SuplaRelayMode.AUTOMATIC -> R.string.relay_schedule_program_mode_automatic
  SuplaRelayMode.NOT_SET,
  SuplaRelayMode.CMD_WEEKLY_SCHEDULE,
  SuplaRelayMode.CMD_SWITCH_TO_MANUAL -> R.string.hvac_mode_no_caption
}

private fun SuplaRelayMode.relayModeStateLabel(): String = when (this) {
  SuplaRelayMode.START_ON -> "ON"
  SuplaRelayMode.START_OFF -> "OFF"
  else -> ""
}

private fun SuplaRelayMode.oppositeModeStateLabel(): String = when (this) {
  SuplaRelayMode.START_ON -> "OFF"
  SuplaRelayMode.START_OFF -> "ON"
  else -> ""
}

private val previewScope = object : RelayProgramSettingsScope {
  override fun onProgramSettingsModeChange(mode: SuplaRelayMode) {}
  override fun onProgramSettingsDurationMinusClick(duration: RelayProgramDuration) {}
  override fun onProgramSettingsDurationPlusClick(duration: RelayProgramDuration) {}
  override fun onProgramSettingsDurationManualChange(duration: RelayProgramDuration, value: String) {}
  override fun onProgramSettingsDismiss() {}
  override fun onProgramSettingsSave() {}
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.RelayProgramDialog(
      data = RelayProgramSettingsData(
        program = SuplaScheduleProgram.PROGRAM_1,
        modes = listOf(
          SuplaRelayMode.START_ON,
          SuplaRelayMode.START_OFF,
          SuplaRelayMode.FORCED_ON,
          SuplaRelayMode.FORCED_OFF,
          SuplaRelayMode.AUTOMATIC
        ),
        selectedMode = SuplaRelayMode.START_OFF,
        relayModeDurationS = 20,
        relayOppositeModeDurationS = 10
      )
    )
  }
}
