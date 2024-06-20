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
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer

class PowerSwitchIconResourceProducer : IconResourceProducer {
  override fun accepts(function: Int): Boolean =
    when (function) {
      SUPLA_CHANNELFNC_POWERSWITCH -> true
      else -> false
    }

  override fun produce(data: IconData): Int =
    when (data.altIcon) {
      1 -> if (data.state.value == ChannelState.Value.ON) {
        R.drawable.tvon
      } else {
        R.drawable.tvoff
      }

      2 -> if (data.state.value == ChannelState.Value.ON) {
        R.drawable.radioon
      } else {
        R.drawable.radiooff
      }

      3 -> if (data.state.value == ChannelState.Value.ON) {
        R.drawable.pcon
      } else {
        R.drawable.pcoff
      }

      4 -> if (data.state.value == ChannelState.Value.ON) {
        R.drawable.fanon
      } else {
        R.drawable.fanoff
      }

      else -> if (data.state.value == ChannelState.Value.ON) {
        R.drawable.fnc_switch_on
      } else {
        R.drawable.fnc_switch_off
      }
    }
}
