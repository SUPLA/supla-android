package org.supla.android.features.thermostatdetail.ui
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
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.extensions.fromSuplaTemperature
import org.supla.android.extensions.valuesFormatter

val SuplaWeeklyScheduleProgram.description: StringProvider
  get() = { context ->
    val heatTemperature = setpointTemperatureHeat?.fromSuplaTemperature()?.toDouble()
    val coolTemperature = setpointTemperatureCool?.fromSuplaTemperature()?.toDouble()
    when {
      program == SuplaScheduleProgram.OFF ->
        context.resources.getString(R.string.turn_off)
      mode == SuplaHvacMode.HEAT ->
        context.valuesFormatter.getTemperatureString(heatTemperature)
      mode == SuplaHvacMode.COOL ->
        context.valuesFormatter.getTemperatureString(coolTemperature)
      mode == SuplaHvacMode.HEAT_COOL -> {
        val minTemperature = context.valuesFormatter.getTemperatureString(heatTemperature)
        val maxTemperature = context.valuesFormatter.getTemperatureString(coolTemperature)
        "$minTemperature - $maxTemperature"
      }
      else -> ValuesFormatter.NO_VALUE_TEXT
    }
  }

val SuplaWeeklyScheduleProgram.Companion.OFF: SuplaWeeklyScheduleProgram
  get() = SuplaWeeklyScheduleProgram(
    program = SuplaScheduleProgram.OFF,
    mode = SuplaHvacMode.OFF
  )
