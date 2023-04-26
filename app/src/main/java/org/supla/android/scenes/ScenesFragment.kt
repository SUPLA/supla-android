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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.databinding.FragmentScenesBinding
import org.supla.android.db.DbHelper
import javax.inject.Inject

@AndroidEntryPoint
class ScenesFragment : Fragment() {

  private lateinit var binding: FragmentScenesBinding

  private val viewModel: ScenesViewModel by viewModels()

  @Inject
  lateinit var dbHelper: DbHelper

  @Inject
  lateinit var scenesAdapter: ScenesAdapter

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = DataBindingUtil.inflate(
      inflater,
      R.layout.fragment_scenes,
      container,
      false
    )

    setupAdapter()

    binding.lifecycleOwner = requireActivity()
    binding.viewModel = viewModel
    binding.scenesList.adapter = scenesAdapter

    return binding.root
  }

  fun reload() {
    viewModel.cleanup()

    binding.scenesList.adapter = null
    binding.scenesList.adapter = scenesAdapter

    viewModel.reload()
  }

  private fun setupAdapter() {
    scenesAdapter.leftButtonClickCallback = {
      SuplaApp.Vibrate(context)
      SuplaApp.getApp().getSuplaClient()?.stopScene(it)
    }
    scenesAdapter.rightButtonClickCallback = {
      SuplaApp.Vibrate(context)
      SuplaApp.getApp().getSuplaClient()?.startScene(it)
    }
    scenesAdapter.movementFinishedCallback = { viewModel.onSceneOrderUpdate(it) }
    scenesAdapter.reloadCallback = { viewModel.reload() }
    scenesAdapter.toggleLocationCallback = { viewModel.toggleLocationCollapsed(it) }
  }
}
