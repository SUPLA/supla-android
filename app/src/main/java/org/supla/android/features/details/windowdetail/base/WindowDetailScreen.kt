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

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.features.details.detailbase.base.ItemBundle
import org.supla.android.features.details.windowdetail.base.ui.WindowView
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.main.MainComposeNavigator
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.dialogs.AuthorizationDialog
import org.supla.android.ui.extensions.ifTrue

@Composable
fun <S : BaseWindowViewModelState> WindowDetailScreen(
  item: ItemBundle,
  navigator: MainComposeNavigator,
  viewModel: BaseWindowViewModel<S>,
  onCreate: () -> Unit = {},
  onResume: () -> Unit = {}
) {
  val context = LocalContext.current
  ViewModelHost(
    viewModel = viewModel,
    onCreate = {
      viewModel.observeData(item.remoteId, item.itemType)
      onCreate()
    },
    onResume = onResume,
    eventHandler = { handleEvent(it, context, navigator) }
  ) { state ->
    WindowView(
      windowState = state.windowState,
      viewState = state.viewState,
      onAction = { viewModel.handleAction(it, item.remoteId, item.itemType) }
    )

    state.showCalibrationDialog.ifTrue {
      AlertDialog(
        title = stringResource(id = R.string.roller_shutter_calibration),
        message = stringResource(id = R.string.roller_shutter_start_calibration_message),
        positiveButtonTitle = stringResource(id = R.string.yes),
        negativeButtonTitle = stringResource(id = R.string.no),
        onPositiveClick = { viewModel.startCalibration() },
        onNegativeClick = { viewModel.cancelCalibration() }
      )
    }

    state.authorizationDialogState?.let {
      viewModel.AuthorizationDialog(state = it)
    }
  }
}

private fun handleEvent(event: BaseWindowViewEvent, context: Context, navigator: MainComposeNavigator) {
  when (event) {
    BaseWindowViewEvent.LoadingError -> {
      Toast.makeText(context, R.string.channel_loading_error, Toast.LENGTH_LONG).show()
      navigator.back()
    }
  }
}
