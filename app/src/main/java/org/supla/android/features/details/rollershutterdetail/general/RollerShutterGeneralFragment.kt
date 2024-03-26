package org.supla.android.features.details.rollershutterdetail.general
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.dialogs.AuthorizationDialog

private const val ARG_ITEM_BUNDLE = "ARG_ITEM_BUNDLE"

@AndroidEntryPoint
class RollerShutterGeneralFragment :
  BaseFragment<RollerShutterGeneralModelState, RollerShutterGeneralViewEvent>(R.layout.fragment_compose) {

  override val viewModel: RollerShutterGeneralViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  private val item: ItemBundle by lazy { requireSerializable(ARG_ITEM_BUNDLE, ItemBundle::class.java) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.composeContent.setContent {
      val modelState by viewModel.getViewState().collectAsState()
      SuplaTheme {
        if (modelState.showCalibrationDialog) {
          AlertDialog(
            title = stringResource(id = R.string.roller_shutter_calibration),
            message = stringResource(id = R.string.roller_shutter_start_calibration_message),
            positiveButtonTitle = stringResource(id = R.string.yes),
            negativeButtonTitle = stringResource(id = R.string.no),
            onPositiveClick = { viewModel.startCalibration() },
            onNegativeClick = { viewModel.cancelCalibration() }
          )
        }
        modelState.authorizationDialogState?.let {
          AuthorizationDialog(
            dialogState = it,
            onCancel = { viewModel.hideAuthorizationDialog() },
            onAuthorize = viewModel::authorize,
            onStateChange = viewModel::updateAuthorizationState
          )
        }

        RollerShutterGeneralView(
          windowState = modelState.rollerState,
          viewState = modelState.viewState
        ) {
          viewModel.handleAction(it, item.remoteId, item.itemType)
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()

    viewModel.loadData(item.remoteId, item.itemType)
  }

  override fun handleEvents(event: RollerShutterGeneralViewEvent) {
  }

  override fun handleViewState(state: RollerShutterGeneralModelState) {
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    if (message.type == SuplaClientMsg.onDataChanged && (message.channelId == item.remoteId || message.channelGroupId == item.remoteId)) {
      viewModel.loadData(item.remoteId, item.itemType)
    }
  }

  companion object {
    fun bundle(itemBundle: ItemBundle) = bundleOf(
      ARG_ITEM_BUNDLE to itemBundle
    )
  }
}
