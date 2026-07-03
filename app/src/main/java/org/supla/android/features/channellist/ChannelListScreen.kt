package org.supla.android.features.channellist
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.captionchangedialog.CaptionChangeViewEvent
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.nfc.call.screens.ViewModelHostBase
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.images.ImageId
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.MainRoute.AddWizard
import org.supla.android.main.MainRoute.DeviceCatalog
import org.supla.android.main.MainRoute.StandardDetail
import org.supla.android.main.scaffold.screenUnderTopBarPaddings
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.lists.ListItem
import org.supla.android.ui.lists.ListOnlineState
import org.supla.android.ui.views.EmptyListInfoView
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.list.ListItemStatus
import org.supla.android.ui.views.list.ListView
import org.supla.android.ui.views.list.MainListScope
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.lists.ListItemIssues
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.LocalizedString

interface ChannelListScope : MainListScope {
  fun onDeviceCatalogClick()
  fun onAddDeviceClick()
}

@Composable
fun ChannelListScreen(
  navigator: MainComposeNavigator,
  modifier: Modifier = Modifier,
  viewModel: ChannelListViewModel = hiltViewModel(),
  captionChangeViewModel: CaptionChangeViewModel = hiltViewModel(),
  stateDialogViewModel: StateDialogViewModel = hiltViewModel()
) {
  ViewModelHostBase(
    viewModel = viewModel,
    eventHandler = { handleChannelEvents(it, navigator, captionChangeViewModel, stateDialogViewModel) }
  ) { state ->
    viewModel.Content(
      state = state,
      modifier = modifier
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

  ViewModelHostBase(
    viewModel = stateDialogViewModel,
  ) {
    stateDialogViewModel.View(it)
  }
}

private fun handleChannelEvents(
  event: ChannelListViewEvent,
  navigator: MainComposeNavigator,
  captionChangeViewModel: CaptionChangeViewModel,
  stateDialogViewModel: StateDialogViewModel
) {
  when (event) {
    is ChannelListViewEvent.ShowChannelCaptionChangeDialog ->
      captionChangeViewModel.showChannelDialog(event.remoteId, event.profileId, event.caption)
    is ChannelListViewEvent.ShowLocationCaptionChangeDialog ->
      captionChangeViewModel.showLocationDialog(event.remoteId, event.profileId, event.caption)
    is ChannelListViewEvent.ShowInfoDialog -> stateDialogViewModel.showDialog(event.remoteId)
    is ChannelListViewEvent.OpenDetail -> navigator.navigateTo(StandardDetail(event.itemBundle, event.pages))
    ChannelListViewEvent.NavigateToAddDevice -> navigator.navigateTo(AddWizard)
    ChannelListViewEvent.NavigateToDeviceCatalog -> navigator.navigateTo(DeviceCatalog)
    is ChannelListViewEvent.OpenLegacyDetail -> navigator.navigateTo(MainRoute.LegacyDetail(event.remoteId, ItemType.CHANNEL, event.type))
  }
}

private fun handleCaptionChangeEvents(event: CaptionChangeViewEvent, viewModel: ChannelListViewModel) {
  when (event) {
    is CaptionChangeViewEvent.Finish ->
      event.type.isLocation.forTrue { viewModel.loadChannels() }
  }
}

@Composable
private fun ChannelListScope.Content(
  state: ChannelListViewState,
  modifier: Modifier = Modifier
) {
  if (state.channels.isNullOrEmpty()) {
    Box(
      modifier = modifier
        .fillMaxHeight()
        .screenUnderTopBarPaddings()
    ) {
      EmptyContent(modifier = Modifier.align(Alignment.Center))
    }
  } else {
    ListView(
      items = state.channels,
      modifier = modifier
    )
  }
}

@Composable
private fun ChannelListScope.EmptyContent(
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
      text = stringResource(R.string.menu_device_catalog),
      onClick = { onDeviceCatalogClick() }
    )
    OutlinedButton(
      text = stringResource(R.string.add_device),
      onClick = { onAddDeviceClick() }
    )
  }
}

val previewScope = object : ChannelListScope {
  override fun onDeviceCatalogClick() {}
  override fun onAddDeviceClick() {}
  override fun onLeftButtonClick(remoteId: Int) {}
  override fun onRightButtonClick(remoteId: Int) {}
  override fun swapItems(from: Int, to: Int): Boolean = false
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
      ChannelListViewState()
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewList() {
  SuplaTheme {
    CompositionLocalProvider(LocalApplicationPreferences provides ApplicationPreferences(LocalContext.current)) {
      previewScope.Content(
        ChannelListViewState(
          channels = listOf(
            ListItem.LocationItem(1, 1L, "Leaving Room", false),
            ListItem.DefaultItem(
              remoteId = 1,
              profileId = 1L,
              function = SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
              locationCaption = "",
              locationId = 1,
              status = ListItemStatus.Channel(ListOnlineState.ONLINE),
              captionProvider = LocalizedString.Constant("Distance sensor"),
              userCaption = "",
              icon = ImageId(R.drawable.fnc_distance),
              value = "123 m",
              issues = ListItemIssues.empty,
              processing = false
            ),
            ListItem.DefaultItem(
              remoteId = 1,
              profileId = 1L,
              function = SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
              locationCaption = "",
              locationId = 1,
              status = ListItemStatus.Channel(ListOnlineState.ONLINE),
              captionProvider = LocalizedString.Constant("Rain sensor"),
              userCaption = "",
              icon = ImageId(R.drawable.fnc_rain),
              value = "123 m",
              issues = ListItemIssues.empty,
              processing = false
            )
          )
        )
      )
    }
  }
}
