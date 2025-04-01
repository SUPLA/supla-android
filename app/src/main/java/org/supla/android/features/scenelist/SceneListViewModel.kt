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

import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.events.UpdateEventsManager
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.android.usecases.scene.CreateProfileScenesListUseCase
import org.supla.android.usecases.scene.UpdateSceneOrderUseCase
import javax.inject.Inject

@HiltViewModel
class SceneListViewModel @Inject constructor(
  private val createProfileScenesListUseCase: CreateProfileScenesListUseCase,
  private val updateSceneOrderUseCase: UpdateSceneOrderUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase,
  updateEventsManager: UpdateEventsManager,
  schedulers: SuplaSchedulers,
  dateProvider: DateProvider,
  preferences: Preferences
) : BaseListViewModel<SceneListViewState, SceneListViewEvent>(
  preferences,
  dateProvider,
  schedulers,
  SceneListViewState(),
  loadActiveProfileUrlUseCase
) {

  override fun sendReassignEvent() = sendEvent(SceneListViewEvent.ReassignAdapter)

  override fun reloadList() = loadScenes()

  init {
    observeUpdates(updateEventsManager.observeScenesUpdate())
  }

  fun loadScenes() {
    createProfileScenesListUseCase()
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(scenes = it) } },
        onError = defaultErrorHandler("loadScenes()")
      )
      .disposeBySelf()
  }

  fun onSceneOrderUpdate(scenes: List<SceneDataEntity>) {
    updateSceneOrderUseCase(scenes)
      .attachSilent()
      .subscribeBy(onError = defaultErrorHandler("onSceneOrderUpdate()"))
      .disposeBySelf()
  }

  fun toggleLocationCollapsed(location: LocationEntity) {
    toggleLocationUseCase(location, CollapsedFlag.SCENE)
      .andThen(createProfileScenesListUseCase())
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(scenes = it) } },
        onError = defaultErrorHandler("toggleLocationCollapsed($location)")
      )
      .disposeBySelf()
  }

  fun onAddGroupClick() {
    loadServerUrl {
      when (it) {
        is CloudUrl.DefaultCloud -> sendEvent(SceneListViewEvent.NavigateToSuplaCloud)
        is CloudUrl.ServerUri -> sendEvent(SceneListViewEvent.NavigateToPrivateCloud(it.url))
      }
    }
  }
}

sealed class SceneListViewEvent : ViewEvent {
  data object ReassignAdapter : SceneListViewEvent()
  data object NavigateToSuplaCloud : SceneListViewEvent()
  data object NavigateToSuplaBetaCloud : SceneListViewEvent()
  data class NavigateToPrivateCloud(val url: Uri) : SceneListViewEvent()
}

data class SceneListViewState(
  val scenes: List<ListItem>? = null
) : ViewState()
