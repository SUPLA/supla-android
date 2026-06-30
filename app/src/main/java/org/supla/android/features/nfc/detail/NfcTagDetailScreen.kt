package org.supla.android.features.nfc.detail
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.MainRoute
import org.supla.android.main.scaffold.BackScaffold

@Composable
fun NfcTagDetailScreen(
  id: Long,
  navigator: MainComposeNavigator,
  viewModel: NfcTagDetailViewModel = hiltViewModel()
) {
  val title = remember(viewModel) { mutableStateOf("") }
  BackScaffold(
    title = title.value,
    navigator = navigator
  ) {
    ViewModelHost(
      viewModel = viewModel,
      eventHandler = { handleEvent(id, it, navigator, title) },
      onCreate = { viewModel.setItemId(id) }
    ) {
      viewModel.View(it)
    }
  }
}

private fun handleEvent(id: Long, event: NfcTagDetailViewEvent, navigator: MainComposeNavigator, title: MutableState<String>) {
  when (event) {
    NfcTagDetailViewEvent.Close -> navigator.back()
    NfcTagDetailViewEvent.EditTag -> navigator.navigateTo(MainRoute.EditNfcTag(id = id))
    NfcTagDetailViewEvent.LockTag -> navigator.navigateTo(MainRoute.LockNfcTag(id = id))
    is NfcTagDetailViewEvent.SetToolbarTitle -> title.value = event.tagName
  }
}
