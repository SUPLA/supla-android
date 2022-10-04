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

import android.database.Cursor
import org.supla.android.db.Scene
import org.supla.android.lib.SuplaScene
import org.supla.android.lib.SuplaSceneState
import org.supla.android.data.source.local.SceneDao

public class DefaultSceneRepository(private val dao: SceneDao): SceneRepository {


    override fun getAllProfileScenes(): List<Scene> {
        var rv = mutableListOf<Scene>()
        val cur = dao.sceneCursor()

        cur.moveToFirst()
        while(!cur.isAfterLast()) {
            val itm = Scene()
            itm.AssignCursorData(cur)
            rv.add(itm)
            cur.moveToNext()
        }
        cur.close()

        return rv
    }

    override fun updateScene(scene: Scene): Boolean {
        return dao.updateScene(scene)
    }

    override fun updateSuplaScene(suplaScene: SuplaScene): Boolean {
        val scene = dao.getSceneByRemoteId(suplaScene.id)

        if(scene == null) {
            val newScene = Scene()
            newScene.assign(suplaScene)
            return dao.insertScene(newScene)
        } else {
            val sceneClone = scene.clone()
            sceneClone.assign(suplaScene)
            if(scene == sceneClone) {
                // no need to update, received scene matches current
                // persistent representation
                return false
            } else {
                return dao.updateScene(sceneClone)
            }
        }
    }

    override fun updateSuplaSceneState(state: SuplaSceneState): Boolean {
        val scene = dao.getSceneByRemoteId(state.sceneId)
        if(scene == null) { 
           return false 
        }

        val cloned = scene.clone()
        cloned.assign(state)
        if(scene == cloned) {
            // no change in data
            return false
        } else {
            return dao.updateScene(cloned)
        }
    }

    override fun getSceneUserIconIds(): List<Int> {
        return getAllProfileScenes().flatMap { listOf(it.altIcon, it.userIcon) }
    }
}
