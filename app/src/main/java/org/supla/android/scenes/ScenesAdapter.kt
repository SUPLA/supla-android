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
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.supla.android.db.Scene
import org.supla.android.databinding.SceneListItemBinding
import org.supla.android.Trace
import org.supla.android.R

class ScenesAdapter(private val scenesVM: ScenesViewModel): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    

    private var scenes: List<Scene> = emptyList()

    init {
    }

    override fun onAttachedToRecyclerView(v: RecyclerView) {
        super.onAttachedToRecyclerView(v)
        val ctx = v.context
        Trace.d("SUPLA", "before check $ctx")
        if(ctx is LifecycleOwner) {
            Trace.d("SUPLA", "observing scenes")
            scenesVM.scenes.observe(ctx) {
                Trace.d("SUPLA", "scenes updated")
                setScenes(it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SceneListItemBinding.inflate(inflater, parent, false)
        binding.viewModel = SceneListItemViewModel()
        return SceneListItemViewHolder(binding)
    }

    override fun onBindViewHolder(vh: RecyclerView.ViewHolder,
                                  pos: Int) {
        if(vh is SceneListItemViewHolder) {
            val vm = SceneListItemViewModel()
            vh.binding.viewModel = vm
        }
    }

    override fun getItemCount(): Int {
        return scenes.map { it.locationId }.distinct().count() +
            scenes.count()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun setScenes(scenes: List<Scene>) {
        this.scenes = scenes
        notifyDataSetChanged()
    }

    inner class SceneListItemViewHolder(val binding: SceneListItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
