package org.supla.android.features.androidauto.add
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
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.ViewModelHost
import org.supla.android.ui.dialogs.AlertDialog
import org.supla.android.ui.extensions.ifTrue

@Composable
fun AddAndroidAutoItemScreen(
  navigator: MainComposeNavigator,
  itemId: Long? = null,
  viewModel: AddAndroidAutoItemViewModel = hiltViewModel()
) {
  ViewModelHost(
    viewModel = viewModel,
    eventHandler = { handleEvent(it, navigator) },
    onCreate = { viewModel.onViewCreated(itemId) }
  ) {
    viewModel.View(it.viewState)

    it.showDeletePopup.ifTrue {
      AlertDialog(
        title = stringResource(R.string.android_auto_delete_title),
        message = stringResource(R.string.android_auto_delete_message),
        positiveButtonTitle = stringResource(R.string.android_auto_delete_confirm),
        negativeButtonTitle = stringResource(R.string.cancel),
        onDismiss = viewModel::onDeleteCanceled,
        onPositiveClick = viewModel::onDeleteConfirmed,
        onNegativeClick = viewModel::onDeleteCanceled
      )
    }
  }
}

private fun handleEvent(event: AddAndroidAutoItemViewEvent, navigator: MainComposeNavigator) {
  when (event) {
    AddAndroidAutoItemViewEvent.Close -> navigator.back()
  }
}
