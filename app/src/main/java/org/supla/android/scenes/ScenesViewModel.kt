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
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.Scene
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg
import javax.inject.Inject

@HiltViewModel
class ScenesViewModel @Inject constructor(
  private val messageHandler: SuplaClientMessageHandler,
  private val sceneEventsManager: SceneEventsManager,
  private val sceneRepository: SceneRepository
) : ViewModel(), SuplaClientMessageHandler.OnSuplaClientMessageListener {

  private val scenesDisposable: Disposable

  private var _scenes = MutableLiveData<List<Scene>>(emptyList())
  private var _loading = MutableLiveData(false)

  val scenes: LiveData<List<Scene>> = _scenes
  val loading: LiveData<Boolean> = _loading

  init {
    _loading.postValue(true)
    messageHandler.registerMessageListener(this@ScenesViewModel)

    scenesDisposable = sceneRepository.getAllProfileScenes()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe {
        _loading.postValue(false)
        for (scene in it) {
          emitSceneStateChange(scene)
        }
        _scenes.postValue(it)
      }
  }

  override fun onSuplaClientMessageReceived(msg: SuplaClientMsg) {
    if (msg.type == SuplaClientMsg.onSceneStateChanged) {
      val scene = sceneRepository.getScene(msg.sceneId) ?: return
      emitSceneStateChange(scene)
    }
  }

  override fun onCleared() {
    messageHandler.unregisterMessageListener(this)
    scenesDisposable.dispose()
  }

  private fun emitSceneStateChange(scene: Scene) {
    sceneEventsManager.emitStateChange(
      scene.sceneId,
      SceneEventsManager.SceneState(scene.isExecuting(), scene.estimatedEndDate)
    )
  }
}
