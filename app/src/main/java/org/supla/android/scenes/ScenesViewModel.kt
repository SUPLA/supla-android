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

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.profile.ProfileManager
import org.supla.android.db.Scene
import org.supla.android.db.DbHelper
import org.supla.android.data.source.local.LocationDao
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg

@HiltViewModel
class ScenesViewModel @Inject constructor(
    private val messageHandler: SuplaClientMessageHandler,
    private val dbHelper: DbHelper,
    private val scController: SceneController
): ViewModel(), SuplaClientMessageHandler.OnSuplaClientMessageListener {

    private var _scenes = MutableLiveData<List<Scene>>(emptyList())
    private val _sceneRepo = dbHelper.sceneRepository
    val scenes: LiveData<List<Scene>> = _scenes

    val scenesAdapter = ScenesAdapter(this, LocationDao(dbHelper),
                                      scController)

    init {
        messageHandler.registerMessageListener(this)
    }

    fun onLocationStateChanged() {
        reload()
    }

    fun onSceneOrderUpdate(scenes: List<Scene>) {
        var si = 0
        for(s in scenes) {
            s.sortOrder = si++
            _sceneRepo.updateScene(s)
        }
        reload()
    }

    override fun onSuplaClientMessageReceived(msg: SuplaClientMsg) {
        if(msg.type == SuplaClientMsg.onSceneStateChanged) {
            reload()
        }
    }

    fun reload() {
        _scenes.value = _sceneRepo.getAllProfileScenes()
    }

    fun reset() {
        scenesAdapter.invalidateAll()
    }

    override protected fun onCleared() {
        messageHandler.unregisterMessageListener(this)
    }

}
