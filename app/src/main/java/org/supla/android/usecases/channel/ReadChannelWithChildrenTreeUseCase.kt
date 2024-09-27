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

import io.reactivex.rxjava3.core.Observable
import org.supla.android.Trace
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.extensions.TAG
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadChannelWithChildrenTreeUseCase @Inject constructor(
  private val channelRelationRepository: ChannelRelationRepository,
  private val channelRepository: RoomChannelRepository
) {

  operator fun invoke(remoteId: Int): Observable<ChannelWithChildren> =
    Observable.combineLatest(
      channelRelationRepository.findChildrenToParentsRelations(),
      channelRepository.findObservableList()
    ) { relationMap, entities -> Pair(relationMap, entities) }
      .flatMap { (relationMap, entities) ->
        val channel = entities.firstOrNull { it.remoteId == remoteId }
        if (channel == null) {
          Trace.w(TAG, "Could not find channel where channels tree was requested!")
          return@flatMap Observable.empty()
        }
        val channelsMap = mutableMapOf<Int, ChannelDataEntity>().also { map -> entities.forEach { map[it.remoteId] = it } }
        val childrenList = LinkedList<Int>()

        Observable.just(
          ChannelWithChildren(
            channel = channel,
            children = findChildren(channel.remoteId, relationMap, channelsMap, childrenList)
          )
        )
      }
      .distinctUntilChanged()

  private fun findChildren(
    forChannelId: Int,
    relationMap: Map<Int, List<ChannelRelationEntity>>,
    channelsMap: MutableMap<Int, ChannelDataEntity>,
    childrenList: LinkedList<Int>
  ): List<ChannelChildEntity> {
    childrenList.add(forChannelId)

    val result = relationMap[forChannelId]?.mapNotNull {
      channelsMap[it.channelId]?.let { child ->
        if (childrenList.contains(child.remoteId)) {
          Trace.w(TAG, "Cycle dependency found! Skipping child ${child.remoteId}")
          null
        } else {
          ChannelChildEntity(
            it,
            child,
            findChildren(it.channelId, relationMap, channelsMap, childrenList)
          )
        }
      }
    } ?: emptyList()

    childrenList.removeLast()

    return result
  }
}
