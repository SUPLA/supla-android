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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.awaitFirst
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.SceneRepository
import org.supla.android.events.UpdateEventsManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.main.topbar.TopBarSearchData
import org.supla.android.main.topbar.TopBarSearchEvent
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.lists.BaseListViewModel
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.sceneItem
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.android.usecases.scene.CreateProfileScenesListUseCase
import org.supla.android.usecases.scene.ReorderScenesUseCase
import javax.inject.Inject

@HiltViewModel
class SceneListViewModel @Inject constructor(
  private val createProfileScenesListUseCase: CreateProfileScenesListUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val reorderScenesUseCase: ReorderScenesUseCase,
  private val toggleLocationUseCase: ToggleLocationUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val sceneRepository: SceneRepository,
  loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase,
  updateEventsManager: UpdateEventsManager,
  vibrationHelper: VibrationHelper,
  schedulers: SuplaSchedulers,
  dateProvider: DateProvider,
) : BaseListViewModel<SceneListViewState, SceneListViewEvent>(
  vibrationHelper,
  dateProvider,
  schedulers,
  SceneListViewState(),
  loadActiveProfileUrlUseCase
),
  SceneListScope {

  var searchData: TopBarSearchData = TopBarSearchData()
    private set

  override fun reloadList() = loadScenes()

  init {
    observeUpdates(updateEventsManager.observeScenesUpdate())

    updateEventsManager.observeAllScenes()
      .attach()
      .flatMapMaybe { sceneRepository.findSceneData(it) }
      .map { it.sceneItem(getSceneIconUseCase) }
      .subscribeBy(
        onNext = { listItem ->
          updateState { it.copy(scenes = it.scenes?.replace(listItem)) }
        }
      )
      .disposeBySelf()
  }

  fun handle(event: TopBarSearchEvent) {
    searchData = searchData.handle(event)
    loadScenes()
  }

  fun loadScenes() {
    createProfileScenesListUseCase(searchData.query)
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(scenes = it) } },
        onError = defaultErrorHandler("loadScenes()")
      )
      .disposeBySelf()
  }

  override fun onAddGroupClick() {
    loadServerUrl {
      when (it) {
        is CloudUrl.DefaultCloud -> sendEvent(SceneListViewEvent.NavigateToSuplaCloud)
        is CloudUrl.ServerUri -> sendEvent(SceneListViewEvent.NavigateToPrivateCloud(it.url))
      }
    }
  }

  override fun onLeftButtonClick(remoteId: Int) {
    executeSimpleActionUseCase.invoke(ActionId.INTERRUPT, SubjectType.SCENE, remoteId)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("onRightButtonClick($remoteId)"))
      .disposeBySelf()
  }

  override fun onRightButtonClick(remoteId: Int) {
    executeSimpleActionUseCase.invoke(ActionId.EXECUTE, SubjectType.SCENE, remoteId)
      .attach()
      .subscribeBy(onError = defaultErrorHandler("onRightButtonClick($remoteId)"))
      .disposeBySelf()
  }

  override fun moveItems(from: Int, to: Int): Boolean {
    var result = false
    updateState {
      val scenes = it.scenes?.toMutableList() ?: return@updateState it
      val firstItem = scenes.getOrNull(from) as? ListItem.SceneItem ?: return@updateState it
      val secondItem = scenes.getOrNull(to) as? ListItem.SceneItem ?: return@updateState it

      if (firstItem.locationCaption == secondItem.locationCaption) {
        result = true
        scenes.add(to, scenes.removeAt(from))
        it.copy(scenes = scenes)
      } else {
        it
      }
    }

    return result
  }

  override fun onDragStopped(remoteId: Int) {
    val scenes = currentState().scenes ?: return
    viewModelScope.launch {
      val reorderedScenes = schedulers.io {
        reorderScenesUseCase(scenes, remoteId)
        createProfileScenesListUseCase().awaitFirst()
      }

      updateState { it.copy(scenes = reorderedScenes) }
    }
  }

  override fun onLocationClick(remoteId: Int) {
    toggleLocationUseCase(remoteId, CollapsedFlag.SCENE)
      .andThen(createProfileScenesListUseCase(searchData.query))
      .attach()
      .subscribeBy(
        onNext = { updateState { state -> state.copy(scenes = it) } },
        onError = defaultErrorHandler("onLocationClick($remoteId)")
      )
      .disposeBySelf()
  }

  override fun onItemClick(remoteId: Int) {} // Not used yet

  override fun onTitleLongClick(item: ListItem) {
    sendEvent(SceneListViewEvent.ShowSceneCaptionChangeDialog(item.remoteId, item.profileId, item.userCaption))
  }

  override fun onLocationLongClick(item: ListItem.LocationItem) {
    sendEvent(SceneListViewEvent.ShowLocationCaptionChangeDialog(item.remoteId, item.profileId, item.userCaption))
  }
}

sealed class SceneListViewEvent : ViewEvent {
  data class ShowLocationCaptionChangeDialog(val remoteId: Int, val profileId: Long, val caption: String) : SceneListViewEvent()
  data class ShowSceneCaptionChangeDialog(val remoteId: Int, val profileId: Long, val caption: String) : SceneListViewEvent()
  data object NavigateToSuplaCloud : SceneListViewEvent()
  data object NavigateToSuplaBetaCloud : SceneListViewEvent()
  data class NavigateToPrivateCloud(val url: Uri) : SceneListViewEvent()
}

data class SceneListViewState(
  val scenes: List<ListItem>? = null
) : ViewState()

private fun List<ListItem>.replace(item: ListItem): List<ListItem> =
  map {
    if (it is ListItem.SceneItem && it.remoteId == item.remoteId) {
      item
    } else {
      it
    }
  }
