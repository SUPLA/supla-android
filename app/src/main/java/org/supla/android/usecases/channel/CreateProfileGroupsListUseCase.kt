package org.supla.android.usecases.channel

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.ChannelGroup
import org.supla.android.db.Location
import org.supla.android.profile.ProfileManager
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.isCollapsed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileGroupsListUseCase @Inject constructor(
  private val channelRepository: ChannelRepository,
  private val profileManager: ProfileManager
) {
  operator fun invoke(): Observable<List<ListItem>> =
    profileManager.getCurrentProfile().flatMapObservable(this::groupsObservable)

  private fun groupsObservable(currentProfile: AuthProfileItem): Observable<List<ListItem>> =
    Observable.fromCallable {
      channelRepository.getAllProfileChannelGroups(currentProfile.id).use { cursor ->
        val channels = mutableListOf<ListItem>()

        var location: Location? = null
        if (cursor.moveToFirst()) {
          do {
            val group = ChannelGroup()
            group.AssignCursorData(cursor)

            if (location == null || location.locationId != group.locationId.toInt()) {
              location = channelRepository.getLocation(group.locationId.toInt())
              channels.add(ListItem.LocationItem(location))
            }

            if (location?.isCollapsed(CollapsedFlag.GROUP) == false) {
              channels.add(ListItem.ChannelItem(group))
            }
          } while (cursor.moveToNext())
        }

        return@use channels
      }
    }
}
