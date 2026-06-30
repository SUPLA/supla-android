package org.supla.android.features.nfc.edit
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.supla.android.R
import org.supla.android.features.nfc.call.screens.ViewModelHost
import org.supla.android.features.nfc.findNfcHost
import org.supla.android.features.nfc.shared.edit.EditNfcTagViewEvent
import org.supla.android.main.MainComposeNavigator
import org.supla.android.main.scaffold.BackScaffold
import timber.log.Timber

@Serializable
data class NewItemData(
  val uuid: String,
  val readOnly: Boolean
)

@Composable
fun EditNfcTagScreen(
  id: Long?,
  newItemData: NewItemData?,
  navigator: MainComposeNavigator,
  viewModel: EditNfcTagViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val nfcHost = context.findNfcHost()
  val scope = rememberCoroutineScope()
  val title = remember { mutableStateOf("") }

  BackScaffold(
    title = title.value,
    navigator = navigator
  ) {
    ViewModelHost(
      viewModel = viewModel,
      eventHandler = { handleEvent(it, navigator, title, context) },
      onCreate = { onCreate(id, newItemData, navigator, viewModel) },
      onStart = {
        nfcHost?.enableNfcReader {
          scope.launch(Dispatchers.Main) {
            Toast.makeText(context, R.string.nfc_tag_finish_editing, Toast.LENGTH_LONG).show()
          }
        }
      },
      onStop = { nfcHost?.disableNfcReader() }
    ) {
      viewModel.View(it)
    }
  }
}

private fun handleEvent(event: EditNfcTagViewEvent, navigator: MainComposeNavigator, title: MutableState<String>, context: Context) {
  when (event) {
    EditNfcTagViewEvent.Close -> navigator.back()
    is EditNfcTagViewEvent.SetEditTagTitle -> title.value = context.getString(R.string.edit_nfc_tag_title_with_name, event.name)
    EditNfcTagViewEvent.SetNewTagTitle -> title.value = context.getString(R.string.edit_nfc_new_tag_header)
  }
}

private fun onCreate(id: Long?, newItemData: NewItemData?, navigator: MainComposeNavigator, viewModel: EditNfcTagViewModel) {
  if (id != null) {
    Timber.d("Got item id: $id")
    viewModel.onViewCreated(id)
  } else if (newItemData != null) {
    Timber.d("Got new item data: $newItemData")
    viewModel.onViewCreated(newItemData.uuid, newItemData.readOnly)
  } else {
    Timber.w("No item id either new item data provided")
    navigator.back()
  }
}
