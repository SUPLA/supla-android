package org.supla.android.features.details.switchdetail.general
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.features.nfc.call.screens.ViewModelHostBase
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.ui.dialogs.AlertDialog

@Composable
fun SwitchGeneralScreen(
  item: ItemBundle,
  viewModel: SwitchGeneralViewModel = hiltViewModel(),
  stateDialogViewModel: StateDialogViewModel = hiltViewModel(),
  captionChangeViewModel: CaptionChangeViewModel = hiltViewModel()
) {
  ViewModelHost(
    viewModel = viewModel,
    onCreate = { viewModel.onViewCreated(item.remoteId) },
    onResume = { viewModel.loadData(item.remoteId, item.itemType) }
  ) { state ->
    viewModel.View(
      state = state,
      onInfoClick = { stateDialogViewModel.showDialog(it.channelId) },
      onCaptionLongPress = { captionChangeViewModel.showChannelDialog(it.channelId, it.profileId, it.userCaption) }
    )

    if (state.showOvercurrentDialog) {
      AlertDialog(
        title = stringResource(android.R.string.dialog_alert_title),
        message = stringResource(R.string.overcurrent_question),
        positiveButtonTitle = stringResource(R.string.yes),
        negativeButtonTitle = stringResource(R.string.no),
        onPositiveClick = { viewModel.forceTurnOn(item.remoteId, item.itemType) },
        onNegativeClick = viewModel::hideOvercurrentDialog
      )
    }
  }

  ViewModelHostBase(captionChangeViewModel) {
    captionChangeViewModel.View(it)
  }

  ViewModelHostBase(stateDialogViewModel) {
    stateDialogViewModel.View(it)
  }
}
