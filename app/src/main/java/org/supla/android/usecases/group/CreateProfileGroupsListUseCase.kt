package org.supla.android.usecases.group

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.supla.android.core.shared.invoke
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.locationItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.core.shared.usecase.GetCaptionUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileGroupsListUseCase @Inject constructor(
  private val channelGroupRepository: ChannelGroupRepository,
  private val groupToListItemMapper: GroupToListItemMapper,
  private val getCaptionUseCase: GetCaptionUseCase,
  @param:ApplicationContext private val context: Context
) {
  operator fun invoke(filterString: String = ""): Observable<List<ListItem>> =
    channelGroupRepository.findList().map { entities ->
      val groups = mutableListOf<ListItem>()

      var location: LocationEntity? = null
      entities.forEach {
        if (filterString.length > 1) {
          val caption = getCaptionUseCase.invoke(it.shareable)(context)
          if (!caption.contains(filterString, ignoreCase = true)) {
            // Skip filtered out channels
            return@forEach
          }
        }

        val currentLocation = location
        if (currentLocation == null || currentLocation.remoteId != it.locationId) {
          val newLocation = it.locationEntity

          if (currentLocation == null || newLocation.caption != currentLocation.caption) {
            location = newLocation
            groups.add(newLocation.locationItem(CollapsedFlag.GROUP))
          }
        }

        location.let { locationEntity ->
          if (!locationEntity.isCollapsed(CollapsedFlag.GROUP) || filterString.length > 1) {
            groups.add(groupToListItemMapper(it))
          }
        }
      }

      groups.toList()
    }.toObservable()
}
