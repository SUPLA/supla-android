package org.supla.android.features.scenelist
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
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dagger.hilt.android.qualifiers.ActivityContext
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.LiSceneItemBinding
import org.supla.android.db.Location
import org.supla.android.db.entity.Scene
import org.supla.android.ui.dialogs.SceneCaptionEditor
import org.supla.android.ui.layouts.SceneLayout
import org.supla.android.ui.lists.BaseListAdapter
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import javax.inject.Inject

class ScenesAdapter @Inject constructor(
  @ActivityContext private val context: Context,
  preferences: Preferences
) : BaseListAdapter<ListItem, Scene>(context, preferences), SceneLayout.Listener {

  override val callback = SceneListCallback(context, this).also {
    it.onMovedListener = { fromPos, toPos -> swapInternally(fromPos, toPos) }
    it.onMoveFinishedListener = {
      val scenesOrdered = items
        .filterIsInstance<ListItem.SceneItem>()
        .map { sceneItem -> sceneItem.scene }

      if (movedItem != replacedItem) {
        swappedElementsCallback(
          (movedItem as? ListItem.SceneItem)?.scene,
          (replacedItem as? ListItem.SceneItem)?.scene
        )
      }
      movementFinishedCallback(scenesOrdered)
      cleanSwap()
    }
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      R.layout.li_scene_item -> {
        val binding = LiSceneItemBinding.inflate(inflater, parent, false)
        val holder = SceneListItemViewHolder(binding)
        holder
      }
      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
    when (vh) {
      is SceneListItemViewHolder -> {
        val item = (items[pos] as ListItem.SceneItem)
        vh.binding.sceneLayout.tag = item.scene.sceneId
        vh.binding.sceneLayout.setSceneListener(this)
        vh.binding.sceneLayout.setScene(item.scene)
        vh.binding.sceneLayout.setLocationCaption(item.location.caption)
        vh.binding.sceneLayout.setOnLongClickListener { onLongPress(vh) }
      }
      else -> super.onBindViewHolder(vh, pos)
    }
  }

  override fun isLocationCollapsed(location: Location) = ((location.collapsed and CollapsedFlag.SCENE.value) > 0)

  override fun getItemViewType(pos: Int): Int {
    return if (items[pos] is ListItem.SceneItem) {
      R.layout.li_scene_item
    } else {
      R.layout.li_location_item
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun onCaptionLongPress(sceneId: Int) {
    SuplaApp.Vibrate(context)
    val editor = SceneCaptionEditor(context)
    editor.captionChangedListener = reloadCallback
    editor.edit(sceneId)
  }

  private fun onLongPress(viewHolder: ViewHolder): Boolean {
    SuplaApp.Vibrate(context)
    callback.closeWhenSwiped()
    itemTouchHelper.startDrag(viewHolder)

    return true
  }

  inner class SceneListItemViewHolder(val binding: LiSceneItemBinding) : ViewHolder(binding.root)
}
