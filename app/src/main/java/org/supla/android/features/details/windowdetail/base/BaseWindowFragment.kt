package org.supla.android.features.details.windowdetail.base
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
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.features.details.detailbase.standarddetail.ItemBundle
import org.supla.android.features.details.windowdetail.base.ui.WindowView
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.core.shared.infrastructure.messaging.SuplaClientMessage

abstract class BaseWindowFragment<S : BaseWindowViewModelState> : BaseFragment<S, BaseWindowViewEvent>(R.layout.fragment_compose) {

  abstract override val viewModel: BaseWindowViewModel<S>
  protected abstract val item: ItemBundle

  private val binding by viewBinding(FragmentComposeBinding::bind)

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
          viewModel.AuthorizationDialog(state = it)
        }

        Box {
          WindowView(
            windowState = modelState.windowState,
            viewState = modelState.viewState
          ) {
            viewModel.handleAction(it, item.remoteId, item.itemType)
          }
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()

    viewModel.loadData(item.remoteId, item.itemType)
  }

  override fun handleEvents(event: BaseWindowViewEvent) {
    when (event) {
      is BaseWindowViewEvent.LoadingError -> {
        Toast.makeText(requireContext(), R.string.channel_loading_error, Toast.LENGTH_LONG).show()
        activity?.supportFragmentManager?.popBackStack()
      }
    }
  }

  override fun handleViewState(state: S) {
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
  }
}
