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
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dagger.hilt.android.qualifiers.ActivityContext
import org.supla.android.LocationCaptionEditor
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.data.source.ChannelRepository
import org.supla.android.databinding.LocationListItemBinding
import org.supla.android.databinding.SceneListItemBinding
import org.supla.android.db.Location
import org.supla.android.db.Scene
import javax.inject.Inject


class ScenesAdapter @Inject constructor(
  @ActivityContext private val context: Context,
  private val channelRepository: ChannelRepository
) : RecyclerView.Adapter<ViewHolder>(),
  SceneLayout.Listener {

  private var _sections: List<Section> = emptyList()
  private var _vTypes: List<Int> = emptyList()
  private var _paths: List<Path> = emptyList()

  private val callback = ScenesListCallback(context, this).also {
    it.onMovedListener = { fromPos, toPos -> swapScenesInternally(fromPos, toPos) }
    it.onMoveFinishedListener = {
      movementFinishedCallback(_sections.map { section -> section.scenes }.flatten())
    }
  }
  private val itemTouchHelper = ItemTouchHelper(callback)

  var leftButtonClickCallback: (sceneId: Int) -> Unit = { _: Int -> }
  var rightButtonClickCallback: (sceneId: Int) -> Unit = { _: Int -> }
  var movementFinishedCallback: (scenes: List<Scene>) -> Unit = { }
  var reloadCallback: () -> Unit = { }
  var toggleLocationCallback: (location: Location) -> Unit = { }

  override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
    super.onAttachedToRecyclerView(recyclerView)

    itemTouchHelper.attachToRecyclerView(recyclerView)
    callback.setup(recyclerView)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val inflater = LayoutInflater.from(parent.context)
    return when (viewType) {
      R.layout.scene_list_item -> {
        val binding = SceneListItemBinding.inflate(inflater, parent, false)
        val holder = SceneListItemViewHolder(binding)
        holder
      }
      R.layout.location_list_item -> {
        val binding = LocationListItemBinding.inflate(inflater, parent, false)
        binding.tvSectionCaption.typeface = SuplaApp.getApp().typefaceQuicksandRegular
        LocationListItemViewHolder(binding)
      }
      else -> throw IllegalArgumentException("unsupported view type $viewType")
    }
  }

  override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
    when (vh) {
      is SceneListItemViewHolder -> {
        val scene = getScene(pos)
        vh.scene = scene
        vh.binding.sceneLayout.tag = scene.sceneId
        vh.binding.sceneLayout.setSceneListener(this)
        vh.binding.sceneLayout.setScene(getScene(pos))
        vh.binding.sceneLayout.setViewHolderProvider { vh }
      }
      is LocationListItemViewHolder -> {
        val location = getLocation(pos)
        vh.binding.container.setOnClickListener {
          callback.closeWhenSwiped(withAnimation = false)
          toggleLocationCallback(location)
        }
        vh.binding.container.setOnLongClickListener { changeLocationCaption(location.locationId) }
        vh.binding.tvSectionCaption.text = location.caption
        vh.binding.ivSectionCollapsed.visibility = if ((location.collapsed and 0x4) > 0) {
          VISIBLE
        } else {
          GONE
        }
      }
    }
  }

  override fun getItemViewType(pos: Int): Int {
    return _vTypes[pos]
  }

  override fun getItemCount(): Int {
    return if (_sections.isEmpty()) {
      0
    } else {
      _sections.map { it.scenes.count() + 1 }.reduce { a, v -> a + v }
    }
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  fun setScenes(scenes: List<Scene>) {
    val secs = mutableListOf<Section>()
    val vTypes = mutableListOf<Int>()
    val paths = mutableListOf<Path>()

    var loc: Location? = null
    var locScenes: MutableList<Scene> = mutableListOf()
    var i = 0
    var lc = -1
    while (i < scenes.count()) {
      if (loc == null) {
        loc = channelRepository.getLocation(scenes[i].locationId)
        vTypes.add(R.layout.location_list_item)
        paths.add(Path(++lc))
      }

      if (loc!!.collapsed and 0x4 == 0) {
        paths.add(Path(lc, locScenes.count()))
        locScenes.add(scenes[i])
        vTypes.add(R.layout.scene_list_item)
      }
      i++
      if (i == scenes.count() ||
        scenes[i].locationId != loc.locationId
      ) {
        secs.add(Section(loc, locScenes))
        locScenes = mutableListOf()
        loc = null
      }
    }

    val oldPaths = _paths
    val oldSecs = _sections
    _sections = secs
    _vTypes = vTypes
    _paths = paths

    if (_paths.size != oldPaths.size) {
      notifyDataSetChanged()
      return
    }

    var pos = 0
    for (p in _paths) {
      if (oldSecs[p.sectionIdx].scenes.size ==
        _sections[p.sectionIdx].scenes.size
      ) {
        if (p.sceneIdx != null) {
          val a = oldSecs[p.sectionIdx].scenes[p.sceneIdx!!]
          val b = _sections[p.sectionIdx].scenes[p.sceneIdx!!]
          if (a != b) {
            notifyItemChanged(pos)
          }
        } else {
          val oldLocationName = oldSecs[p.sectionIdx].location.caption
          val newLocationName = _sections[p.sectionIdx].location.caption
          if (oldLocationName != newLocationName) {
            notifyItemChanged(pos)
          }
        }
      } else {
        notifyDataSetChanged()
        return
      }

      pos += 1
    }
  }

  private fun getLocation(pos: Int): Location {
    return _sections[_paths[pos].sectionIdx].location
  }

  private fun getScene(pos: Int): Scene {
    val path = _paths[pos]
    return _sections[path.sectionIdx].scenes[path.sceneIdx!!]
  }

  private fun swapScenesInternally(fromPos: Int, toPos: Int) {
    val fromPath = _paths[fromPos]
    val toPath = _paths[toPos]

    val buf = _sections[fromPath.sectionIdx].scenes[fromPath.sceneIdx!!]
    _sections[fromPath.sectionIdx].scenes[fromPath.sceneIdx!!] =
      _sections[toPath.sectionIdx].scenes[toPath.sceneIdx!!]
    _sections[toPath.sectionIdx].scenes[toPath.sceneIdx!!] = buf
  }

  override fun onLeftButtonClick(sceneId: Int) {
    callback.closeWhenSwiped()
    leftButtonClickCallback(sceneId)
  }

  override fun onRightButtonClick(sceneId: Int) {
    callback.closeWhenSwiped()
    rightButtonClickCallback(sceneId)
  }

  override fun onCaptionLongPress(sceneId: Int) {
    SuplaApp.Vibrate(context)
    val editor = SceneCaptionEditor(context)
    editor.captionChangedListener = reloadCallback
    editor.edit(sceneId)
  }

  override fun onLongPress(viewHolder: ViewHolder) {
    SuplaApp.Vibrate(context)
    callback.closeWhenSwiped()
    itemTouchHelper.startDrag(viewHolder)
  }

  private fun changeLocationCaption(locationId: Int): Boolean {
    SuplaApp.Vibrate(context)
    val editor = LocationCaptionEditor(context)
    editor.captionChangedListener = reloadCallback
    editor.edit(locationId)

    return true
  }

  inner class Section(
    var location: Location,
    var scenes: MutableList<Scene> = mutableListOf()
  )

  data class Path(val sectionIdx: Int, var sceneIdx: Int? = null)

  inner class SceneListItemViewHolder(val binding: SceneListItemBinding) :
    ViewHolder(binding.root) {
    var scene: Scene? = null
  }

  inner class LocationListItemViewHolder(val binding: LocationListItemBinding) :
    ViewHolder(binding.root)
}
