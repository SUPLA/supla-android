package org.supla.android.features.details.containerdetail.general
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

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.features.statedialog.handleStateDialogViewEvent
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage

@AndroidEntryPoint
class ContainerGeneralDetailFragment : BaseComposeFragment<ContainerGeneralDetailViewModeState, ContainerGeneralDetailViewEvent>() {

  override val viewModel: ContainerGeneralDetailViewModel by viewModels()
  override val helperViewModels: List<BaseViewModel<*, *>>
    get() = listOf(stateDialogViewModel, captionChangeViewModel)

  private val stateDialogViewModel: StateDialogViewModel by viewModels()
  private val captionChangeViewModel: CaptionChangeViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.observe(item.remoteId)
  }

  @Composable
  override fun ComposableContent(modelState: ContainerGeneralDetailViewModeState) {
    SuplaTheme {
      viewModel.View(
        state = modelState.viewState,
        showStateDialog = stateDialogViewModel::showDialog,
        showCaptionChangeDialog = captionChangeViewModel::showChannelDialog
      )

      stateDialogViewModel.View()
      captionChangeViewModel.View()

      modelState.authorizationDialogState?.let {
        viewModel.AuthorizationDialog(state = it)
      }
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadData(remoteId = item.remoteId)
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelState)?.let { stateDialogViewModel.updateStateDialog(it.channelState) }
  }

  override fun handleEvents(event: ContainerGeneralDetailViewEvent) {
  }

  override fun handleHelperEvents(event: ViewEvent) {
    handleStateDialogViewEvent(event)
  }
}
