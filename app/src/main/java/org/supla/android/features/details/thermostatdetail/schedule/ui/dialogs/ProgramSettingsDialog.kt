package org.supla.android.features.details.thermostatdetail.schedule.ui.dialogs
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.features.details.thermostatdetail.schedule.data.ProgramSettingsData
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.dialogs.DialogButtons
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.buttons.MinusIconButton
import org.supla.android.ui.views.buttons.PlusIconButton
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.schedule.ScheduleProgramDialogHeader
import org.supla.android.ui.views.spinner.Spinner
import org.supla.android.ui.views.texts.Label
import org.supla.core.shared.data.model.thermometer.TemperatureUnit

interface ProgramSettingsScope {
  fun onProgramSettingsModeChange(mode: SuplaHvacMode)
  fun onProgramSettingsTemperatureClickChange(forMode: SuplaHvacMode, correction: TemperatureCorrection)
  fun onProgramSettingsTemperatureManualChange(forMode: SuplaHvacMode, value: String)
  fun onProgramSettingsDismiss()
  fun onProgramSettingsSave()
}

@Composable
fun ProgramSettingsScope.ProgramDialog(
  data: ProgramSettingsData
) {
  Dialog(onDismiss = { onProgramSettingsDismiss() }) {
    ScheduleProgramDialogHeader(program = data.program)

    if (LocalConfiguration.current.isPhoneLandscape) {
      LandscapeViewComponents(data)
    } else {
      PortraitViewComponents(data)
    }

    DialogButtons(
      onNegativeClick = ::onProgramSettingsDismiss,
      onPositiveClick = ::onProgramSettingsSave,
      positiveTextRes = R.string.save,
      positiveEnabled =
      when (data.selectedMode) {
        SuplaHvacMode.HEAT -> data.temperatureHeatCorrect
        SuplaHvacMode.COOL -> data.temperatureCoolCorrect
        SuplaHvacMode.HEAT_COOL -> data.temperatureHeatCorrect && data.temperatureCoolCorrect
        else -> false
      }
    )
  }
}

@Composable
private fun ProgramSettingsScope.PortraitViewComponents(data: ProgramSettingsData) {
  if (data.modes.size > 1) {
    Spinner(
      label = stringResource(id = R.string.schedule_detail_program_dialog_program),
      options = data.spinnerModes(),
      selectedOption = data.selectedMode,
      onOptionSelected = { onProgramSettingsModeChange(it) },
      modifier = Modifier
        .padding(
          start = dimensionResource(id = R.dimen.distance_default),
          top = dimensionResource(id = R.dimen.distance_default),
          end = dimensionResource(id = R.dimen.distance_default)
        )
        .fillMaxWidth()
    )
  }

  if (data.selectedMode == SuplaHvacMode.HEAT_COOL) {
    TemperatureControlRow(
      headerTextRes = SuplaHvacMode.HEAT.temperatureTextRes(),
      temperature = data.setpointTemperatureHeatString!!,
      isError = data.temperatureHeatCorrect.not(),
      plusAllowed = data.setpointTemperatureHeatPlusAllowed,
      minusAllowed = data.setpointTemperatureHeatMinusAllowed,
      unit = data.temperatureUnit,
      onDownClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.HEAT, TemperatureCorrection.DOWN) },
      onUpClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.HEAT, TemperatureCorrection.UP) },
      onValueChanged = { onProgramSettingsTemperatureManualChange(SuplaHvacMode.HEAT, it) }
    )
    TemperatureControlRow(
      headerTextRes = SuplaHvacMode.COOL.temperatureTextRes(),
      temperature = data.setpointTemperatureCoolString!!,
      isError = data.temperatureCoolCorrect.not(),
      plusAllowed = data.setpointTemperatureCoolPlusAllowed,
      minusAllowed = data.setpointTemperatureCoolMinusAllowed,
      unit = data.temperatureUnit,
      onDownClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.COOL, TemperatureCorrection.DOWN) },
      onUpClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.COOL, TemperatureCorrection.UP) },
      onValueChanged = { onProgramSettingsTemperatureManualChange(SuplaHvacMode.COOL, it) }
    )
  } else {
    val mode = data.selectedMode
    val temperature = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureCoolString!! else data.setpointTemperatureHeatString!!
    val temperatureCorrect = if (mode == SuplaHvacMode.COOL) data.temperatureCoolCorrect else data.temperatureHeatCorrect
    val plus = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureCoolPlusAllowed else data.setpointTemperatureHeatPlusAllowed
    val minus = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureCoolMinusAllowed else data.setpointTemperatureHeatMinusAllowed

    TemperatureControlRow(
      headerTextRes = data.selectedMode.temperatureTextRes(),
      temperature = temperature,
      isError = temperatureCorrect.not(),
      plusAllowed = plus,
      minusAllowed = minus,
      unit = data.temperatureUnit,
      onDownClicked = { onProgramSettingsTemperatureClickChange(data.selectedMode, TemperatureCorrection.DOWN) },
      onUpClicked = { onProgramSettingsTemperatureClickChange(data.selectedMode, TemperatureCorrection.UP) },
      onValueChanged = { onProgramSettingsTemperatureManualChange(data.selectedMode, it) }
    )
  }
}

@Composable
private fun ProgramSettingsScope.LandscapeViewComponents(data: ProgramSettingsData) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    modifier = Modifier.padding(horizontal = Distance.horizontal)
  ) {
    if (data.modes.size > 1) {
      Spinner(
        label = stringResource(id = R.string.schedule_detail_program_dialog_program),
        options = data.spinnerModes(),
        selectedOption = data.selectedMode,
        onOptionSelected = { onProgramSettingsModeChange(it) },
        modifier = Modifier.weight(1f)
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      if (data.selectedMode == SuplaHvacMode.HEAT_COOL) {
        TemperatureControlRow(
          headerTextRes = SuplaHvacMode.HEAT.temperatureTextRes(),
          temperature = data.setpointTemperatureHeatString!!,
          isError = data.temperatureHeatCorrect.not(),
          plusAllowed = data.setpointTemperatureHeatPlusAllowed,
          minusAllowed = data.setpointTemperatureHeatMinusAllowed,
          unit = data.temperatureUnit,
          labelPadding = PaddingValues.Zero,
          valuePadding = PaddingValues(top = Distance.tiny),
          onDownClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.HEAT, TemperatureCorrection.DOWN) },
          onUpClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.HEAT, TemperatureCorrection.UP) },
          onValueChanged = { onProgramSettingsTemperatureManualChange(SuplaHvacMode.HEAT, it) }
        )
        TemperatureControlRow(
          headerTextRes = SuplaHvacMode.COOL.temperatureTextRes(),
          temperature = data.setpointTemperatureCoolString!!,
          isError = data.temperatureCoolCorrect.not(),
          plusAllowed = data.setpointTemperatureCoolPlusAllowed,
          minusAllowed = data.setpointTemperatureCoolMinusAllowed,
          unit = data.temperatureUnit,
          labelPadding = PaddingValues(top = Distance.small),
          valuePadding = PaddingValues(top = Distance.tiny),
          onDownClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.COOL, TemperatureCorrection.DOWN) },
          onUpClicked = { onProgramSettingsTemperatureClickChange(SuplaHvacMode.COOL, TemperatureCorrection.UP) },
          onValueChanged = { onProgramSettingsTemperatureManualChange(SuplaHvacMode.COOL, it) }
        )
      } else {
        val mode = data.selectedMode
        val temperature = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureCoolString!! else data.setpointTemperatureHeatString!!
        val temperatureCorrect = if (mode == SuplaHvacMode.COOL) data.temperatureCoolCorrect else data.temperatureHeatCorrect
        val plus = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureCoolPlusAllowed else data.setpointTemperatureHeatPlusAllowed
        val minus = if (mode == SuplaHvacMode.COOL) data.setpointTemperatureCoolMinusAllowed else data.setpointTemperatureHeatMinusAllowed

        TemperatureControlRow(
          headerTextRes = data.selectedMode.temperatureTextRes(),
          temperature = temperature,
          isError = temperatureCorrect.not(),
          plusAllowed = plus,
          minusAllowed = minus,
          unit = data.temperatureUnit,
          labelPadding = PaddingValues.Zero,
          valuePadding = PaddingValues(top = Distance.tiny),
          onDownClicked = { onProgramSettingsTemperatureClickChange(data.selectedMode, TemperatureCorrection.DOWN) },
          onUpClicked = { onProgramSettingsTemperatureClickChange(data.selectedMode, TemperatureCorrection.UP) },
          onValueChanged = { onProgramSettingsTemperatureManualChange(data.selectedMode, it) }
        )
      }
    }
  }
}

@Composable
private fun TemperatureControlRow(
  @StringRes headerTextRes: Int,
  temperature: String,
  isError: Boolean,
  plusAllowed: Boolean,
  minusAllowed: Boolean,
  unit: TemperatureUnit,
  labelPadding: PaddingValues = PaddingValues(start = Distance.default, top = Distance.default, end = Distance.default),
  valuePadding: PaddingValues = PaddingValues(start = Distance.default, top = Distance.tiny, end = Distance.default),
  onDownClicked: () -> Unit,
  onUpClicked: () -> Unit,
  onValueChanged: (String) -> Unit
) {
  Label(
    text = stringResource(id = headerTextRes),
    modifier = Modifier.padding(labelPadding),
  )
  Row(
    modifier = Modifier.padding(valuePadding),
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

private val previewScope = object : ProgramSettingsScope {
  override fun onProgramSettingsModeChange(mode: SuplaHvacMode) {}
  override fun onProgramSettingsTemperatureClickChange(forMode: SuplaHvacMode, correction: TemperatureCorrection) {}
  override fun onProgramSettingsTemperatureManualChange(forMode: SuplaHvacMode, value: String) {}
  override fun onProgramSettingsDismiss() {}
  override fun onProgramSettingsSave() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun PreviewAuto() {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    SuplaTheme {
      previewScope.ProgramDialog(ProgramSettingsData.auto())
    }
  }
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun PreviewHeat() {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    SuplaTheme {
      previewScope.ProgramDialog(ProgramSettingsData.heat())
    }
  }
}

private fun ProgramSettingsData.Companion.auto() = ProgramSettingsData(
  program = SuplaScheduleProgram.PROGRAM_1,
  modes = listOf(SuplaHvacMode.HEAT_COOL, SuplaHvacMode.HEAT, SuplaHvacMode.COOL),
  selectedMode = SuplaHvacMode.HEAT_COOL,
  setpointTemperatureCool = 21.0f,
  setpointTemperatureHeat = 22.0f,
  setpointTemperatureCoolString = "21.0",
  setpointTemperatureHeatString = "22.0",
  temperatureUnit = TemperatureUnit.CELSIUS
)

private fun ProgramSettingsData.Companion.heat() = ProgramSettingsData(
  program = SuplaScheduleProgram.PROGRAM_1,
  modes = listOf(SuplaHvacMode.HEAT),
  selectedMode = SuplaHvacMode.HEAT,
  setpointTemperatureCool = null,
  setpointTemperatureHeat = 22.0f,
  setpointTemperatureCoolString = null,
  setpointTemperatureHeatString = "22.0",
  temperatureUnit = TemperatureUnit.FAHRENHEIT
)
