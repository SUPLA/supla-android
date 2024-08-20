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
import org.supla.android.SuplaApp
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.databinding.LiSceneItemBinding
import org.supla.android.ui.dialogs.SceneCaptionEditor
import org.supla.android.ui.layouts.SceneLayout
import org.supla.android.ui.lists.BaseListAdapter
import org.supla.android.ui.lists.ListCallback
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import javax.inject.Inject

class ScenesAdapter @Inject constructor(
  @ActivityContext private val context: Context,
  preferences: Preferences
) : BaseListAdapter<ListItem, SceneDataEntity>(context, preferences), SceneLayout.Listener {

  override val callback = ListCallback(context, this).also {
    it.onMovedListener = { fromPos, toPos -> swapInternally(fromPos, toPos) }
    it.onMoveFinishedListener = {
      val scenesOrdered = items
        .filterIsInstance<ListItem.SceneItem>()
        .map { sceneItem -> sceneItem.sceneData }

      if (movedItem != replacedItem) {
        swappedElementsCallback(
          (movedItem as? ListItem.SceneItem)?.sceneData,
          (replacedItem as? ListItem.SceneItem)?.sceneData
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
      ViewType.SCENE_ITEM.ordinal -> {
        val binding = LiSceneItemBinding.inflate(inflater, parent, false)
        val holder = SceneListItemViewHolder(binding)
        holder
      }
      else -> super.onCreateViewHolder(parent, viewType)
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (holder) {
      is SceneListItemViewHolder -> {
        val item = (items[position] as ListItem.SceneItem)
        holder.binding.sceneLayout.tag = item.sceneData.remoteId
        holder.binding.sceneLayout.setSceneListener(this)
        holder.binding.sceneLayout.setScene(item.sceneData.sceneEntity)
        holder.binding.sceneLayout.setLocationCaption(item.sceneData.locationCaption)
        holder.binding.sceneLayout.setOnLongClickListener { onLongPress(holder) }
      }
      else -> super.onBindViewHolder(holder, position)
    }
  }

  override fun isLocationCollapsed(location: LocationEntity) = ((location.collapsed and CollapsedFlag.SCENE.value) > 0)

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
