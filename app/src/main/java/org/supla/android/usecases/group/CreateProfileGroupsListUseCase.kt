package org.supla.android.usecases.group
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
    }
}
