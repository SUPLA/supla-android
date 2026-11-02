package org.supla.android.features.notificationslog
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

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.ViewModelHost
import org.supla.android.main.snackbar.LocalSnackbarController
import org.supla.android.main.topbar.LocalTopBarController
import org.supla.android.main.topbar.TopBarEvent
import org.supla.android.main.topbar.TopBarIcon
import org.supla.android.main.topbar.TopBarSearchState
import org.supla.android.main.topbar.TopBarState
import org.supla.android.main.topbar.topBarAction

@Composable
fun NotificationsLogScreen(
  navigator: MainComposeNavigator,
  viewModel: NotificationsLogViewModel = hiltViewModel()
) {
  val topBarController = LocalTopBarController.current
  val snackbarController = LocalSnackbarController.current
  val scope = rememberCoroutineScope()

  val snackbarMessage = stringResource(R.string.notification_deleted)
  val snackbarActionLabel = stringResource(R.string.cancel)

  BackHandler {
    if (viewModel.searchData.query.isNotEmpty()) {
      topBarController.updateSearchValue("")
    } else if (viewModel.searchData.visible) {
      topBarController.setSearchVisible(false)
    } else {
      navigator.back()
    }
  }

  ViewModelHost(
    viewModel = viewModel,
    topBarState = TopBarState(
      search = TopBarSearchState(
        data = viewModel.searchData,
        observer = viewModel::handle
      ),
      action = topBarAction {
        icon = TopBarIcon.NotificationsDeletion
        handle<TopBarEvent.DeleteAll> { viewModel.askDeleteAll() }
        handle<TopBarEvent.DeleteLastMonth> { viewModel.askDeleteOlderThanMonth() }
      }
    ),
    eventHandler = { event ->
      when (event) {
        is NotificationsLogViewEvent.ShowDeleteNotification ->
          scope.launch {
            val result = snackbarController.state.showSnackbar(
              message = snackbarMessage,
              actionLabel = snackbarActionLabel,
              duration = SnackbarDuration.Long
            )

            when (result) {
              SnackbarResult.Dismissed -> {} // nothing to do
              SnackbarResult.ActionPerformed -> viewModel.cancelDeletion(event.id)
            }
          }
      }
    }
  ) { state ->
    viewModel.View(state)
  }
}
