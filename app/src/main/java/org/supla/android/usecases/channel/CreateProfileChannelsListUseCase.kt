package org.supla.android.usecases.channel

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.db.Location
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.isCollapsed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileChannelsListUseCase @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val profileManager: ProfileManager
) {

  operator fun invoke(): Observable<List<ListItem>> =
    profileManager.getCurrentProfile().flatMapObservable(this::channelsObservable)

  private fun channelsObservable(currentProfile: AuthProfileItem): Observable<List<ListItem>> =
    Observable.fromCallable {
      channelRepository.getAllProfileChannels(currentProfile.id).use { cursor ->
        val channels = mutableListOf<ListItem>()

        var location: Location? = null
        if (cursor.moveToFirst()) {
          do {
            val channel = Channel()
            channel.AssignCursorData(cursor)

            if (location == null || location.locationId != channel.locationId.toInt()) {
              val newLocation = channelRepository.getLocation(channel.locationId.toInt())

              if (location == null || newLocation.caption != location.caption) {
                location = newLocation
                channels.add(ListItem.LocationItem(location))
              }
            }

            if (location?.isCollapsed(CollapsedFlag.CHANNEL) == false) {
              channels.add(ListItem.ChannelItem(channel, location))
            }
          } while (cursor.moveToNext())
        }

        return@use channels
      }
    }
}
