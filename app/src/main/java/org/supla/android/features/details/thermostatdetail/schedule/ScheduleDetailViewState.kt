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
import org.supla.android.core.networking.suplaclient.DelayableState
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.calendar.QuarterOfHour
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleEntry
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.toSuplaTemperature
import org.supla.android.features.details.thermostatdetail.schedule.data.ProgramSettingsData
import org.supla.android.features.details.thermostatdetail.schedule.data.QuartersSelectionData
import org.supla.android.features.details.thermostatdetail.schedule.data.ScheduleDetailProgramBox
import org.supla.android.features.details.thermostatdetail.schedule.data.ThermostatScheduleDetailEntryBoxValue
import org.supla.android.lib.SuplaConst
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.ScheduleTableState
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.guardLet

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

  val activeProgram: SuplaScheduleProgram? = null,
  val programs: List<ScheduleDetailProgramBox> = emptyList(),
  val scheduleTableState: ScheduleTableState<ThermostatScheduleDetailEntryBoxValue> = ScheduleTableState(),
  val quarterSelection: QuartersSelectionData? = null,
  val programSettings: ProgramSettingsData? = null,
  val showHelp: Boolean = false,
  override val sent: Boolean = false
) : ViewState(), DelayableState {

  fun quarterSelectionData(forKey: ScheduleDetailEntryBoxKey?): QuartersSelectionData? {
    val (key) = guardLet(forKey) { return null }
    val (value) = guardLet(scheduleTableState.schedule[forKey]) { return null }

    return QuartersSelectionData(
      entryKey = key.copy(),
      entryValue = value.copy(),
      activeProgram = activeProgram
    )
  }

  fun updatedPrograms(function: Int): List<ScheduleDetailProgramBox> =
    programSettings?.let { programToUpdate ->
      mutableListOf<ScheduleDetailProgramBox>().apply {
        for (program in programs) {
          if (program.scheduleProgram.program == programToUpdate.program) {
            val icon = when (function) {
              SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL if program.scheduleProgram.mode == SuplaHvacMode.HEAT ->
                R.drawable.ic_heat
              SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_HEAT_COOL if program.scheduleProgram.mode == SuplaHvacMode.COOL ->
                R.drawable.ic_cool
              else -> null
            }

            ScheduleDetailProgramBox(
              channelFunction = function,
              thermostatFunction = thermostatFunction!!,
              SuplaWeeklyScheduleProgram(
                program = programToUpdate.program,
                mode = programToUpdate.selectedMode,
                setpointTemperatureHeat = programToUpdate.setpointTemperatureHeat?.toSuplaTemperature(),
                setpointTemperatureCool = programToUpdate.setpointTemperatureCool?.toSuplaTemperature()
              ),
              iconRes = icon
            ).also {
              add(it)
            }
          } else {
            add(program)
          }
        }
      }
    } ?: programs

  fun suplaPrograms(): List<SuplaWeeklyScheduleProgram> = mutableListOf<SuplaWeeklyScheduleProgram>().apply {
    for (program in programs) {
      if (program.scheduleProgram.program == SuplaScheduleProgram.OFF) {
        continue
      }
      add(program.scheduleProgram.copy())
    }
  }

  fun suplaSchedule(): List<SuplaWeeklyScheduleEntry> = mutableListOf<SuplaWeeklyScheduleEntry>().apply {
    for (entry in scheduleTableState.schedule) {
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.FIRST, entry.value.firstQuarterProgram))
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.SECOND, entry.value.secondQuarterProgram))
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.THIRD, entry.value.thirdQuarterProgram))
      add(SuplaWeeklyScheduleEntry(entry.key.dayOfWeek, entry.key.hour.toInt(), QuarterOfHour.FOURTH, entry.value.fourthQuarterProgram))
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

  override fun sentState(): DelayableState = copy(sent = true)
  override fun delayableCopy(): DelayableState = copy()
}
