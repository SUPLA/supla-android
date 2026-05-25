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
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.lib.SuplaScene
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateSceneUseCase @Inject constructor(
  private val sceneRepository: SceneRepository,
  private val profileRepository: ProfileRepository
) {

  operator fun invoke(suplaScene: SuplaScene): Boolean =
    runBlocking {
      withContext(Dispatchers.IO) {
        updateScene(suplaScene)
      }
    }

  private suspend fun updateScene(suplaScene: SuplaScene): Boolean {
    val scene = sceneRepository.findByRemoteIdKtx(suplaScene.id)
      ?: return insertScene(suplaScene)

    val updatedScene = scene.copy(
      visible = 1,
      locationId = suplaScene.locationId,
      altIcon = suplaScene.altIcon,
      userIcon = suplaScene.userIcon,
      caption = suplaScene.caption
    )

    return if (scene == updatedScene) {
      false
    } else {
      sceneRepository.update(updatedScene)
      true
    }
  }

  private suspend fun insertScene(suplaScene: SuplaScene): Boolean {
    val profile = profileRepository.findActiveProfileKtx() ?: return false
    val profileId = profile.id ?: return false

    sceneRepository.insert(
      SceneEntity(
        id = null,
        remoteId = suplaScene.id,
        locationId = suplaScene.locationId,
        altIcon = suplaScene.altIcon,
        userIcon = suplaScene.userIcon,
        caption = suplaScene.caption,
        startedAt = null,
        estimatedEndDate = null,
        initiatorId = null,
        initiatorName = null,
        sortOrder = 0,
        visible = 1,
        profileId = profileId.toString()
      )
    )

    return true
  }
}
