package org.supla.android.usecases.channel
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

import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.custom.LocationSortingType
import org.supla.android.ui.lists.ListItem
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReorderChannelsUseCase @Inject constructor(
  private val channelRepository: RoomChannelRepository,
  private val locationRepository: LocationRepository
) {

  suspend operator fun invoke(items: List<ListItem>, movedItemId: Int) {
    val moved = items.filterIsInstance<ListItem.DefaultItem>().firstOrNull { it.remoteId == movedItemId } ?: return

    val locations = items.filterIsInstance<ListItem.LocationItem>().filter { it.userCaption == moved.locationCaption }
    if (locations.isEmpty()) {
      Timber.w("No location found, reorder stopped!")
      return
    }

    var useId = true
    if (locations.size > 1) {
      useId = false
    }

    val orderedChannels =
      if (useId) {
        items.filterIsInstance<ListItem.DefaultItem>().filter { it.locationId == moved.locationId }
      } else {
        items.filterIsInstance<ListItem.DefaultItem>().filter { it.locationCaption == moved.locationCaption }
      }

    val location = items.filterIsInstance<ListItem.LocationItem>().firstOrNull { it.remoteId == moved.locationId } ?: return
    locationRepository.changeSortingType(location.remoteId, LocationSortingType.USER_DEFINED)

    var position = 1
    for (channel in orderedChannels) {
      channelRepository.updatePosition(channel.remoteId, position++)
    }
  }
}
