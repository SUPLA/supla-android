package org.supla.android.features.scenelist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.SuplaApp
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.databinding.FragmentSceneListBinding
import org.supla.android.scenes.ScenesAdapter
import javax.inject.Inject

@AndroidEntryPoint
class SceneListFragment : BaseFragment<SceneListViewState, SceneListViewEvent>(R.layout.fragment_scene_list) {

  private val viewModel: SceneListViewModel by viewModels()
  private val binding by viewBinding(FragmentSceneListBinding::bind)

  @Inject
  lateinit var adapter: ScenesAdapter


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.scenesList.adapter = adapter
    setupAdapter()
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadScenes()
  }

  override fun getViewModel(): BaseViewModel<SceneListViewState, SceneListViewEvent> = viewModel

  override fun handleEvents(event: SceneListViewEvent) {
  }

  override fun handleViewState(state: SceneListViewState) {
    adapter.setScenes(state.scenes)
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
    adapter.reloadCallback = { viewModel.reload() }
    adapter.toggleLocationCallback = { viewModel.toggleLocationCollapsed(it) }
  }
}