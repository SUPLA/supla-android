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

import org.supla.android.R
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.toSuplaTemperature
import org.supla.android.features.details.thermostatdetail.schedule.data.ProgramSettingsData
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.lib.SuplaConst
import org.supla.android.ui.views.schedule.editor.QuartersSelectionData
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleEditorState
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter

private const val DEFAULT_HEAT_TEMPERATURE = 21f
private const val DEFAULT_WATER_TEMPERATURE = 40f

data class ScheduleDetailViewState(
  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val lastInteractionTime: Long? = null,
  val changing: Boolean = false,

  val remoteId: Int = 0,
  val channelFunction: Int = 0,
  val thermostatFunction: ThermostatSubfunction? = null,
  val configTemperatureMin: Float = 0f,
  val configTemperatureMax: Float = 0f,

  val editorState: WeeklyScheduleEditorState<ScheduleDetailProgramBox> = WeeklyScheduleEditorState(),
  val quarterSelection: QuartersSelectionData? = null,
  val programSettings: ProgramSettingsData? = null,
  val showHelp: Boolean = false
) : ViewState() {

  fun updatedPrograms(function: Int, thermometerValueFormatter: ValueFormatter): List<ScheduleDetailProgramBox> =
    programSettings?.let { programToUpdate ->
      mutableListOf<ScheduleDetailProgramBox>().apply {
        for (program in editorState.programs) {
          if (program.program == programToUpdate.program) {
            val icon = when (function) {
              SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL if program.mode == SuplaHvacMode.HEAT -> R.drawable.ic_heat
              SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL if program.mode == SuplaHvacMode.COOL -> R.drawable.ic_cool
              else -> null
            }

            ScheduleDetailProgramBox(
              channelFunction = function,
              thermostatFunction = thermostatFunction!!,
              program = programToUpdate.program,
              mode = programToUpdate.selectedMode,
              setpointTemperatureHeat = programToUpdate.setpointTemperatureHeat,
              setpointTemperatureCool = programToUpdate.setpointTemperatureCool,
              valueFormatter = thermometerValueFormatter,
              iconRes = icon
            ).also {
              add(it)
            }
          } else {
            add(program)
          }
        }
      }
    } ?: editorState.programs

  fun suplaPrograms(): List<SuplaWeeklyScheduleProgram> = mutableListOf<SuplaWeeklyScheduleProgram>().apply {
    for (program in editorState.programs) {
      if (program.program == SuplaScheduleProgram.OFF) {
        continue
      }
      add(
        SuplaWeeklyScheduleProgram(
          program = program.program,
          mode = program.modeForModify,
          setpointTemperatureHeat = program.setpointTemperatureHeat?.toSuplaTemperature(),
          setpointTemperatureCool = program.setpointTemperatureCool?.toSuplaTemperature()
        )
      )
    }
  }

  fun alignTemperature(temperature: Float?): Float {
    val temperatureToAlign = temperature
      ?: if (channelFunction == SuplaFunction.HVAC_DOMESTIC_HOT_WATER.value) DEFAULT_WATER_TEMPERATURE else DEFAULT_HEAT_TEMPERATURE
    return if (temperatureToAlign < configTemperatureMin) {
      configTemperatureMin
    } else if (temperatureToAlign > configTemperatureMax) {
      configTemperatureMax
    } else {
      temperatureToAlign
    }
  }
}
