package org.supla.android.cfg

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

import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider
import org.supla.android.databinding.FragmentLocationReorderBinding
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.listview.draganddrop.ListViewDragListener

class LocationOrderingFragment: Fragment(), HasDefaultViewModelProviderFactory {
 
    private lateinit var binding: FragmentLocationReorderBinding
    private val viewModel: LocationReorderViewModel by viewModels()
    private lateinit var adapter: LocationReorderAdapter
    private var dragStartPos: Int? = null
    
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,
                                          R.layout.fragment_location_reorder,
                                          container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        
        adapter = LocationReorderAdapter(getActivity()!!,
                                         viewModel.getLocations())
        binding.locationList.adapter = adapter
        binding.locationList.setOnItemLongClickListener {
            parent, view, pos, id ->
                startDrag(view, pos)
        }
        
        val dl = ListViewDragListener(binding.locationList,
                                      { pos -> viewDropped(pos) },
                                      { pos -> viewMoved(pos) })
        binding.locationList.setOnDragListener(dl)

        return binding.root
    }
   
    private fun viewDropped(pos: Int) {
        if(adapter.endDrag(pos)) {
            viewModel.onLocationsUpdate(adapter.orderedLocations)
        }
        dragStartPos = null
    }

    private fun viewMoved(pos: Int) {
        val start = dragStartPos
        if(start != null) {
            adapter.updateDrag(start, pos)
        }
    }

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return LocationReorderViewModelFactory(activity ?: SuplaApp.getApp())
    }

    private fun startDrag(view: View, pos: Int): Boolean {
        dragStartPos = pos
        val shadowBuilder = View.DragShadowBuilder(view)
        view.startDrag(null, shadowBuilder,
                       binding.locationList.getItemAtPosition(pos), 0)
        adapter.enableDrag(pos)
        return true
    }

}
