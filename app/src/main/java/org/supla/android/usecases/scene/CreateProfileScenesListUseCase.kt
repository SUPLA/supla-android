package org.supla.android.usecases.scene
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
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileScenesListUseCase @Inject constructor(
  private val roomSceneRepository: RoomSceneRepository
) {
  operator fun invoke(): Observable<List<ListItem>> =
    roomSceneRepository.findList().map { entities ->
      val result = mutableListOf<ListItem>()

      var location: LocationEntity? = null
      entities.forEach {
        val currentLocation = location
        if (currentLocation == null || currentLocation.remoteId != it.locationEntity.remoteId) {
          val newLocation = it.locationEntity

          if (currentLocation == null || newLocation.caption != currentLocation.caption) {
            location = newLocation
            result.add(ListItem.LocationItem(newLocation))
          }
        }

        location.let { locationEntity ->
          if (!locationEntity.isCollapsed(CollapsedFlag.SCENE)) {
            result.add(ListItem.SceneItem(it))
          }
        }
      }

      result.toList()
    }.toObservable()
}
