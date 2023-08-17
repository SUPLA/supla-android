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
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ListsEventsManager
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.channel.ReadChannelWithChildrenUseCase
import org.supla.android.usecases.list.eventmappers.ThermostatUpdateEventMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateListItemUpdateEventDataUseCase @Inject constructor(
  private val eventsManager: ListsEventsManager,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val readChannelWithChildrenUseCase: ReadChannelWithChildrenUseCase,
  thermostatUpdateEventMapper: ThermostatUpdateEventMapper
) {

  private val mappers: List<Mapper> = listOf(thermostatUpdateEventMapper)

  operator fun invoke(itemType: ItemType, remoteId: Int): Observable<SlideableListItemData> {
    return when (itemType) {
      ItemType.CHANNEL -> eventsManager.observeChannel(remoteId).flatMapMaybe { readChannelWithChildrenUseCase(remoteId) }
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

  interface Mapper {
    fun handle(item: Any): Boolean
    fun map(item: Any): SlideableListItemData
  }
}
