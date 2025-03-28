package org.supla.android.features.details.valveDetail.general
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.features.statedialog.handleStateDialogViewEvent
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.ui.dialogs.AlertDialog

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class ValveGeneralDetailFragment : BaseComposeFragment<ValveGeneralDetailViewModeState, ValveGeneralDetailViewEvent>() {

  override val viewModel: ValveGeneralDetailViewModel by viewModels()
  override val helperViewModels: List<BaseViewModel<*, *>>
    get() = listOf(stateDialogViewModel, captionChangeViewModel)

  private val stateDialogViewModel: StateDialogViewModel by viewModels()
  private val captionChangeViewModel: CaptionChangeViewModel by viewModels()

  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.observe(item.remoteId)
  }

  @Composable
  override fun ComposableContent() {
    val modelState by viewModel.getViewState().collectAsState()

    SuplaTheme {
      modelState.dialog?.let { dialog ->
        AlertDialog(
          title = stringResource(id = android.R.string.dialog_alert_title),
          message = stringResource(dialog.messageRes),
          positiveButtonTitle = dialog.positiveButtonRes?.let { stringResource(it) },
          negativeButtonTitle = dialog.negativeButtonRes?.let { stringResource(it) },
          onPositiveClick = { viewModel.forceAction(item.remoteId, dialog.action) },
          onNegativeClick = viewModel::closeErrorDialog
        )
      }
      stateDialogViewModel.View()
      captionChangeViewModel.View()
      ValveGeneralDetailView(
        state = modelState.viewState,
        onOpenClick = { viewModel.onActionClick(item.remoteId, ValveAction.OPEN) },
        onCloseClick = { viewModel.onActionClick(item.remoteId, ValveAction.CLOSE) },
        onInfoClick = { stateDialogViewModel.showDialog(it.channelId) },
        onCaptionLongPress = { captionChangeViewModel.showChannelDialog(it.channelId, it.profileId, it.userCaption) }
      )
    }
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadData(remoteId = item.remoteId)
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    when (message.type) {
      SuplaClientMsg.onChannelState -> stateDialogViewModel.updateStateDialog(message.channelState)
    }
  }

  override fun handleEvents(event: ValveGeneralDetailViewEvent) {
  }

  override fun handleHelperEvents(event: ViewEvent) {
    handleStateDialogViewEvent(event)
  }

  companion object {
    fun bundle(itemBundle: ItemBundle) = bundleOf(
      ARG_ITEM_BUNDLE to itemBundle
    )
  }
}
