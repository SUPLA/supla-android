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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.custom.LocationSortingType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReorderChannelsUseCase @Inject constructor(
  private val channelRepository: RoomChannelRepository,
  private val locationRepository: LocationRepository
) {

  operator fun invoke(firstItemId: Long, firstItemLocationId: Int, secondItemId: Long): Completable =
    locationRepository.findByRemoteId(firstItemLocationId)
      .toSingle()
      .flatMapCompletable { location ->
        channelRepository.findChannelsForLocation(location.caption)
          .map { channels ->
            val orderedIds = channels.map { requireNotNull(it.id) { "Channel id is null" } }.toMutableList()
            val channelsById = channels.associateBy { requireNotNull(it.id) { "Channel id is null" } }
            reorderList(orderedIds, firstItemId, secondItemId)
            orderedIds to channelsById
          }
          .flatMapCompletable { (orderedIds, channelsById) ->
            locationRepository.updateLocation(location.copy(sorting = LocationSortingType.USER_DEFINED))
              .andThen(
                Observable.fromIterable(orderedIds.withIndex())
                  .concatMapCompletable { (position, channelId) ->
                    channelRepository.update(channelsById.getValue(channelId).channelEntity.copy(position = position + 1))
                  }
              )
          }
      }

  private fun reorderList(orderedItems: MutableList<Long>, firstItemId: Long, secondItemId: Long) {
    var initialPosition = -1
    var finalPosition = -1

    for (i in orderedItems.indices) {
      val id = orderedItems[i]
      if (id == firstItemId) {
        initialPosition = i
      }
      if (id == secondItemId) {
        finalPosition = i
      }
    }

    if (initialPosition < 0 || finalPosition < 0) {
      throw IllegalArgumentException("Swap items not found")
    }

    val removedId = orderedItems.removeAt(initialPosition)
    orderedItems.add(finalPosition, removedId)
  }
}
