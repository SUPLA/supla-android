package org.supla.android.features.scenelist

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.Location
import org.supla.android.db.Scene
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class SceneListViewModel @Inject constructor(
  private val sceneRepository: SceneRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<SceneListViewState, SceneListViewEvent>(SceneListViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

  fun loadScenes() {
    sceneRepository.getAllProfileScenes()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(scenes = it) } }
      )
      .disposeBySelf()
  }

  fun onSceneOrderUpdate(scenes: List<Scene>) {
    TODO("Not yet implemented")
  }

  fun reload() {
    TODO("Not yet implemented")
  }

  fun toggleLocationCollapsed(location: Location) {
    TODO("Not yet implemented")
  }
}

sealed class SceneListViewEvent : ViewEvent

data class SceneListViewState(
  override val loading: Boolean = false,
  val scenes: List<Scene> = emptyList()
) : ViewState(loading)