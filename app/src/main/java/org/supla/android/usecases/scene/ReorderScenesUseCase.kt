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

import org.supla.android.data.source.SceneRepository
import org.supla.android.ui.lists.ListItem
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReorderScenesUseCase @Inject constructor(
  private val sceneRepository: SceneRepository
) {

  suspend operator fun invoke(items: List<ListItem>, movedItemId: Int) {
    val moved = items.filterIsInstance<ListItem.SceneItem>().firstOrNull { it.remoteId == movedItemId } ?: return

    val locations = items.filterIsInstance<ListItem.LocationItem>().filter { it.userCaption == moved.locationCaption }
    if (locations.isEmpty()) {
      Timber.w("No location found, reorder stopped!")
      return
    }

    var useId = true
    if (locations.size > 1) {
      useId = false
    }

    val orderedScenes =
      if (useId) {
        items.filterIsInstance<ListItem.SceneItem>().filter { it.locationId == moved.locationId }
      } else {
        items.filterIsInstance<ListItem.SceneItem>().filter { it.locationCaption == moved.locationCaption }
      }

    var position = 1
    for (scene in orderedScenes) {
      sceneRepository.updatePosition(scene.remoteId, position++)
    }
  }
}
