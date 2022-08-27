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
import android.view.View
import android.view.ViewGroup
import android.view.DragEvent
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.databinding.Observable
import org.supla.android.data.source.local.LocationDao
import org.supla.android.db.Scene
import org.supla.android.db.Location
import org.supla.android.databinding.SceneListItemBinding
import org.supla.android.databinding.LocationListItemBinding
import org.supla.android.Trace
import org.supla.android.R

class ScenesAdapter(private val scenesVM: ScenesViewModel,
                    private val locationDao: LocationDao,
                    private val sceneController: SceneController): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    inner class Section(var location: Location,
                        var scenes: List<Scene> = emptyList())

    data class Path(val sectionIdx: Int, var sceneIdx: Int? = null)

    private var _sections: List<Section> = emptyList()
    private var _vTypes: List<Int> = emptyList()
    private var _paths: List<Path> = emptyList()

    private val _scenesObserver: Observer<List<Scene>> = Observer {
        setScenes(it)
    }

    private val _reorderingCallback = ScenesReorderingCallback()
    private val _touchHelper = ItemTouchHelper(_reorderingCallback)

    private val TAG = "supla"


    override fun onAttachedToRecyclerView(v: RecyclerView) {
        super.onAttachedToRecyclerView(v)
        
        scenesVM.scenes.observeForever(_scenesObserver)
        _touchHelper.attachToRecyclerView(v)
    }

    override fun onDetachedFromRecyclerView(v: RecyclerView) {
        scenesVM.scenes.removeObserver(_scenesObserver)

        super.onDetachedFromRecyclerView(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            R.layout.scene_list_item -> {
                val binding = SceneListItemBinding.inflate(inflater, parent, false)
                SceneListItemViewHolder(binding)
            }
            R.layout.location_list_item -> LocationListItemViewHolder(LocationListItemBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("unsupported view type $viewType")
        }
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder,
                                  pos: Int) {
        when(vh) {
            is SceneListItemViewHolder -> {
                vh.binding.viewModel = SceneListItemViewModel(getScene(pos), sceneController)
                vh.itemView.setOnLongClickListener({ v ->
                   Trace.d(TAG, "gonna trigger drag of $v")
               _touchHelper.startDrag(vh)
               true
                    })

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
        val ic =  _sections.map { it.scenes.count() + 1 }.reduce { a, v -> a + v }
        return ic
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun setScenes(scenes: List<Scene>) {
        val secs = mutableListOf<Section>()
        val vTypes = mutableListOf<Int>()
        val paths = mutableListOf<Path>()
        
        var loc: Location? = null
        var locScenes: MutableList<Scene> = mutableListOf<Scene>()
        var i = 0
        var lc = -1
        while(i < scenes.count()) {
            Trace.d(TAG, "looking at scene ${scenes[i]}")
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
                locScenes = mutableListOf<Scene>()
                loc = null
            }
        }

        _sections = secs
        _vTypes = vTypes
        _paths = paths

        notifyDataSetChanged()
    }

    private fun getLocation(pos: Int): Location {
        return _sections[_paths[pos].sectionIdx].location
    }

    private fun getScene(pos: Int): Scene {
        val path = _paths[pos]
        return _sections[path.sectionIdx].scenes[path.sceneIdx!!]
    }

    inner class SceneListItemViewHolder(val binding: SceneListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
    inner class LocationListItemViewHolder(val binding: LocationListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ScenesReorderingCallback:  ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        

        override fun onMove(recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder): Boolean {
            if(viewHolder is SceneListItemViewHolder &&
               target is SceneListItemViewHolder) {
                Trace.d(TAG, "gonna drop ${viewHolder.binding.viewModel!!.scene} to ${target.binding.viewModel!!.scene}")
                return true
            } else {
                // drop target type not compatible
                return false
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, 
                              direction: Int) {
            // no-op
        }
    }
}
