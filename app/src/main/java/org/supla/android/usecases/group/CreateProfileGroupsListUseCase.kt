package org.supla.android.usecases.group

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.supla.android.core.shared.invoke
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.main.topbar.searchable
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.locationItem
import org.supla.android.usecases.list.toLocationSections
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

      entities.toLocationSections(
        locationOf = { it.locationEntity },
        positionOf = { it.channelGroupEntity.position }
      ).forEach { section ->
        val visibleGroups = section.items.filter {
          if (!filterString.searchable) {
            return@filter true
          }

          val caption = getCaptionUseCase.invoke(it.shareable)(context)
          val captionContains = caption.contains(filterString, ignoreCase = true)
          val locationContains = it.locationEntity.caption.contains(filterString, ignoreCase = true)
          captionContains || locationContains
        }

        if (visibleGroups.isEmpty()) {
          return@forEach
        }

        val location = visibleGroups.minBy { it.locationEntity.sortOrder }.locationEntity
        groups.add(location.locationItem(CollapsedFlag.GROUP))

        if (!location.isCollapsed(CollapsedFlag.GROUP) || filterString.searchable) {
          visibleGroups.forEach {
            groups.add(groupToListItemMapper(it))
          }
        }
      }

      groups.toList()
    }.toObservable()
}
