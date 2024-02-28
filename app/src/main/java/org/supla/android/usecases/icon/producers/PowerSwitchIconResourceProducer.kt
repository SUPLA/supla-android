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
        data.icon(R.drawable.tvon, R.drawable.tvon_nightmode)
      } else {
        data.icon(R.drawable.tvoff, R.drawable.tvoff_nightmode)
      }

      2 -> if (data.state.value == ChannelState.Value.ON) {
        data.icon(R.drawable.radioon, R.drawable.radioon_nightmode)
      } else {
        data.icon(R.drawable.radiooff, R.drawable.radiooff_nightmode)
      }

      3 -> if (data.state.value == ChannelState.Value.ON) {
        data.icon(R.drawable.pcon, R.drawable.pcon_nightmode)
      } else {
        data.icon(R.drawable.pcoff, R.drawable.pcoff_nightmode)
      }

      4 -> if (data.state.value == ChannelState.Value.ON) {
        data.icon(R.drawable.fanon, R.drawable.fanon_nightmode)
      } else {
        data.icon(R.drawable.fanoff, R.drawable.fanoff_nightmode)
      }

      else -> if (data.state.value == ChannelState.Value.ON) {
        data.icon(R.drawable.poweron, R.drawable.poweron_nightmode)
      } else {
        data.icon(R.drawable.poweroff, R.drawable.poweroff_nightmode)
      }
    }
}
