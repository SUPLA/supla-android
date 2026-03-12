package org.supla.android.features.nfc.lock
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
import org.supla.android.features.nfc.NfcHost
import org.supla.android.navigator.MainNavigator
import org.supla.android.navigator.NavigationSubcontroller
import javax.inject.Inject

private const val ARG_BUNDLE_ID = "ARG_BUNDLE_ID"

@AndroidEntryPoint
class LockTagFragment : BaseComposeFragment<LockTagViewState, LockTagViewEvent>(), NavigationSubcontroller {
  override val viewModel: LockTagViewModel by viewModels()

  override fun screenTakeoverAllowed(): Boolean = false

  private val itemId: Long? by lazy { arguments?.getLong(ARG_BUNDLE_ID)?.let { if (it == 0L) null else it } }

  @Inject
  lateinit var navigator: MainNavigator

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    itemId?.let { viewModel.loadData(it) }
  }

  @Composable
  override fun ComposableContent(modelState: LockTagViewState) {
    SuplaTheme {
      viewModel.View(modelState)
    }
  }

  override fun onStart() {
    super.onStart()
    (activity as? NfcHost)?.enableNfcReader { viewModel.handleTag(it) }
  }

  override fun onStop() {
    super.onStop()
    (activity as? NfcHost)?.disableNfcReader()
  }

  override fun handleEvents(event: LockTagViewEvent) {
    when (event) {
      LockTagViewEvent.Close -> navigator.back()
    }
  }

  companion object {
    fun bundle(itemId: Long): Bundle = bundleOf(ARG_BUNDLE_ID to itemId)
  }
}
