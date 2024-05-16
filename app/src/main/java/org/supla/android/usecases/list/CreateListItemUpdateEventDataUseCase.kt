package org.supla.android.usecases.list
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
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.UpdateEventsManager
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToGpmUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToMeasurementUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToProjectScreenUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToShadingSystemUpdateEventMapper
import org.supla.android.usecases.list.eventmappers.ChannelWithChildrenToThermostatUpdateEventMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateListItemUpdateEventDataUseCase @Inject constructor(
  private val eventsManager: UpdateEventsManager,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  channelWithChildrenToThermostatUpdateEventMapper: ChannelWithChildrenToThermostatUpdateEventMapper,
  channelWithChildrenToMeasurementUpdateEventMapper: ChannelWithChildrenToMeasurementUpdateEventMapper,
  channelWithChildrenToGpmUpdateEventMapper: ChannelWithChildrenToGpmUpdateEventMapper,
  channelWithChildrenToShadingSystemUpdateEventMapper: ChannelWithChildrenToShadingSystemUpdateEventMapper,
  channelWithChildrenToProjectScreenUpdateEventMapper: ChannelWithChildrenToProjectScreenUpdateEventMapper
) {

  private val mappers: List<Mapper> = listOf(
    channelWithChildrenToThermostatUpdateEventMapper,
    channelWithChildrenToMeasurementUpdateEventMapper,
    channelWithChildrenToGpmUpdateEventMapper,
    channelWithChildrenToShadingSystemUpdateEventMapper,
    channelWithChildrenToProjectScreenUpdateEventMapper
  )

  operator fun invoke(itemType: ItemType, remoteId: Int): Observable<SlideableListItemData> {
    return when (itemType) {
      ItemType.CHANNEL -> observeChannel(remoteId)
      ItemType.GROUP -> eventsManager.observeGroup(remoteId).flatMapMaybe { readChannelGroupByRemoteIdUseCase(remoteId) }
    }.map { map(it) }
  }

  private fun map(item: Any): SlideableListItemData {
    for (mapper in mappers) {
      if (mapper.handle(item)) {
        return mapper.map(item)
      }
    }

    throw IllegalStateException("No mapper found for item '$item'")
  }

  private fun observeChannel(remoteId: Int): Observable<ChannelWithChildren> {
    return readChannelWithChildrenUseCase.invoke(remoteId)
      .flatMapObservable { channelWithChildren ->
        // For channel we observe the channel itself but also all children
        val ids = mutableListOf<Int>().also { list ->
          list.add(channelWithChildren.channel.remoteId)
          channelWithChildren.children.firstOrNull { it.relationType == ChannelRelationType.MAIN_THERMOMETER }?.let {
            list.add(it.channelDataEntity.remoteId)
          }
        }

        return@flatMapObservable Observable.merge(
          ids.map { id ->
            eventsManager.observeChannel(id).flatMapMaybe { readChannelWithChildrenUseCase.invoke(remoteId) }
          }
        )
      }
  }

  interface Mapper {
    fun handle(item: Any): Boolean
    fun map(item: Any): SlideableListItemData
  }
}
