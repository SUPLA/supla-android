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
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.data.source.local.LocationDao
import org.supla.android.databinding.LocationListItemBinding
import org.supla.android.databinding.SceneListItemBinding
import org.supla.android.db.Location
import org.supla.android.db.Scene


class ScenesAdapter(private val scenesVM: ScenesViewModel,
                    private val locationDao: LocationDao): RecyclerView.Adapter<ViewHolder>(),
                    SceneLayout.Listener {
    
    inner class Section(var location: Location,
                        var scenes: MutableList<Scene> = mutableListOf())

    data class Path(val sectionIdx: Int, var sceneIdx: Int? = null)

    private var _sections: List<Section> = emptyList()
    private var _vTypes: List<Int> = emptyList()
    private var _paths: List<Path> = emptyList()
    private lateinit var _context: Context
    private var _parentView: RecyclerView? = null
    private var _slidedScene: SceneLayout? = null

    private val _scenesObserver: Observer<List<Scene>> = Observer {
        setScenes(it)
    }

    private val _touchHelper = ItemTouchHelper(ScenesReorderingCallback())

    override fun onAttachedToRecyclerView(v: RecyclerView) {
        super.onAttachedToRecyclerView(v)
        
        scenesVM.scenes.observeForever(_scenesObserver)
        _touchHelper.attachToRecyclerView(v)
        _context = v.context
        _parentView = v
    }



    override fun onDetachedFromRecyclerView(v: RecyclerView) {
        scenesVM.scenes.removeObserver(_scenesObserver)
        _parentView = null
        super.onDetachedFromRecyclerView(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            R.layout.scene_list_item -> {
                val binding = SceneListItemBinding.inflate(inflater, parent, false)
                val holder = SceneListItemViewHolder(binding)
                holder
            }
            R.layout.location_list_item -> {
                val binding = LocationListItemBinding.inflate(inflater, parent, false)
                binding.tvSectionCaption.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular())
                LocationListItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("unsupported view type $viewType")
        }
    }

    override fun onBindViewHolder(vh: ViewHolder, pos: Int) {
        when(vh) {
            is SceneListItemViewHolder -> {
                val scene = getScene(pos)
                vh.scene = scene
                vh.binding.sceneLayout.tag = scene.sceneId
                vh.binding.sceneLayout.setSceneListener(this)
                vh.binding.sceneLayout.setScene(getScene(pos))
                vh.binding.sceneLayout.setViewHolderProvider { vh }
            }
            is LocationListItemViewHolder -> {
                val vm = LocationListItemViewModel(locationDao, getLocation(pos))
                vm.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
                                                    override fun onPropertyChanged(sender: Observable, pid:Int) {
                                                        scenesVM.onLocationStateChanged()
                                                    }
                })
                vh.binding.viewModel = vm
            }
        }
    }

    override fun getItemViewType(pos: Int): Int {
        return _vTypes[pos]
    }

    override fun getItemCount(): Int {
        return if(_sections.isEmpty()) {
            0
        } else {
            _sections.map { it.scenes.count() + 1 }.reduce { a, v -> a + v }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun setScenes(scenes: List<Scene>) {
        val secs = mutableListOf<Section>()
        val vTypes = mutableListOf<Int>()
        val paths = mutableListOf<Path>()
        
        var loc: Location? = null
        var locScenes: MutableList<Scene> = mutableListOf()
        var i = 0
        var lc = -1
        while(i < scenes.count()) {
            if(loc == null) {
                loc = locationDao.getLocation(scenes[i].locationId)
                vTypes.add(R.layout.location_list_item)
                paths.add(Path(++lc))
            }

            if(loc!!.getCollapsed() and 0x4 == 0) {
                paths.add(Path(lc, locScenes.count()))
                locScenes.add(scenes[i])
                vTypes.add(R.layout.scene_list_item)
            }
            i++
            if(i == scenes.count() ||
               scenes[i].locationId != loc.locationId) {
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

        if(_paths.size != oldPaths.size) {
            notifyDataSetChanged()
        } else {
            var pos = 0
            for(p in _paths) {
                if(oldSecs[p.sectionIdx].scenes.size ==
                       _sections[p.sectionIdx].scenes.size) {
                    if(p.sceneIdx != null) {
                        val a = oldSecs[p.sectionIdx].scenes[p.sceneIdx!!]
                        val b = _sections[p.sectionIdx].scenes[p.sceneIdx!!]
                        if(a != b) {
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
    }

    private fun getLocation(pos: Int): Location {
        return _sections[_paths[pos].sectionIdx].location
    }

    private fun getScene(pos: Int): Scene {
        val path = _paths[pos]
        return _sections[path.sectionIdx].scenes[path.sceneIdx!!]
    }

    private fun swapScenes(src: Scene, dst: Scene) {
        var offset = 1
        for(sec in _sections) {
            var si:Int? = null
            var di:Int? = null
            for(i in 0.until(sec.scenes.count())) {
                if(sec.scenes[i].sceneId == src.sceneId) {
                    si = i
                }

                if(sec.scenes[i].sceneId == dst.sceneId) {
                    di = i
                }

                if(si != null && di != null) break
            }

            if(si != null && di != null) {
                var delta = 0
                if(si > di) {
                    delta = -1
                } else if(si < di) {
                    delta = 1
                }
                var i = si

                while(i != di) {
                    val tmp = sec.scenes[i]
                    sec.scenes[i] = sec.scenes[i + delta]
                    sec.scenes[i + delta] = tmp
                    notifyItemMoved(offset + i, offset + i + delta)
                    i += delta
                }
                return
            }
            offset += sec.scenes.count() + 1
        }
    }

    inner class SceneListItemViewHolder(val binding: SceneListItemBinding) :
        ViewHolder(binding.root) {
            var scene: Scene? = null
    }

    inner class LocationListItemViewHolder(val binding: LocationListItemBinding) :
        ViewHolder(binding.root)

    inner class ScenesReorderingCallback:  ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        override fun getMovementFlags(recyclerView: RecyclerView,
                                      viewHolder: ViewHolder): Int {
            if((_slidedScene?.Slided() ?: 0) != 0) {
                return 0
            }
            return super.getMovementFlags(recyclerView, viewHolder)
        }

        override fun onMove(recyclerView: RecyclerView,
                            viewHolder: ViewHolder,
                            target: ViewHolder): Boolean {
            if (viewHolder !is SceneListItemViewHolder || target !is SceneListItemViewHolder) {
                return false;
            }

            val sourceScene = viewHolder.scene!!
            val destinationScene = target.scene!!
            if (sourceScene.locationId != destinationScene.locationId) {
                return false
            }

            swapScenes(sourceScene, destinationScene)
            return true
        }

        override fun onSwiped(viewHolder: ViewHolder,
                              direction: Int) {
            scenesVM.onSceneOrderUpdate(_sections.map { it.scenes }.flatten())
        }

        override fun isLongPressDragEnabled(): Boolean {
            return false
        }
    }

    override fun onLeftButtonClick(sl: SceneLayout) {
        SuplaApp.getApp().suplaClient.stopScene(sl.tag as Int)
    }
    override fun onRightButtonClick(sl: SceneLayout) {
        SuplaApp.getApp().suplaClient.startScene(sl.tag as Int)
    }
    
    override fun onMove(sl: SceneLayout) {
        if(sl != _slidedScene) {
            _slidedScene?.AnimateToRestingPosition(true)
        }
    }

    override fun onButtonSlide(sl: SceneLayout) {
        _slidedScene?.AnimateToRestingPosition(true)
        _slidedScene = sl
    }

    override fun onCaptionLongPress(sl: SceneLayout) {
        val sceneId = sl.tag as Int
        SuplaApp.Vibrate(_context)
        val editor = SceneCaptionEditor(_context)
        editor.listener = object: SceneCaptionEditor.Listener {
            override fun onCaptionChange() {
                scenesVM.reload()
            }
        }
        editor.edit(sceneId)
    }

    override fun onLongPress(viewHolder: ViewHolder) {
        SuplaApp.Vibrate(_context)
        _touchHelper.startDrag(viewHolder)
    }
}
