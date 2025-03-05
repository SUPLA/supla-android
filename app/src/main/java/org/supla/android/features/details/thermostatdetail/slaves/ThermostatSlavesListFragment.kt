package org.supla.android.features.details.thermostatdetail.slaves
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.android.ui.dialogs.CaptionChangeDialog
import org.supla.android.ui.dialogs.state.StateDialog

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class ThermostatSlavesListFragment : BaseComposeFragment<ThermostatSlavesListViewModelState, ThermostatSlavesListViewEvent>() {

  override val viewModel: ThermostatSlavesListViewModel by viewModels()
  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  @Composable
  override fun ComposableContent() {
    val modelState by viewModel.getViewState().collectAsState()

    SuplaTheme {
      modelState.showMessage?.let {
        AlertDialog(
          title = stringResource(id = android.R.string.dialog_alert_title),
          message = it,
          positiveButtonTitle = stringResource(id = R.string.ok),
          negativeButtonTitle = null,
          onPositiveClick = viewModel::closeMessage
        )
      }
      modelState.stateDialogViewState?.let {
        StateDialog(state = it, onDismiss = viewModel::closeStateDialog)
      }
      modelState.captionChangeDialogState?.let {
        viewModel.CaptionChangeDialog(state = it)
      }
      modelState.authorizationDialogState?.let {
        viewModel.AuthorizationDialog(state = it)
      }
      ThermostatSlavesListView(
        state = modelState.viewState,
        onShowMessage = viewModel::showMessage,
        onShowInfo = { viewModel.showStateDialog(it.channelId, it.caption) },
        onCaptionLongPress = { viewModel.changeChannelCaption(it.userCaption, it.channelId, it.profileId) }
      )
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.onCreate(item.remoteId)
  }

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  override fun onStop() {
    super.onStop()
    viewModel.onStop()
  }

  override fun handleEvents(event: ThermostatSlavesListViewEvent) {
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onChannelState -> viewModel.updateStateDialog(message.channelState)
    }
  }

  companion object {
    fun bundle(itemBundle: ItemBundle) = bundleOf(
      ARG_ITEM_BUNDLE to itemBundle
    )
  }
}
