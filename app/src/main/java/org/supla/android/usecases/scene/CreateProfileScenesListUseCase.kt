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
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.locationItem
import org.supla.android.ui.lists.sceneItem
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileScenesListUseCase @Inject constructor(
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val sceneRepository: SceneRepository
) {
  operator fun invoke(filterString: String = ""): Observable<List<ListItem>> =
    sceneRepository.findList().map { entities ->
      val result = mutableListOf<ListItem>()

      var location: LocationEntity? = null
      entities.forEach {
        if (filterString.length > 1) {
          if (!it.sceneEntity.caption.contains(filterString, ignoreCase = true)) {
            // Skip filtered out channels
            return@forEach
          }
        }

        val currentLocation = location
        if (currentLocation == null || currentLocation.remoteId != it.locationEntity.remoteId) {
          val newLocation = it.locationEntity

          if (currentLocation == null || newLocation.caption != currentLocation.caption) {
            location = newLocation
            result.add(location.locationItem(CollapsedFlag.SCENE))
          }
        }

        location.let { locationEntity ->
          if (!locationEntity.isCollapsed(CollapsedFlag.SCENE) || filterString.isNotEmpty()) {
            result.add(it.sceneItem(getSceneIconUseCase))
          }
        }
      }

      result.toList()
    }.toObservable()
}
