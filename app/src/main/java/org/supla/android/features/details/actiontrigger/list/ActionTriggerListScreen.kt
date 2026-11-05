package org.supla.android.features.details.actiontrigger.list
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

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.main.LocalNavigator
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.ViewModelHost
import org.supla.android.main.ViewModelHostBase

@Composable
fun ActionTriggerListScreen(
  item: ItemBundle,
  viewModel: ActionTriggerListViewModel = hiltViewModel(),
  captionChangeViewModel: CaptionChangeViewModel = hiltViewModel(),
  stateDialogViewModel: StateDialogViewModel = hiltViewModel()
) {
  val navigator = LocalNavigator.current
  ViewModelHost(
    viewModel = viewModel,
    onCreate = { viewModel.observeData(item.remoteId) },
    eventHandler = { handleEvent(it, navigator, captionChangeViewModel, stateDialogViewModel) }
  ) { state ->
    viewModel.View(state)
  }

  ViewModelHostBase(captionChangeViewModel) {
    captionChangeViewModel.View(it)
  }

  ViewModelHostBase(stateDialogViewModel) {
    stateDialogViewModel.View(it)
  }
}

private fun handleEvent(
  event: ActionTriggerListViewEvent,
  navigator: MainComposeNavigator?,
  captionChangeViewModel: CaptionChangeViewModel,
  stateDialogViewModel: StateDialogViewModel
) {
  when (event) {
    is ActionTriggerListViewEvent.ChangeCaption ->
      captionChangeViewModel.showChannelDialog(event.data.channelId, event.data.profileId, event.data.userCaption)
    is ActionTriggerListViewEvent.ShowInfo -> stateDialogViewModel.showDialog(event.data.channelId)
    is ActionTriggerListViewEvent.OpenDetails ->
      navigator?.navigateTo(MainRoute.StandardDetail(event.bundle, event.pages))
  }
}
