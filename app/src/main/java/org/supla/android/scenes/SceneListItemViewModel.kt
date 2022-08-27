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

import androidx.databinding.Bindable
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import org.supla.android.db.Scene
import org.supla.android.images.ImageId
import org.supla.android.R
import org.supla.android.Trace

class SceneListItemViewModel(val scene: Scene,
                             private val controller: SceneController) {

    val sceneName: String
        get() = scene.caption

    val sceneIcon: ImageId 
        get() {
        if(scene.userIcon > 0) {
            return ImageId(scene.userIcon, 0)
        } else if(scene.altIcon > 0) {
            return ImageId(scene.altIcon, 1)
        } else {
            Trace.d(TAG, "returning coffee")
            return ImageId(R.drawable.coffee_black)
        }
    }

    val sceneInitiator: String?
        get() = scene.initiatorName

    val timeSinceStart: String = computeTimeSinceStart()

    private val TAG = "supla"

    fun startStopScene() {
        Trace.d(TAG, "start stop scene")
        if(scene.startedAt == null) {
            controller.startScene(scene.sceneId)
        } else {
            controller.stopScene(scene.sceneId)
        }
    }

    private fun computeTimeSinceStart(): String {
        val std = scene.startedAt
        if(std != null) {
            val res = Date(Date().time - std.time) 
            val fmt = SimpleDateFormat("HH:mm:ss")
            return fmt.format(res)!!
        } else {
            return "--:--:--"
        }
    }
}
