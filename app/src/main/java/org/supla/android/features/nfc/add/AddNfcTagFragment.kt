package org.supla.android.features.nfc.add
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

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.nfc.NfcHost
import org.supla.android.features.nfc.edit.EditNfcTagFragment
import org.supla.android.navigator.MainNavigator
import org.supla.android.navigator.NavigationSubcontroller
import org.supla.android.ui.ToolbarVisibilityController
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AddNfcTagFragment : BaseComposeFragment<AddNfcTagViewModelState, AddNfcTagViewEvent>(), NavigationSubcontroller {
  override val viewModel: AddNfcTagViewModel by viewModels()

  @Inject
  lateinit var navigator: MainNavigator

  @Composable
  override fun ComposableContent(modelState: AddNfcTagViewModelState) {
    SuplaTheme {
      viewModel.View(modelState.viewState)
    }
  }

  fun handle(intent: Intent) {
    Timber.d("Got an intent (action: ${intent.action})")
    viewModel.handleIntent(intent)
  }

  override fun handleEvents(event: AddNfcTagViewEvent) {
    when (event) {
      AddNfcTagViewEvent.Close -> navigator.back()
      is AddNfcTagViewEvent.ConfigureTagAction -> {
        navigator.back()
        navigator.navigateTo(R.id.edit_nfc_tag_fragment, EditNfcTagFragment.bundle(event.tagId))
      }

      AddNfcTagViewEvent.DisableNfc -> disableNfcDispatch()
      AddNfcTagViewEvent.EnableNfc -> enableNfcDispatch()
    }
  }

  override fun getToolbarVisibility(): ToolbarVisibilityController.ToolbarVisibility =
    ToolbarVisibilityController.ToolbarVisibility(
      visible = true,
      toolbarColorRes = R.color.primary_container,
      navigationBarColorRes = R.color.primary_container,
      shadowVisible = false,
      isLight = false
    )

  override fun screenTakeoverAllowed(): Boolean = false

  private fun enableNfcDispatch() {
    (activity as? NfcHost)?.enableNfcDispatch { intent -> handle(intent) }
  }

  private fun disableNfcDispatch() {
    (activity as? NfcHost)?.disableNfcDispatch()
  }
}
