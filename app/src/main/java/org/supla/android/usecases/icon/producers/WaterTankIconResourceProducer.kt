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
import org.supla.android.data.model.general.ChannelState
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer
import org.supla.core.shared.data.model.general.SuplaFunction

object WaterTankIconResourceProducer : IconResourceProducer {
  override fun accepts(function: SuplaFunction): Boolean =
    function == SuplaFunction.WATER_TANK

  override fun produce(data: IconData): Int =
    when (data.state.value) {
      ChannelState.Value.EMPTY -> emptyIcon(data.altIcon)
      ChannelState.Value.HALF -> halfIcon(data.altIcon)
      ChannelState.Value.FULL -> fullIcon(data.altIcon)
      else -> R.drawable.ic_unknown_channel
    }

  private fun emptyIcon(alt: Int): Int =
    when (alt) {
      1 -> R.drawable.fnc_water_tank_1_empty
      2 -> R.drawable.fnc_water_tank_2_empty
      3 -> R.drawable.fnc_water_tank_3_empty
      else -> R.drawable.fnc_water_tank_empty
    }

  private fun halfIcon(alt: Int): Int =
    when (alt) {
      1 -> R.drawable.fnc_water_tank_1_half
      2 -> R.drawable.fnc_water_tank_2_half
      3 -> R.drawable.fnc_water_tank_3_half
      else -> R.drawable.fnc_water_tank_half
    }

  private fun fullIcon(alt: Int): Int =
    when (alt) {
      1 -> R.drawable.fnc_water_tank_1_full
      2 -> R.drawable.fnc_water_tank_2_full
      3 -> R.drawable.fnc_water_tank_3_full
      else -> R.drawable.fnc_water_tank_full
    }
}
