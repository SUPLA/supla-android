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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.local.dao.SceneDao
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.usecases.captionchange.CaptionChangeUseCase
import org.supla.android.usecases.developerinfo.CountProvider
import org.supla.android.usecases.scene.RemoveHiddenScenesUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSceneRepository @Inject constructor(
  private val sceneDao: SceneDao
) : CountProvider, CaptionChangeUseCase.Updater, RemoveHiddenScenesUseCase.ScenesDeletable {

  fun findByRemoteId(remoteId: Int) = sceneDao.findByRemoteId(remoteId)

  suspend fun findHiddenScenes() = sceneDao.findHiddenScenes()

  fun findList() = sceneDao.findList()

  fun update(scenes: List<SceneEntity>) = sceneDao.update(scenes)

  fun findProfileScenes(profileId: Long) = sceneDao.findProfileScenes(profileId)

  override fun count(): Observable<Int> = sceneDao.count()

  override fun updateCaption(caption: String, remoteId: Int, profileId: Long): Completable =
    sceneDao.updateCaption(caption, remoteId, profileId)

  override suspend fun deleteScenesRelated(remoteId: Int, profileId: Long) =
    sceneDao.deleteScene(profileId, remoteId)
}
