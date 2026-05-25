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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.lib.SuplaSceneState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateSceneStateUseCase @Inject constructor(
  private val sceneRepository: RoomSceneRepository
) {

  operator fun invoke(suplaSceneState: SuplaSceneState): Boolean =
    runBlocking {
      withContext(Dispatchers.IO) {
        updateSceneState(suplaSceneState)
      }
    }

  private suspend fun updateSceneState(suplaSceneState: SuplaSceneState): Boolean {
    val scene = sceneRepository.findByRemoteIdKtx(suplaSceneState.sceneId) ?: return false

    val updatedScene = scene.copy(
      startedAt = suplaSceneState.startedAt,
      estimatedEndDate = suplaSceneState.estimatedEndDate,
      initiatorId = suplaSceneState.initiatorId,
      initiatorName = suplaSceneState.initiatorName
    )

    return if (scene == updatedScene) {
      false
    } else {
      sceneRepository.update(updatedScene)
      true
    }
  }
}
