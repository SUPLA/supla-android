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

object DimmerCctAndRgbIconResourceProducer : IconResourceProducer {
  override fun accepts(function: SuplaFunction): Boolean =
    function == SuplaFunction.DIMMER_CCT_AND_RGB

  override fun produce(data: IconData): Int? {
    return when (data.state) {
      is ChannelState.RgbAndDimmer -> {
        if (data.state.dimmer == ChannelState.Value.ON && data.state.rgb == ChannelState.Value.ON) {
          R.drawable.fnc_dimmer_cct_rgb_on_on
        } else if (data.state.dimmer == ChannelState.Value.OFF && data.state.rgb == ChannelState.Value.ON) {
          R.drawable.fnc_dimmer_cct_rgb_off_on
        } else if (data.state.dimmer == ChannelState.Value.ON && data.state.rgb == ChannelState.Value.OFF) {
          R.drawable.fnc_dimmer_cct_rgb_on_off
        } else {
          R.drawable.fnc_dimmer_cct_rgb_off_off
        }
      }

      else -> null
    }
  }
}
