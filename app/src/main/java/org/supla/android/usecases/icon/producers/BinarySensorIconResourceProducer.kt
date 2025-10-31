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
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer
import org.supla.core.shared.data.model.general.SuplaFunction

object BinarySensorIconResourceProducer : IconResourceProducer {
  override fun accepts(function: SuplaFunction): Boolean =
    function == SuplaFunction.BINARY_SENSOR

  override fun produce(data: IconData): Int =
    when (data.altIcon) {
      1 -> data.stateIcon(R.drawable.fnc_binary_sensor_1_on, R.drawable.fnc_binary_sensor_1_off)
      2 -> data.stateIcon(R.drawable.fnc_binary_sensor_2_on, R.drawable.fnc_binary_sensor_2_off)
      3 -> data.stateIcon(R.drawable.fnc_binary_sensor_3_on, R.drawable.fnc_binary_sensor_3_off)
      4 -> data.stateIcon(R.drawable.fnc_binary_sensor_4_on, R.drawable.fnc_binary_sensor_4_off)
      5 -> data.stateIcon(R.drawable.fnc_binary_sensor_5_on, R.drawable.fnc_binary_sensor_5_off)
      6 -> data.stateIcon(R.drawable.fnc_binary_sensor_6_on, R.drawable.fnc_binary_sensor_6_off)
      7 -> data.stateIcon(R.drawable.fnc_binary_sensor_7_on, R.drawable.fnc_binary_sensor_7_off)
      8 -> data.stateIcon(R.drawable.fnc_binary_sensor_8_on, R.drawable.fnc_binary_sensor_8_off)
      9 -> data.stateIcon(R.drawable.fnc_binary_sensor_9_on, R.drawable.fnc_binary_sensor_9_off)
      else -> data.stateIcon(R.drawable.fnc_binary_sensor_on, R.drawable.fnc_binary_sensor_off)
    }
}
