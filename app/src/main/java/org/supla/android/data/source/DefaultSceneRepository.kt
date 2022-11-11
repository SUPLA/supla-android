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
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.supla.android.data.source.local.SceneDao
import org.supla.android.db.Scene
import org.supla.android.lib.SuplaScene
import org.supla.android.lib.SuplaSceneState
import java.util.concurrent.TimeUnit

class DefaultSceneRepository(private val dao: SceneDao) : SceneRepository {

  private val scenesSubject: Subject<List<Scene>> = PublishSubject.create()
  private var remissionId: UInt = 0u
  private val remissionSubject: Subject<Int> = PublishSubject.create()

  init {
    // This strange construction is needed to proper reload the list of scenes when
    // something was added on the web interface. It simulates the reactive behavior of the database
    // (something like Rx with Room).
    // Debounce is used for optimization -> to avoid many reloads.
    remissionSubject.subscribeOn(Schedulers.io())
      .debounce(100, TimeUnit.MILLISECONDS)
      .subscribe { scenesSubject.onNext(loadScenes()) }
  }

  override fun getAllProfileScenes(): Observable<List<Scene>> {
    return scenesSubject.hide()
      .doOnSubscribe { scenesSubject.onNext(loadScenes()) }
  }

  override fun getScene(id: Int): Scene? {
    return dao.getSceneByRemoteId(id)
  }

  override fun updateScene(scene: Scene): Boolean {
    return dao.updateScene(scene)
  }

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

    if (result) {
      // We're notifying only when something changed.
      remissionSubject.onNext(remissionId++.toInt())
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

  override fun setScenesVisible(visible: Int, whereVisible: Int): Boolean {
    remissionSubject.onNext(remissionId++.toInt())
    return dao.setScenesVisible(visible, whereVisible)
  }

  override suspend fun reloadScenes() {
    scenesSubject.onNext(loadScenes())
  }

  override fun getSceneUserIconIds(): List<Int> {
    return loadScenes().flatMap { listOf(it.altIcon, it.userIcon) }
  }

  private fun loadScenes(): List<Scene> {
    val rv = mutableListOf<Scene>()
    val cur = dao.sceneCursor()

    cur.moveToFirst()
    while (!cur.isAfterLast) {
      val itm = Scene()
      itm.AssignCursorData(cur)
      rv.add(itm)
      cur.moveToNext()
    }
    cur.close()

    return rv
  }
}
