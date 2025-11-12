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

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.features.captionchangedialog.CaptionChangeViewModel
import org.supla.android.features.captionchangedialog.View
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.features.statedialog.StateDialogViewModel
import org.supla.android.features.statedialog.View
import org.supla.android.features.statedialog.handleStateDialogViewEvent
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage
import javax.inject.Inject

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class SwitchGeneralFragment : BaseComposeFragment<SwitchGeneralViewState, SwitchGeneralViewEvent>() {

  override val viewModel: SwitchGeneralViewModel by viewModels()
  override val helperViewModels: List<BaseViewModel<*, *>>
    get() = listOf(stateDialogViewModel, captionChangeViewModel)

  private val stateDialogViewModel: StateDialogViewModel by viewModels()
  private val captionChangeViewModel: CaptionChangeViewModel by viewModels()

  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  @Inject
  lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Composable
  override fun ComposableContent(modelState: SwitchGeneralViewState) {
    SuplaTheme {
      viewModel.View(
        state = modelState,
        onInfoClick = { stateDialogViewModel.showDialog(it.channelId) },
        onCaptionLongPress = { captionChangeViewModel.showChannelDialog(it.channelId, it.profileId, it.userCaption) }
      )

      stateDialogViewModel.View()
      captionChangeViewModel.View()

      if (modelState.showOvercurrentDialog) {
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
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.onViewCreated(item.remoteId)
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadData(item.remoteId, item.itemType)
  }

  override fun handleEvents(event: SwitchGeneralViewEvent) {
  }

  override fun onSuplaMessage(message: SuplaClientMessage) {
    (message as? SuplaClientMessage.ChannelDataChanged)?.let {
      if (it.channelId == item.remoteId && item.itemType == ItemType.CHANNEL) {
        viewModel.loadData(item.remoteId, item.itemType)
      }
    }
    (message as? SuplaClientMessage.GroupDataChanged)?.let {
      if (it.groupId == item.remoteId && item.itemType == ItemType.GROUP) {
        viewModel.loadData(item.remoteId, item.itemType)
      }
    }
    (message as? SuplaClientMessage.ChannelState)?.let {
      stateDialogViewModel.updateStateDialog(it.channelState)
    }
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
