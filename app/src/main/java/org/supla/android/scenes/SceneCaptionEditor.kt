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

import android.content.Context
import org.supla.android.db.DbHelper
import org.supla.android.CaptionEditor
import org.supla.android.SuplaApp
import org.supla.android.R

class SceneCaptionEditor(context: Context) : CaptionEditor(context) {

  interface Listener {
    fun onCaptionChange()
  }

  var listener: Listener? = null

  override fun getTitle(): Int {
    return R.string.scene_name
  }

  override fun getCaption(): String {
    val repo = DbHelper.getInstance(context).sceneRepository
    val scene = repo.getScene(id)!!
    return scene.caption
  }

  override fun applyChanged(newCaption: String) {
    SuplaApp.getApp().suplaClient.renameScene(id, newCaption)
    val repo = DbHelper.getInstance(context).sceneRepository
    val scene = repo.getScene(id)!!
    scene.caption = newCaption
    repo.updateScene(scene)
    listener?.onCaptionChange()
  }

  override fun getHint(): Int {
    return R.string.str_default
  }
}
