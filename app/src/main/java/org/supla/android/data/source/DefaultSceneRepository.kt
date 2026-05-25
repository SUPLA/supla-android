package org.supla.android.data.source

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

import org.supla.android.data.source.local.SceneDao
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.lib.SuplaScene
import org.supla.android.lib.SuplaSceneState

class DefaultSceneRepository(private val dao: SceneDao) : SceneRepository {

  override fun updateSuplaScene(suplaScene: SuplaScene): Boolean {
    val scene = dao.getSceneByRemoteId(suplaScene.id)

    val result = if (scene == null) {
      val newScene = Scene()
      newScene.assign(suplaScene)
      newScene.visible = 1
      dao.insertScene(newScene)
    } else {
      val clone = scene.clone()
      clone.visible = 1
      clone.assign(suplaScene)
      if (scene == clone) {
        // no need to update, received scene matches current
        // persistent representation
        false
      } else {
        dao.updateScene(clone)
      }
    }

    return result
  }

  override fun updateSuplaSceneState(suplaSceneState: SuplaSceneState): Boolean {
    val scene = dao.getSceneByRemoteId(suplaSceneState.sceneId) ?: return false

    val cloned = scene.clone()
    cloned.assign(suplaSceneState)
    return if (scene == cloned) {
      // no change in data
      false
    } else {
      dao.updateScene(cloned)
    }
  }
}
