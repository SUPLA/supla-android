package org.supla.android.extensions
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
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.ValuesFormatter.Companion.NO_VALUE_TEXT
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.data.source.remote.thermostat.SuplaThermostatFlags
import org.supla.android.db.Channel
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaTimerState
import org.supla.android.ui.lists.data.SlideableListItemData

fun Channel.getTimerStateValue(): SuplaTimerState? = extendedValue?.extendedValue?.TimerStateValue

fun Channel.toSlideableListItemData(mainThermometerChild: Channel?, valuesFormatter: ValuesFormatter?): SlideableListItemData.Thermostat {
  val thermostatValue = value.asThermostatValue()

  val temperatureMin = valuesFormatter?.getTemperatureString(thermostatValue.setpointTemperatureMin)
  val temperatureMax = valuesFormatter?.getTemperatureString(thermostatValue.setpointTemperatureMax)
  val subValue = when (thermostatValue.mode) {
    SuplaHvacMode.COOL -> temperatureMax
    SuplaHvacMode.AUTO -> "$temperatureMin - $temperatureMax"
    SuplaHvacMode.HEAT -> temperatureMin
    else -> ""
  }

  val indicatorIcon = when {
    onLine && thermostatValue.flags.contains(SuplaThermostatFlags.COOLING) -> R.drawable.ic_cooling
    onLine && thermostatValue.flags.contains(SuplaThermostatFlags.HEATING) -> R.drawable.ic_heating
    onLine && thermostatValue.mode != SuplaHvacMode.OFF -> R.drawable.ic_standby
    else -> null
  }

  return SlideableListItemData.Thermostat(
    online = onLine,
    titleProvider = { getNotEmptyCaption(it) },
    iconProvider = { ImageCache.getBitmap(it, imageIdx) },
    value = mainThermometerChild?.humanReadableValue?.toString() ?: NO_VALUE_TEXT,
    subValue = subValue ?: NO_VALUE_TEXT,
    indicatorIcon = indicatorIcon
  )
}
