package org.supla.android.usecases.icon.producers
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
import org.supla.android.data.model.general.IconType
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer
import org.supla.core.shared.data.model.general.SuplaFunction

class HumidityAndTemperatureIconResourceProducer : IconResourceProducer {
  override fun accepts(function: SuplaFunction): Boolean =
    function == SuplaFunction.HUMIDITY_AND_TEMPERATURE

  override fun produce(data: IconData): Int =
    when (data.type) {
      IconType.SECOND -> R.drawable.fnc_humidity
      else -> thermometerIcon(data)
    }

  private fun thermometerIcon(data: IconData): Int =
    when (data.altIcon) {
      1 -> R.drawable.fnc_thermometer_tap
      2 -> R.drawable.fnc_thermometer_floor
      3 -> R.drawable.fnc_thermometer_water
      4 -> R.drawable.fnc_thermometer_heating
      5 -> R.drawable.fnc_thermometer_cooling
      6 -> R.drawable.fnc_thermometer_heater
      7 -> R.drawable.fnc_thermometer_home
      else -> R.drawable.fnc_thermometer
    }
}
