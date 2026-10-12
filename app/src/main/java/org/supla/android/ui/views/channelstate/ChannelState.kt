package org.supla.android.ui.views.channelstate
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

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.supla.android.core.ui.theme.Distance

@Composable
fun ChannelState(
  deviceStateData: ChannelCurrentData?,
  programInfo: List<ChannelProgramData>,
  modifier: Modifier = Modifier
) {
  if (programInfo.isNotEmpty()) {
    ChannelProgramState(infos = programInfo, modifier = modifier)
  } else {
    deviceStateData?.let {
      ChannelCurrentState(data = it, modifier = modifier.padding(vertical = Distance.vertical))
    }
  }
}
