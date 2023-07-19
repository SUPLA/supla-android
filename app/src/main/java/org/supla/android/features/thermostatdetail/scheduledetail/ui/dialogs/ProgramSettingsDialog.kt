package org.supla.android.features.thermostatdetail.scheduledetail.ui.dialogs
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.temperature.TemperatureCorrection
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit
import org.supla.android.features.thermostatdetail.scheduledetail.data.ProgramSettingsData
import org.supla.android.features.thermostatdetail.scheduledetail.extensions.colorRes
import org.supla.android.features.thermostatdetail.scheduledetail.extensions.number
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtonsRow
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.Spinner
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.MinusIconButton
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.buttons.PlusIconButton

@Composable
fun ProgramSettingsDialog(
  data: ProgramSettingsData,
  onTemperatureClickChange: (forMode: SuplaHvacMode, correction: TemperatureCorrection) -> Unit,
  onTemperatureManualChange: (forMode: SuplaHvacMode, value: String) -> Unit,
  onDismiss: () -> Unit,
  onNegativeClick: () -> Unit,
  onPositiveClick: () -> Unit
) {
  Dialog(onDismiss = onDismiss) {
    DialogHeader(program = data.program)
    Separator(style = SeparatorStyle.LIGHT)
    if (data.modes.size > 1) {
      Spinner(
        label = stringResource(id = R.string.schedule_detail_program_dialog_program),
        options = data.spinnerModes(),
        onOptionSelected = { },
        modifier = Modifier
          .padding(
            start = dimensionResource(id = R.dimen.distance_default),
            top = dimensionResource(id = R.dimen.distance_default),
            end = dimensionResource(id = R.dimen.distance_default)
          )
          .fillMaxWidth()
      )
    }

    if (data.selectedMode == SuplaHvacMode.AUTO) {
      TemperatureControlRow(
        headerTextRes = SuplaHvacMode.HEAT.temperatureTextRes(),
        temperature = data.setpointTemperatureMinString!!,
        isError = data.temperatureMinCorrect.not(),
        plusAllowed = data.setpointTemperatureMinPlusAllowed,
        minusAllowed = data.setpointTemperatureMinMinusAllowed,
        unit = data.temperatureUnit,
        onDownClicked = { onTemperatureClickChange(SuplaHvacMode.HEAT, TemperatureCorrection.DOWN) },
        onUpClicked = { onTemperatureClickChange(SuplaHvacMode.HEAT, TemperatureCorrection.UP) },
        onValueChanged = { onTemperatureManualChange(SuplaHvacMode.HEAT, it) }
      )
      TemperatureControlRow(
        headerTextRes = SuplaHvacMode.COOL.temperatureTextRes(),
        temperature = data.setpointTemperatureMaxString!!,
        isError = data.temperatureMaxCorrect.not(),
        plusAllowed = data.setpointTemperatureMaxPlusAllowed,
        minusAllowed = data.setpointTemperatureMaxMinusAllowed,
        unit = data.temperatureUnit,
        onDownClicked = { onTemperatureClickChange(SuplaHvacMode.COOL, TemperatureCorrection.DOWN) },
        onUpClicked = { onTemperatureClickChange(SuplaHvacMode.COOL, TemperatureCorrection.UP) },
        onValueChanged = { onTemperatureManualChange(SuplaHvacMode.COOL, it) }
      )
    } else {
      val mode = data.selectedMode
      val temperature = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureMaxString!! else data.setpointTemperatureMinString!!
      val temperatureCorrect = if (mode == SuplaHvacMode.COOL) data.temperatureMaxCorrect else data.temperatureMinCorrect
      val plus = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureMaxPlusAllowed else data.setpointTemperatureMinPlusAllowed
      val minus = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureMaxMinusAllowed else data.setpointTemperatureMinMinusAllowed

      TemperatureControlRow(
        headerTextRes = data.selectedMode.temperatureTextRes(),
        temperature = temperature,
        isError = temperatureCorrect.not(),
        plusAllowed = plus,
        minusAllowed = minus,
        unit = data.temperatureUnit,
        onDownClicked = { onTemperatureClickChange(data.selectedMode, TemperatureCorrection.DOWN) },
        onUpClicked = { onTemperatureClickChange(data.selectedMode, TemperatureCorrection.UP) },
        onValueChanged = { onTemperatureManualChange(data.selectedMode, it) }
      )
    }

    Separator(style = SeparatorStyle.LIGHT, modifier = Modifier.padding(top = dimensionResource(id = R.dimen.distance_default)))
    DialogButtonsRow {
      OutlinedButton(onClick = onNegativeClick, text = stringResource(id = R.string.cancel), modifier = Modifier.weight(1f))
      Button(
        onClick = onPositiveClick,
        text = stringResource(id = R.string.save),
        enabled = when (data.selectedMode) {
          SuplaHvacMode.HEAT -> data.temperatureMinCorrect
          SuplaHvacMode.COOL -> data.temperatureMaxCorrect
          SuplaHvacMode.AUTO -> data.temperatureMinCorrect && data.temperatureMaxCorrect
          else -> false
        },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun DialogHeader(program: SuplaScheduleProgram) =
  Row(
    modifier = Modifier
      .padding(all = dimensionResource(id = R.dimen.distance_default))
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    ProgramIcon(program)
    Text(
      text = stringResource(id = R.string.schedule_detail_program_dialog_header, program.number()),
      style = MaterialTheme.typography.h6,
      textAlign = TextAlign.Center
    )
  }

@Composable
private fun ProgramIcon(program: SuplaScheduleProgram) =
  Box(
    modifier = Modifier
      .size(16.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(color = colorResource(id = program.colorRes()))
  )

@Composable
private fun TemperatureControlRow(
  @StringRes headerTextRes: Int,
  temperature: String,
  isError: Boolean,
  plusAllowed: Boolean,
  minusAllowed: Boolean,
  unit: TemperatureUnit,
  onDownClicked: () -> Unit,
  onUpClicked: () -> Unit,
  onValueChanged: (String) -> Unit
) {
  Text(
    text = stringResource(id = headerTextRes),
    modifier = Modifier.padding(
      start = dimensionResource(id = R.dimen.distance_default),
      top = dimensionResource(id = R.dimen.distance_default),
      end = dimensionResource(id = R.dimen.distance_default)
    ),
    style = MaterialTheme.typography.h6
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
    MinusIconButton(disabled = minusAllowed.not(), onClick = onDownClicked)
    TextField(
      value = temperature,
      modifier = Modifier.width(120.dp),
      keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
      isError = isError,
      singleLine = true,
      onValueChange = onValueChanged,
      trailingIcon = { Text(text = if (unit == TemperatureUnit.CELSIUS) "°C" else "°F") }
    )
    PlusIconButton(disabled = plusAllowed.not(), onClick = onUpClicked)
  }
}

@StringRes
private fun SuplaHvacMode.temperatureTextRes(): Int = when (this) {
  SuplaHvacMode.HEAT -> R.string.hvac_mode_temperature_heating
  SuplaHvacMode.COOL -> R.string.hvac_mode_temperature_cooling
  else -> R.string.hvac_mode_no_caption
}

@Preview
@Composable
private fun PreviewAuto() {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    SuplaTheme {
      ProgramSettingsDialog(ProgramSettingsData.auto(), { _, _ -> }, { _, _ -> }, {}, {}, {})
    }
  }
}

@Preview
@Composable
private fun PreviewHeat() {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    SuplaTheme {
      ProgramSettingsDialog(ProgramSettingsData.heat(), { _, _ -> }, { _, _ -> }, {}, {}, {})
    }
  }
}

private fun ProgramSettingsData.Companion.auto() = ProgramSettingsData(
  program = SuplaScheduleProgram.PROGRAM_1,
  modes = listOf(SuplaHvacMode.AUTO, SuplaHvacMode.HEAT, SuplaHvacMode.COOL),
  selectedMode = SuplaHvacMode.AUTO,
  setpointTemperatureMax = 21.0f,
  setpointTemperatureMin = 22.0f,
  setpointTemperatureMaxString = "21.0",
  setpointTemperatureMinString = "22.0",
  temperatureUnit = TemperatureUnit.CELSIUS
)

private fun ProgramSettingsData.Companion.heat() = ProgramSettingsData(
  program = SuplaScheduleProgram.PROGRAM_1,
  modes = listOf(SuplaHvacMode.HEAT),
  selectedMode = SuplaHvacMode.HEAT,
  setpointTemperatureMax = null,
  setpointTemperatureMin = 22.0f,
  setpointTemperatureMaxString = null,
  setpointTemperatureMinString = "22.0",
  temperatureUnit = TemperatureUnit.FAHRENHEIT
)
