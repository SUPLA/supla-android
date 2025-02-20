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

import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelToRootRelationHolderUseCase @Inject constructor(
  private val channelRepository: RoomChannelRepository,
  private val channelRelationRepository: ChannelRelationRepository
) {

  private val channelToRootMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()

  fun getParents(channelId: Int): List<Int>? = channelToRootMap[channelId]

  fun reloadRelations() {
    val channels = channelRepository.findList().blockingGet()
    val parentsMap = channelRelationRepository.findChildrenToParentsRelations().blockingFirst()

    val channelsMap = channels.associateBy { it.remoteId }
    channels.forEach { channel ->
      val childrenIds = getChildrenIds(channel.remoteId, parentsMap, channelsMap, mutableListOf())
      childrenIds.forEach { childId ->
        if (channelToRootMap[childId] == null) {
          channelToRootMap[childId] = mutableListOf(channel.remoteId)
        } else {
          channelToRootMap[childId]?.add(channel.remoteId)
        }
      }
    }
  }

  private fun getChildrenIds(
    channelId: Int,
    parentsMap: Map<Int, List<ChannelRelationEntity>>,
    channelsMap: Map<Int, ChannelDataEntity>,
    childrenIds: MutableList<Int>
  ): List<Int> {
    val result = mutableListOf<Int>()

    parentsMap[channelId]?.forEach { relation ->
      channelsMap[relation.channelId]?.let { child ->
        if (!childrenIds.contains(child.remoteId)) {
          result.add(child.remoteId)
          result.addAll(getChildrenIds(child.remoteId, parentsMap, channelsMap, result))
        }
      }
    }

    return result
  }
}
