package org.supla.android.features.about
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.R
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.main.MainComposeNavigator

@Composable
fun AboutScreen(
  navigator: MainComposeNavigator,
  viewModel: AboutViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  ViewModelHost(
    viewModel = viewModel,
    eventHandler = { handleEvent(it, context) }
  ) {
    AboutView(
      viewState = it.viewState,
      onSuplaUrlClick = navigator::navigateToSuplaOrgExternal,
      onVersionClick = viewModel::onVersionClick
    )
  }
}

private fun handleEvent(event: AboutViewEvent, context: Context) {
  when (event) {
    AboutViewEvent.ShowDeveloperModeActivated ->
      Toast.makeText(context, R.string.developer_info_activated, Toast.LENGTH_SHORT).show()
  }
}
