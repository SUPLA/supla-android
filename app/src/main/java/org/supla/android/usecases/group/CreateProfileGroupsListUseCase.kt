package org.supla.android.usecases.group

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileGroupsListUseCase @Inject constructor(
  private val channelGroupRepository: ChannelGroupRepository
) {
  operator fun invoke(): Observable<List<ListItem>> =
    channelGroupRepository.findList().map { entities ->
      val groups = mutableListOf<ListItem>()

      var location: LocationEntity? = null
      entities.forEach {
        val currentLocation = location
        if (currentLocation == null || currentLocation.remoteId != it.locationId) {
          val newLocation = it.locationEntity

          if (currentLocation == null || newLocation.caption != currentLocation.caption) {
            location = newLocation
            groups.add(ListItem.LocationItem(newLocation))
          }
        }

        location.let { locationEntity ->
          if (!locationEntity.isCollapsed(CollapsedFlag.GROUP)) {
            groups.add(ListItem.ChannelItem(it, it.getLegacyGroup()))
          }
        }
      }

      groups.toList()
    }.toObservable()
}
