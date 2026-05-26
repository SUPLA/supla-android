package org.supla.android.usecases.group
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
import org.supla.android.data.source.ChannelGroupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReorderGroupsUseCase @Inject constructor(
  private val channelGroupRepository: ChannelGroupRepository
) {

  operator fun invoke(firstItemId: Long, firstItemLocationId: Int, secondItemId: Long): Completable =
    channelGroupRepository.findList()
      .map { groups ->
        val groupsInLocation = groups.filter { it.locationEntity.remoteId == firstItemLocationId }
        val orderedIds = groupsInLocation.map { requireNotNull(it.id) { "Group id is null" } }.toMutableList()
        val groupsById = groupsInLocation.associateBy { requireNotNull(it.id) { "Group id is null" } }

        reorderList(orderedIds, firstItemId, secondItemId)

        orderedIds.mapIndexed { position, groupId ->
          groupsById.getValue(groupId).channelGroupEntity.copy(position = position + 1)
        }
      }
      .flatMapCompletable { channelGroupRepository.update(it) }

  private fun reorderList(orderedItems: MutableList<Long>, firstItemId: Long, secondItemId: Long) {
    var initialPosition = -1
    var finalPosition = -1

    for (index in orderedItems.indices) {
      val id = orderedItems[index]
      if (id == firstItemId) {
        initialPosition = index
      }
      if (id == secondItemId) {
        finalPosition = index
      }
    }

    if (initialPosition < 0 || finalPosition < 0) {
      throw IllegalArgumentException("Swap items not found")
    }

    val removedId = orderedItems.removeAt(initialPosition)
    orderedItems.add(finalPosition, removedId)
  }
}
