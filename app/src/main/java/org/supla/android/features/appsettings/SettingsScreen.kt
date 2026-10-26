package org.supla.android.features.appsettings

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

@Composable
fun SettingsScreen(
  navigator: MainComposeNavigator,
  viewModel: SettingsViewModel = hiltViewModel()
) {
  ViewModelHost(
    viewModel = viewModel,
    eventHandler = { handleEvent(it, navigator) },
    onResume = viewModel::loadSettings
  ) {
    viewModel.View(it)
  }
}

private fun handleEvent(event: SettingsViewEvent, navigator: MainComposeNavigator) {
  when (event) {
    SettingsViewEvent.NavigateToLocalizationsOrdering -> navigator.navigateTo(MainRoute.LocationReorder)
    SettingsViewEvent.NavigateToAndroidAuto -> navigator.navigateTo(MainRoute.AndroidAutoItems)
    SettingsViewEvent.NavigateToNfc -> navigator.navigateTo(MainRoute.NfcTagList)
    SettingsViewEvent.NavigateToSettings -> navigator.navigateToSystemSettings()
    is SettingsViewEvent.NavigateToPinSetup -> navigator.navigateTo(MainRoute.PinSetup(event.lockScreenScope))
    is SettingsViewEvent.NavigateToPinVerification -> navigator.navigateTo(MainRoute.Unlock(event.verificationAction))
  }
}
