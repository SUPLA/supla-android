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
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer

class GarageDoorIconResourceProducer : IconResourceProducer {
  override fun accepts(function: Int): Boolean =
    when (function) {
      SUPLA_CHANNELFNC_OPENSENSOR_GARAGEDOOR,
      SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR -> true

      else -> false
    }

  override fun produce(data: IconData): Int =
    when (data.state.value) {
      ChannelState.Value.PARTIALLY_OPENED ->
        data.icon(R.drawable.garagedoorclosed50percent, R.drawable.garagedoorclosed50percent_nightmode)

      ChannelState.Value.OPEN ->
        data.icon(R.drawable.garagedooropen, R.drawable.garagedooropen_nightmode)

      else ->
        data.icon(R.drawable.garagedoorclosed, R.drawable.garagedoorclosed_nightmode)
    }
}
