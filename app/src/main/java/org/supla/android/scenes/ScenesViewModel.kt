package org.supla.android.scenes
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.data.source.local.LocationDao
import org.supla.android.db.DbHelper
import org.supla.android.db.Scene
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg
import javax.inject.Inject

@HiltViewModel
class ScenesViewModel @Inject constructor(
  private val messageHandler: SuplaClientMessageHandler,
  private val sceneEventsManager: SceneEventsManager,
  private val dispatchers: CoroutineDispatchers,
  dbHelper: DbHelper,
  scController: SceneController
) : ViewModel(), SuplaClientMessageHandler.OnSuplaClientMessageListener {

  private var _scenes = MutableLiveData<List<Scene>>(emptyList())
  private val _sceneRepo = dbHelper.sceneRepository

  val scenes: LiveData<List<Scene>> = _scenes

  val scenesAdapter = ScenesAdapter(
    this, LocationDao(dbHelper),
    scController
  )

  init {
    viewModelScope.launch {
      withContext(dispatchers.io()) {
        // First initialize all scenes
        val userScenes = _sceneRepo.getAllProfileScenes()
        userScenes.stream().forEach { emitSceneStateChange(it) }
        _scenes.postValue(userScenes)

        // After that start observe
        messageHandler.registerMessageListener(this@ScenesViewModel)
      }
    }
  }

  fun onLocationStateChanged() {
    reload()
  }

  fun onSceneOrderUpdate(scenes: List<Scene>) {
    var si = 0
    for (s in scenes) {
      s.sortOrder = si++
      _sceneRepo.updateScene(s)
    }
    reload()
  }

  override fun onSuplaClientMessageReceived(msg: SuplaClientMsg) {
    if (msg.type == SuplaClientMsg.onSceneStateChanged) {
      val scene = _sceneRepo.getScene(msg.sceneId) ?: return
      emitSceneStateChange(scene)
    }
  }

  fun reload() {
    viewModelScope.launch {
      withContext(dispatchers.io()) {
        // First initialize all scenes
        val userScenes = _sceneRepo.getAllProfileScenes()
        userScenes.stream().forEach { emitSceneStateChange(it) }
        _scenes.postValue(userScenes)
      }
    }
  }

  override fun onCleared() {
    messageHandler.unregisterMessageListener(this)
  }

  private fun emitSceneStateChange(scene: Scene) {
    sceneEventsManager.emitStateChange(
      scene.sceneId,
      SceneEventsManager.SceneState(scene.isExecuting(), scene.estimatedEndDate)
    )
  }
}
