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
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer

class ThermometerIconResourceProducer : IconResourceProducer {
  override fun accepts(function: Int): Boolean =
    function == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER

  override fun produce(data: IconData): Int =
    when (data.altIcon) {
      1 -> data.icon(R.drawable.fnc_thermometer_tap, R.drawable.fnc_thermometer_tap_nm)
      2 -> data.icon(R.drawable.fnc_thermometer_floor, R.drawable.fnc_thermometer_floor_nm)
      3 -> data.icon(R.drawable.fnc_thermometer_water, R.drawable.fnc_thermometer_water_nm)
      4 -> data.icon(R.drawable.fnc_thermometer_heating, R.drawable.fnc_thermometer_heating_nm)
      5 -> data.icon(R.drawable.fnc_thermometer_cooling, R.drawable.fnc_thermometer_cooling_nm)
      6 -> data.icon(R.drawable.fnc_thermometer_heater, R.drawable.fnc_thermometer_heater_nm)
      else -> data.icon(R.drawable.thermometer, R.drawable.thermometer_nightmode)
    }
}
