package org.supla.android.features.grouplist
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
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.captionchangedialog.CaptionChangeViewEvent
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.main.LocalNavigator
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.MainRoute.StandardDetail
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

interface GroupListScope : MainListScope {
  fun onAddGroupClick()
}

@Composable
fun GroupListScreen(
  viewModel: GroupListViewModel = hiltViewModel(),
  captionChangeViewModel: CaptionChangeViewModel = hiltViewModel()
) {
  val navigator = LocalNavigator.current

  ViewModelHostBase(
    viewModel = viewModel,
    eventHandler = { handleGroupEvents(it, navigator, captionChangeViewModel) }
  ) { state ->
    RegisterTopBarSearch(
      data = viewModel.searchData,
      handler = viewModel::handle
    )

    viewModel.Content(
      groups = viewModel.list,
      dragEnabled = viewModel.searchData.query.isEmpty()
    )

    state.actionAlertDialogState?.View(
      onPositiveClick = { remoteId, actionId -> viewModel.forceAction(remoteId, actionId) },
      onNegativeClick = viewModel::dismissActionDialog
    )
  }

  ViewModelHostBase(
    viewModel = captionChangeViewModel,
    eventHandler = { handleCaptionChangeEvents(it, viewModel) }
  ) {
    captionChangeViewModel.View(it)
  }
}

private fun handleGroupEvents(event: GroupListViewEvent, navigator: MainComposeNavigator?, viewModel: CaptionChangeViewModel) {
  when (event) {
    is GroupListViewEvent.ShowGroupCaptionChangeDialog -> viewModel.showGroupDialog(event.remoteId, event.profileId, event.caption)
    is GroupListViewEvent.ShowLocationCaptionChangeDialog -> viewModel.showLocationDialog(event.remoteId, event.profileId, event.caption)
    is GroupListViewEvent.NavigateToPrivateCloud -> navigator?.navigateToWeb(event.url)
    GroupListViewEvent.NavigateToSuplaBetaCloud -> navigator?.navigateToBetaCloudExternal()
    GroupListViewEvent.NavigateToSuplaCloud -> navigator?.navigateToCloudExternal()
    is GroupListViewEvent.OpenLegacyDetail -> navigator?.navigateTo(MainRoute.LegacyDetail(event.remoteId, ItemType.GROUP, event.type))
    is GroupListViewEvent.OpenDetail -> navigator?.navigateTo(StandardDetail(event.itemBundle, event.pages))
  }
}

private fun handleCaptionChangeEvents(event: CaptionChangeViewEvent, viewModel: GroupListViewModel) {
  when (event) {
    is CaptionChangeViewEvent.Finish ->
      event.type.isLocation.forTrue { viewModel.loadGroups() }
  }
}

@Composable
private fun GroupListScope.Content(
  groups: List<ListItem>,
  dragEnabled: Boolean
) {
  if (groups.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .screenUnderTopBarPaddings()
    ) {
      EmptyContent(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    ListView(
      items = groups,
      dragEnabled = dragEnabled
    )
  }
}

@Composable
private fun GroupListScope.EmptyContent(
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
      text = stringResource(R.string.groups_empty_list_button),
      onClick = { onAddGroupClick() }
    )
  }
}

val previewScope = object : GroupListScope {
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
      groups = emptyList(),
      dragEnabled = false
    )
  }
}
