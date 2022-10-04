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
import androidx.databinding.BaseObservable
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import org.supla.android.db.Scene
import org.supla.android.images.ImageId
import org.supla.android.R
import org.supla.android.BR
import org.supla.android.Trace

class SceneListItemViewModel(val scene: Scene,
                             private val controller: SceneController,
                             private val viewModelScope: CoroutineScope,
                             private val clockSource: SharedFlow<Unit>): BaseObservable() {

    val sceneName: String
        get() = scene.caption

    private val standardIcons = listOf(R.drawable.scene0, R.drawable.scene1,
                                       R.drawable.scene2, R.drawable.scene3,
                                       R.drawable.scene4, R.drawable.scene5,
                                       R.drawable.scene6, R.drawable.scene7,
                                       R.drawable.scene8, R.drawable.scene9,
                                       R.drawable.scene10, R.drawable.scene11,
                                       R.drawable.scene12, R.drawable.scene13,
                                       R.drawable.scene14, R.drawable.scene15,
                                       R.drawable.scene16, R.drawable.scene17,
                                       R.drawable.scene18, R.drawable.scene19)

    val sceneIcon: ImageId 
        get() {
            val iconId: Int
            if(scene.altIcon > 0) {
                iconId = scene.altIcon
            } else {
                iconId = scene.userIcon
            }
            if(iconId < standardIcons.size) {
                return ImageId(standardIcons[iconId])
            } else {
                return ImageId(iconId, 0)
            }
    }

    val sceneInitiator: String?
        get() = scene.initiatorName

    private val UNKNOWN_TIME = "--:--:--"
    private var _timeSinceStart = UNKNOWN_TIME
    var timeSinceStart: String
      @Bindable get() = _timeSinceStart
      set(value) {
          if(_timeSinceStart != value) {
              _timeSinceStart = value
              notifyPropertyChanged(BR.timeSinceStart)
          }
      }

    private var _executing = false

    init {
        val sst = scene.startedAt
        val now = Date()
        timeSinceStart = computeTimeSinceStart(now)
        if(sst != null && sst < now) {
            val eet = scene.estimatedEndDate
            if(eet == null || eet > now) {
                _executing = true
            }
        }

        viewModelScope.launch {
            clockSource.collect {
                val now = Date()
                timeSinceStart = computeTimeSinceStart(now)
                val eet = scene.estimatedEndDate
                if(eet != null && eet < now && _executing) {
                    _executing = false
                }
            }
        }
    }

    fun startStopScene() {
        if(scene.startedAt == null || 
           (scene.estimatedEndDate != null &&
            scene.estimatedEndDate!!.compareTo(Date()) < 0)) {
            controller.startScene(scene.sceneId)
        } else {
            controller.stopScene(scene.sceneId)
        }
    }

    private fun formatMillis(v: Long): String {
        var r = v
        var k: Long = 0
        var rv = ""
        k = r / 3600000
        rv += String.format("%02d:", k)
        r -= k * 3600000
        k = r / 60000
        rv += String.format("%02d:", k)
        r -= k * 60000
        rv += String.format("%02d", r / 1000)

        return rv
    }

    private fun computeTimeSinceStart(now: Date): String {
        val sst = scene.startedAt
        val eet = scene.estimatedEndDate
        if(sst != null && sst < now && (eet == null || eet.time > now.time)) {
            val diff = now.time - sst.time
            val rv = formatMillis(diff)
            return rv
        } else {
            return UNKNOWN_TIME
        }
    }
}
