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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import org.supla.android.core.shared.invoke
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.locationItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.usecase.GetCaptionUseCase
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileChannelsListUseCase @Inject constructor(
  private val getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase,
  private val channelRelationRepository: ChannelRelationRepository,
  private val channelToListItemMapper: ChannelToListItemMapper,
  private val channelRepository: ChannelRepository,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val preferences: ApplicationPreferences,
  @param:ApplicationContext private val context: Context
) {

  private val channelListSource: Single<List<ChannelDataEntity>>
    get() =
      if (preferences.hideUnavailableChannels) {
        channelRepository.findListWithoutUnavailable()
      } else {
        channelRepository.findList()
      }

  operator fun invoke(filterString: String = ""): Observable<List<ListItem>> =
    Single.zip(
      channelRelationRepository.findChildrenToParentsRelations().firstOrError(),
      channelListSource
    ) { relationMap, entities -> Pair(relationMap, entities) }
      .map { (relationMap, entities) ->
        val channels = mutableListOf<ListItem>()

        val channelsMap = mutableMapOf<Int, ChannelDataEntity>().also { map -> entities.forEach { map[it.remoteId] = it } }
        val allChildrenIds = relationMap.flatMap { it.value }.map { it.channelId }
        val childrenMap = mutableMapOf<Int, List<ChannelChildEntity?>>().also { map ->
          relationMap.forEach { relation ->
            val childrenList = LinkedList<Int>()
            map[relation.key] = getChannelChildrenTreeUseCase.invoke(relation.key, relationMap, channelsMap, childrenList)
          }
        }

        var location: LocationEntity? = null
        entities.forEach {
          if (allChildrenIds.contains(it.remoteId)) {
            // Skip channels which have parent ID.
            return@forEach
          }
          if (filterString.length > 1) {
            val caption = getCaptionUseCase.invoke(it.shareable)(context)
            if (!caption.contains(filterString, ignoreCase = true)) {
              // Skip filtered out channels
              return@forEach
            }
          }

          val currentLocation = location
          if (currentLocation == null || currentLocation.remoteId != it.locationEntity.remoteId) {
            val newLocation = it.locationEntity

            if (currentLocation == null || newLocation.caption != currentLocation.caption) {
              location = newLocation
              channels.add(location.locationItem(CollapsedFlag.CHANNEL))
            }
          }

          location.let { locationEntity ->
            if (!locationEntity.isCollapsed(CollapsedFlag.CHANNEL)) {
              channels.add(channelToListItemMapper(channelWithChildren(it, childrenMap)))
            }
          }
        }

        channels.toList()
      }.toObservable()

  private fun channelWithChildren(
    channelData: ChannelDataEntity,
    childrenMap: MutableMap<Int, List<ChannelChildEntity?>>
  ): ChannelWithChildren {
    val children = mutableListOf<ChannelChildEntity>().apply {
      childrenMap[channelData.remoteId]?.filterIsInstance<ChannelChildEntity>()?.let { addAll(it) }
    }
    return ChannelWithChildren(channelData, children)
  }
}
