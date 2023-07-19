package org.supla.android.ui.lists
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

import org.supla.android.data.source.local.entity.Scene
import org.supla.android.db.ChannelBase
import org.supla.android.db.Location
import org.supla.android.usecases.channel.ChannelChild

sealed interface ListItem {
  data class SceneItem(val scene: Scene, val location: Location) : ListItem
  data class ChannelItem(var channelBase: ChannelBase, val location: Location, val children: List<ChannelChild>? = null) : ListItem
  data class LocationItem(val location: Location) : ListItem
}
