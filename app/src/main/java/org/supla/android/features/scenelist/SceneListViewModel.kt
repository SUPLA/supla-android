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

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.Location
import org.supla.android.db.Scene
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.ListItem
import javax.inject.Inject

@HiltViewModel
class SceneListViewModel @Inject constructor(
  private val sceneRepository: SceneRepository,
  private val channelRepository: ChannelRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<SceneListViewState, SceneListViewEvent>(SceneListViewState(), schedulers) {

  override fun loadingState(isLoading: Boolean) = currentState().copy(loading = isLoading)

  fun loadScenes() {
    sceneRepository.getAllProfileScenes()
      .map(this::sceneToListItem)
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(scenes = it) } }
      )
      .disposeBySelf()
  }

  private fun sceneToListItem(scenes: List<Scene>): List<ListItem> {
    val result = mutableListOf<ListItem>()

    var location: Location? = null
    for (scene in scenes) {
      if (location == null || location.locationId != scene.locationId) {
        location = channelRepository.getLocation(scene.locationId)
        result.add(ListItem.LocationItem(location))
      }

      if (location?.isCollapsed() == true) {
        continue
      }

      result.add(ListItem.SceneItem(scene))
    }

    return result
  }

  fun onSceneOrderUpdate(scenes: List<Scene>) {
    Completable.fromRunnable {
      var sceneIndex = 0
      for (s in scenes) {
        s.sortOrder = sceneIndex++
        sceneRepository.updateScene(s)
      }
    }
      .attachSilent()
      .subscribeBy()
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: Location) {
    if (location.collapsed and 0x8 > 0) {
      location.collapsed = (location.collapsed and 0x8.inv())
    } else {
      location.collapsed = (location.collapsed or 0x8)
    }

    Completable.fromRunnable { channelRepository.updateLocation(location) }
      .andThen(sceneRepository.getAllProfileScenes())
      .map(this::sceneToListItem)
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(scenes = it) } }
      )
      .disposeBySelf()
  }

  private fun Location.isCollapsed(): Boolean = (collapsed and 0x8 > 0)
}

sealed class SceneListViewEvent : ViewEvent

data class SceneListViewState(
  override val loading: Boolean = false,
  val scenes: List<ListItem> = emptyList()
) : ViewState(loading)