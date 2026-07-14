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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.captionchangedialog.CaptionChangeViewEvent
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.main.LocalNavigator
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.ViewModelHostBase
import org.supla.android.main.scaffold.screenUnderTopBarPaddings
import org.supla.android.main.topbar.RegisterTopBarSearch
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.list.ListView
import org.supla.android.ui.views.list.MainListScope
import org.supla.core.shared.extensions.forTrue

interface SceneListScope : MainListScope {
  fun onAddGroupClick()
}

@Composable
fun SceneListScreen(
  viewModel: SceneListViewModel = hiltViewModel(),
  captionChangeViewModel: CaptionChangeViewModel = hiltViewModel()
) {
  val navigator = LocalNavigator.current

  ViewModelHostBase(
    viewModel = viewModel,
    eventHandler = { handleSceneEvents(it, navigator, captionChangeViewModel) }
  ) { state ->
    RegisterTopBarSearch(
      data = viewModel.searchData,
      handler = viewModel::handle
    )

    viewModel.Content(
      state = state,
      dragEnabled = viewModel.searchData.query.isEmpty()
    )
  }

  ViewModelHostBase(
    viewModel = captionChangeViewModel,
    eventHandler = { handleCaptionChangeEvents(it, viewModel) }
  ) {
    captionChangeViewModel.View(it)
  }
}

private fun handleSceneEvents(event: SceneListViewEvent, navigator: MainComposeNavigator?, viewModel: CaptionChangeViewModel) {
  when (event) {
    is SceneListViewEvent.ShowLocationCaptionChangeDialog ->
      viewModel.showLocationDialog(event.remoteId, event.profileId, event.caption)
    is SceneListViewEvent.ShowSceneCaptionChangeDialog ->
      viewModel.showSceneDialog(event.remoteId, event.profileId, event.caption)
    is SceneListViewEvent.NavigateToPrivateCloud -> navigator?.navigateToWeb(event.url)
    SceneListViewEvent.NavigateToSuplaBetaCloud -> navigator?.navigateToBetaCloudExternal()
    SceneListViewEvent.NavigateToSuplaCloud -> navigator?.navigateToCloudExternal()
  }
}

private fun handleCaptionChangeEvents(event: CaptionChangeViewEvent, viewModel: SceneListViewModel) {
  when (event) {
    is CaptionChangeViewEvent.Finish ->
      event.type.isLocation.forTrue { viewModel.loadScenes() }
  }
}

@Composable
private fun SceneListScope.Content(
  state: SceneListViewState,
  dragEnabled: Boolean
) {
  if (state.scenes.isNullOrEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .screenUnderTopBarPaddings()
    ) {
      EmptyContent(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    ListView(
      items = state.scenes,
      dragEnabled = dragEnabled
    )
  }
}

@Composable
private fun SceneListScope.EmptyContent(
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(Distance.small),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    EmptyListInfoView()
    Spacer(modifier = Modifier.height(Distance.small))
    OutlinedButton(
      text = stringResource(R.string.scenes_empty_list_button),
      onClick = { onAddGroupClick() }
    )
  }
}

val previewScope = object : SceneListScope {
  override fun onAddGroupClick() {}
  override fun onLeftButtonClick(remoteId: Int) {}
  override fun onRightButtonClick(remoteId: Int) {}
  override fun moveItems(from: Int, to: Int): Boolean = false
  override fun onDragStarted(remoteId: Int) {}
  override fun onDragStopped(remoteId: Int) {}
  override fun onLocationClick(remoteId: Int) {}
  override fun onItemClick(remoteId: Int) {}
  override fun onTitleLongClick(item: ListItem) {}
  override fun onLocationLongClick(item: ListItem.LocationItem) {}
}

@SuplaPreview
@Composable
private fun PreviewEmpty() {
  SuplaTheme {
    previewScope.Content(
      state = SceneListViewState(),
      dragEnabled = false
    )
  }
}
