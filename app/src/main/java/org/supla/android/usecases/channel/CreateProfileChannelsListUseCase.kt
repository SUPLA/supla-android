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
import org.supla.android.core.shared.invoke
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.main.topbar.searchable
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.locationItem
import org.supla.android.usecases.list.toLocationSections
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.usecase.GetCaptionUseCase
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileChannelsListUseCase @Inject constructor(
  private val observeChannelsTreeSnapshotUseCase: ObserveChannelsTreeSnapshotUseCase,
  private val channelToListItemMapper: ChannelToListItemMapper,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val preferences: ApplicationPreferences,
  @param:ApplicationContext private val context: Context
) {

  operator fun invoke(filterString: String = ""): Observable<List<ListItem>> =
    observeChannelsTreeSnapshotUseCase()
      .throttleLatest(250, TimeUnit.MILLISECONDS, true)
      .map { snapshot ->
        val channels = mutableListOf<ListItem>()
        val sourceChannels = if (preferences.hideUnavailableChannels) {
          snapshot.channels.filterNot {
            it.channelValueEntity.lastOnlineState == SuplaChannelAvailabilityStatus.ONLINE_BUT_NOT_AVAILABLE
          }
        } else {
          snapshot.channels
        }

        sourceChannels
          .toLocationSections(
            locationOf = { it.locationEntity },
            positionOf = { it.channelEntity.position }
          )
          .forEach { section ->
            val visibleChannels = section.items.filter {
              if (snapshot.childChannelIds.contains(it.remoteId)) {
                // Skip channels which have parent ID.
                return@filter false
              }
              if (!filterString.searchable) {
                return@filter true
              }

              val caption = getCaptionUseCase.invoke(it.shareable)(context)
              val captionContains = caption.contains(filterString, ignoreCase = true)
              val locationContains = it.locationEntity.caption.contains(filterString, ignoreCase = true)
              captionContains || locationContains
            }

            if (visibleChannels.isEmpty()) {
              return@forEach
            }

            val location = visibleChannels.minBy { it.locationEntity.sortOrder }.locationEntity
            channels.add(location.locationItem(CollapsedFlag.CHANNEL))

            if (!location.isCollapsed(CollapsedFlag.CHANNEL) || filterString.searchable) {
              visibleChannels.forEach {
                snapshot.channelsWithChildren[it.remoteId]?.let { channel ->
                  channels.add(channelToListItemMapper(channel))
                }
              }
            }
          }

        channels.toList()
      }
}
