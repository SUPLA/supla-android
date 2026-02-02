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

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.nfc.shared.edit.EditNfcTagViewEvent
import org.supla.android.features.nfc.shared.edit.EditNfcTagViewModelState
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

private const val ARG_ITEM_ID = "ARG_ITEM_ID"

@AndroidEntryPoint
class EditNfcTagFragment : BaseComposeFragment<EditNfcTagViewModelState, EditNfcTagViewEvent>() {
  override val viewModel: EditNfcTagViewModel by viewModels()

  @Inject
  lateinit var navigator: MainNavigator

  private val itemId: Long? by lazy { arguments?.getLong(ARG_ITEM_ID) }

  @Composable
  override fun ComposableContent(modelState: EditNfcTagViewModelState) {
    SuplaTheme {
      viewModel.View(modelState.screenState)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val id = itemId
    if (id == null) {
      navigator.back()
    } else {
      viewModel.onViewCreated(id)
    }
  }

  override fun handleEvents(event: EditNfcTagViewEvent) {
    when (event) {
      EditNfcTagViewEvent.Close -> navigator.back()
    }
  }

  companion object {
    fun bundle(itemId: Long): Bundle = bundleOf(ARG_ITEM_ID to itemId)
  }
}
