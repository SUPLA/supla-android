package org.supla.android.features.thermostatdetail.scheduledetail.data
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit

data class ProgramSettingsData(
  val program: SuplaScheduleProgram,
  val modes: List<SuplaHvacMode>,
  val selectedMode: SuplaHvacMode,
  val setpointTemperatureHeat: Float?,
  val setpointTemperatureCool: Float?,
  val setpointTemperatureHeatString: String?,
  val setpointTemperatureCoolString: String?,
  val temperatureUnit: TemperatureUnit,
  val setpointTemperatureHeatMinusAllowed: Boolean = true,
  val setpointTemperatureHeatPlusAllowed: Boolean = true,
  val setpointTemperatureCoolMinusAllowed: Boolean = true,
  val setpointTemperatureCoolPlusAllowed: Boolean = true,
  val temperatureHeatCorrect: Boolean = true,
  val temperatureCoolCorrect: Boolean = true
) {
  @Composable
  fun spinnerModes(): Map<SuplaHvacMode, String> {
    val returns = mutableMapOf<SuplaHvacMode, String>()
    modes.forEach { mode ->
      returns[mode] = stringResource(id = mode.captionRes())
    }
    return returns
  }

  fun cleanSetpointMin(): ProgramSettingsData = copy(setpointTemperatureHeat = 0f, setpointTemperatureHeatString = "")

  fun cleanSetpointMax(): ProgramSettingsData = copy(setpointTemperatureCool = 0f, setpointTemperatureCoolString = "")

  companion object
}

@StringRes
private fun SuplaHvacMode.captionRes(): Int = when (this) {
  SuplaHvacMode.HEAT -> R.string.hvac_mode_heating
  SuplaHvacMode.COOL -> R.string.hvac_mode_cooling
  SuplaHvacMode.HEAT_COOL -> R.string.hvac_mode_auto
  else -> R.string.hvac_mode_no_caption
}
