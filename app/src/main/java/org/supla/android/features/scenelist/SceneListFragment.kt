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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.core.ui.BaseFragment
import org.supla.android.databinding.FragmentSceneListBinding
import org.supla.android.extensions.toPx
import org.supla.android.extensions.visibleIf
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

@AndroidEntryPoint
class SceneListFragment : BaseFragment<SceneListViewState, SceneListViewEvent>(R.layout.fragment_scene_list) {

  override val viewModel: SceneListViewModel by viewModels()
  private val binding by viewBinding(FragmentSceneListBinding::bind)
  private var scrollDownOnReload = false

  @Inject
  lateinit var adapter: ScenesAdapter

  @Inject
  lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.scenesList.adapter = adapter
    setupAdapter()
    binding.scenesEmptyListButton.setOnClickListener { viewModel.onAddGroupClick() }
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadScenes()
  }

  override fun handleEvents(event: SceneListViewEvent) {
    when (event) {
      is SceneListViewEvent.ReassignAdapter -> {
        binding.scenesList.adapter = null
        binding.scenesList.adapter = adapter
      }
      is SceneListViewEvent.NavigateToPrivateCloud -> navigator.navigateToWebView(event.url)
      is SceneListViewEvent.NavigateToSuplaCloud -> navigator.navigateToCloudExternal()
    }
  }

  override fun handleViewState(state: SceneListViewState) {
    state.scenes?.let { adapter.setItems(it) }

    binding.scenesEmptyListIcon.visibleIf(state.scenes?.isEmpty() == true)
    binding.scenesEmptyListLabel.visibleIf(state.scenes?.isEmpty() == true)
    binding.scenesEmptyListButton.visibleIf(state.scenes?.isEmpty() == true)

    if (scrollDownOnReload) {
      binding.scenesList.smoothScrollBy(0, 50.toPx())
      scrollDownOnReload = false
    }
  }

  private fun setupAdapter() {
    adapter.leftButtonClickCallback = {
      SuplaApp.Vibrate(context)
      SuplaApp.getApp().getSuplaClient()?.stopScene(it)
    }
    adapter.rightButtonClickCallback = {
      SuplaApp.Vibrate(context)
      SuplaApp.getApp().getSuplaClient()?.startScene(it)
    }
    adapter.movementFinishedCallback = { viewModel.onSceneOrderUpdate(it) }
    adapter.reloadCallback = { viewModel.loadScenes() }
    adapter.toggleLocationCallback = { location, scrollDown ->
      viewModel.toggleLocationCollapsed(location)
      scrollDownOnReload = scrollDown
    }
  }
}
