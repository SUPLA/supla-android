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

import android.util.Log
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.db.Location
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaConst.SUPLA_CHANNEL_FLAG_HAS_PARENT
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.channelrelation.FindChannelChildrenUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.isCollapsed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileChannelsListUseCase @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val profileManager: ProfileManager,
  private val channelRelationRepository: ChannelRelationRepository,
  private val findChannelChildrenUseCase: FindChannelChildrenUseCase
) {

  operator fun invoke(): Observable<List<ListItem>> =
    profileManager.getCurrentProfile().flatMapObservable(this::channelsObservable)

  private fun channelsObservable(currentProfile: AuthProfileItem): Observable<List<ListItem>> =
    Observable.fromCallable {
      val relationMap = blockingCall(channelRelationRepository.findListOfParents(currentProfile.id))

      channelRepository.getAllProfileChannels(currentProfile.id).use { cursor ->
        val channels = mutableListOf<ListItem>()

        var location: Location? = null
        if (cursor.moveToFirst()) {
          do {
            val channel = Channel()
            channel.AssignCursorData(cursor)

            if (channel.flags and SUPLA_CHANNEL_FLAG_HAS_PARENT > 0) {
              // Skip channels which have parent ID.
              continue
            }

            if (location == null || location.locationId != channel.locationId.toInt()) {
              val newLocation = channelRepository.getLocation(channel.locationId.toInt())

              if (location == null || newLocation.caption != location.caption) {
                location = newLocation
                channels.add(ListItem.LocationItem(location))
              }
            }

            if (location?.isCollapsed(CollapsedFlag.CHANNEL) == false) {
              if (relationMap.contains(channel.channelId)) {
                val children = blockingCall(findChannelChildrenUseCase(currentProfile.id, channel.remoteId))
                channels.add(ListItem.ChannelItem(channel, location, children))
              } else {
                channels.add(ListItem.ChannelItem(channel, location))
              }
            }
          } while (cursor.moveToNext())
        }

        return@use channels
      }
    }

  private fun <T> blockingCall(observable: Observable<List<T>>): List<T> {
    return try {
      observable.blockingFirst()
    } catch (ex: Exception) {
      Log.w(TAG, "Could not get blocking call", ex)
      emptyList()
    }
  }

  private fun <T> blockingCall(maybe: Maybe<List<T>>): List<T>? {
    return try {
      maybe.blockingGet()
    } catch (ex: Exception) {
      Log.w(TAG, "Could not get blocking call", ex)
      emptyList()
    }
  }
}
