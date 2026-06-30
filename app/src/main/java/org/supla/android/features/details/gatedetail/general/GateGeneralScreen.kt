package org.supla.android.features.details.gatedetail.general
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
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.features.nfc.call.screens.ViewModelHostBase
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View

@Composable
fun GateGeneralScreen(
  item: ItemBundle,
  viewModel: GateGeneralViewModel = hiltViewModel(),
  captionChangeViewModel: CaptionChangeViewModel = hiltViewModel(),
  stateDialogViewModel: StateDialogViewModel = hiltViewModel()
) {
  ViewModelHost(
    viewModel = viewModel,
    onCreate = { viewModel.observeData(item.remoteId, item.itemType) }
  ) {
    viewModel.View(state = it.viewState)
  }

  ViewModelHostBase(captionChangeViewModel) {
    captionChangeViewModel.View(it)
  }

  ViewModelHostBase(stateDialogViewModel) {
    stateDialogViewModel.View(it)
  }
}
