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

import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import timber.log.Timber
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelChildrenTreeUseCase @Inject constructor() {
  operator fun invoke(
    forChannelId: Int,
    relationMap: Map<Int, List<ChannelRelationEntity>>,
    channelsMap: MutableMap<Int, ChannelDataEntity>,
    childrenList: LinkedList<Int>
  ): List<ChannelChildEntity> {
    childrenList.add(forChannelId)

    val result = relationMap[forChannelId]?.mapNotNull {
      channelsMap[it.channelId]?.let { child ->
        if (childrenList.contains(child.remoteId)) {
          Timber.w("Cycle dependency found! Skipping child ${child.remoteId}")
          null
        } else {
          ChannelChildEntity(
            it,
            child,
            invoke(it.channelId, relationMap, channelsMap, childrenList)
          )
        }
      }
    } ?: emptyList()

    childrenList.removeLast()

    return result
  }
}
