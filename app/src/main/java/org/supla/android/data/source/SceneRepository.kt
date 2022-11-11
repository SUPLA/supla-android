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

import io.reactivex.rxjava3.core.Observable
import org.supla.android.db.Scene
import org.supla.android.lib.SuplaScene
import org.supla.android.lib.SuplaSceneState

interface SceneRepository {

  fun getAllProfileScenes(): Observable<List<Scene>>
  fun getSceneUserIconIdsToDownload(): List<Int>
  fun getScene(id: Int): Scene?
  fun updateScene(scene: Scene): Boolean
  fun updateSuplaScene(suplaScene: SuplaScene): Boolean
  fun updateSuplaSceneState(suplaSceneState: SuplaSceneState): Boolean
  fun setScenesVisible(visible: Int, whereVisible: Int): Boolean

  suspend fun reloadScenes()
}
