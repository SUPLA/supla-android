package org.supla.android.features.status
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
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.ViewModelHost
import org.supla.android.main.topbar.SetStatusBarContentColor
import org.supla.android.main.topbar.StatusBarContentColor
import org.supla.android.ui.dialogs.AuthorizationDialog

@Composable
fun StatusScreen(
  navigator: MainComposeNavigator,
  viewModel: StatusViewModel = hiltViewModel()
) {
  ViewModelHost(
    viewModel = viewModel,
    eventHandler = { event ->
      when (event) {
        StatusViewEvent.NavigateToMain -> navigator.replaceTop(MainRoute.List)
        StatusViewEvent.NavigateToProfiles -> navigator.navigateToProfiles()
      }
    }
  ) { viewState ->
    SetStatusBarContentColor(StatusBarContentColor.DARK)

    viewState.authorizationDialogState?.let {
      viewModel.AuthorizationDialog(state = it)
    }
    viewModel.View(viewState)
  }
}
