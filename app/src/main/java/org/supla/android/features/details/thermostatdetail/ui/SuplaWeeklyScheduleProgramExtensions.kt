package org.supla.android.features.details.thermostatdetail.ui
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
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.core.shared.extensions.fromSuplaTemperature
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.ValueFormat

fun SuplaWeeklyScheduleProgram.description(thermometerValuesFormatter: ValueFormatter): LocalizedString {
  val heatTemperature = setpointTemperatureHeat?.fromSuplaTemperature()?.toDouble()
  val coolTemperature = setpointTemperatureCool?.fromSuplaTemperature()?.toDouble()
  return when {
    program == SuplaScheduleProgram.OFF ->
      localizedString(R.string.turn_off)
    mode == SuplaHvacMode.HEAT ->
      LocalizedString.Constant(thermometerValuesFormatter.format(heatTemperature, ValueFormat.TemperatureWithDegree))
    mode == SuplaHvacMode.COOL ->
      LocalizedString.Constant(thermometerValuesFormatter.format(coolTemperature, ValueFormat.TemperatureWithDegree))
    mode == SuplaHvacMode.HEAT_COOL -> {
      val minTemperature = thermometerValuesFormatter.format(heatTemperature, ValueFormat.TemperatureWithDegree)
      val maxTemperature = thermometerValuesFormatter.format(coolTemperature, ValueFormat.TemperatureWithDegree)
      LocalizedString.Constant("$minTemperature - $maxTemperature")
    }
    else -> LocalizedString.Constant(NO_VALUE_TEXT)
  }
}

val SuplaWeeklyScheduleProgram.Companion.OFF: SuplaWeeklyScheduleProgram
  get() = SuplaWeeklyScheduleProgram(
    program = SuplaScheduleProgram.OFF,
    mode = SuplaHvacMode.OFF
  )
