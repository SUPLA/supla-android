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

class GateIconResourceProducer : IconResourceProducer {
  override fun accepts(function: SuplaFunction): Boolean =
    when (function) {
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.CONTROLLING_THE_GATE -> true

      else -> false
    }

  override fun produce(data: IconData): Int =
    when (data.altIcon) {
      1 -> when (data.state.value) {
        ChannelState.Value.PARTIALLY_OPENED -> R.drawable.fnc_gate_1_closed_50_percent
        ChannelState.Value.OPEN -> R.drawable.fnc_gate_1_opened
        else -> R.drawable.fnc_gate_1_closed
      }

      2 -> if (data.state.value == ChannelState.Value.OPEN) {
        R.drawable.fnc_barier_opened
      } else {
        R.drawable.fnc_barier_closed
      }

      else -> when (data.state.value) {
        ChannelState.Value.PARTIALLY_OPENED -> R.drawable.fnc_gate_closed_50_percent
        ChannelState.Value.OPEN -> R.drawable.fnc_gate_opened
        else -> R.drawable.fnc_gate_closed
      }
    }
}
