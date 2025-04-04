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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.Trace
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.data.source.RoomSceneRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoveHiddenScenesUseCase @Inject constructor(
  private val sceneRepository: RoomSceneRepository,
  androidAutoItemRepository: AndroidAutoItemRepository
) {
  private val relatedRepositories: List<ScenesDeletable> = listOf(
    sceneRepository,
    androidAutoItemRepository
  )

  suspend operator fun invoke() {
    withContext(Dispatchers.IO) {
      val hiddenScenes = sceneRepository.findHiddenScenes()
      Trace.i(TAG, "Found scenes to remove: ${hiddenScenes.count()}")

      hiddenScenes.flatMap { scene ->
        relatedRepositories.map {
          launch { it.deleteScenesRelated(scene.remoteId, scene.profileId!!.toLong()) }
        }
      }.joinAll()

      Trace.i(TAG, "Hidden scenes removal finished")
    }
  }

  interface ScenesDeletable {
    suspend fun deleteScenesRelated(remoteId: Int, profileId: Long)
  }

  companion object {
    private val TAG = RemoveHiddenScenesUseCase::class.java.simpleName
  }
}
