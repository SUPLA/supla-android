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
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.db.Location
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.isCollapsed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileScenesListUseCase @Inject constructor(
  private val sceneRepository: SceneRepository,
  private val channelRepository: ChannelRepository
) {
  operator fun invoke(): Observable<List<ListItem>> =
    sceneRepository.getAllProfileScenes()
      .map(this::sceneToListItem)

  private fun sceneToListItem(scenes: List<Scene>): List<ListItem> {
    val result = mutableListOf<ListItem>()

    var location: Location? = null
    for (scene in scenes) {
      if (location == null || location.locationId != scene.locationId) {
        val newLocation = channelRepository.getLocation(scene.locationId)

        if (location == null || newLocation.caption != location.caption) {
          location = newLocation
          result.add(ListItem.LocationItem(location))
        }
      }

      if (location?.isCollapsed(CollapsedFlag.SCENE) == false) {
        result.add(ListItem.SceneItem(scene, location))
      }
    }

    return result
  }
}
