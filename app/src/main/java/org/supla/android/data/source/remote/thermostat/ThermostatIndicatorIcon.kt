package org.supla.android.data.source.remote.thermostat
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

enum class ThermostatIndicatorIcon(private val index: Int) {
  COOLING(0),
  HEATING(0),
  FORCED_OFF_BY_SENSOR(1),
  STANDBY(2),
  OFF(3),
  OFFLINE(4);

  val resource: Int?
    get() = when {
      this == COOLING -> R.drawable.ic_cooling
      this == HEATING -> R.drawable.ic_heating
      this == FORCED_OFF_BY_SENSOR -> R.drawable.ic_sensor_alert
      this == STANDBY -> R.drawable.ic_standby
      else -> null
    }

  infix fun mergeWith(other: ThermostatIndicatorIcon?): ThermostatIndicatorIcon = merge(this, other)

  infix fun moreImportantThan(other: ThermostatIndicatorIcon?): Boolean =
    other?.let { index < other.index } ?: true
}

fun merge(first: ThermostatIndicatorIcon, second: ThermostatIndicatorIcon?): ThermostatIndicatorIcon =
  second?.let { if (first moreImportantThan it) first else second } ?: first
