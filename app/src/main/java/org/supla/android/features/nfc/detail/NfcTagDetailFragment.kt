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

import android.os.Bundle
import android.view.MenuItem
import androidx.compose.runtime.Composable
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.nfc.edit.EditNfcTagFragment
import org.supla.android.features.nfc.lock.LockTagFragment
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.ToolbarItemsClickHandler
import javax.inject.Inject

private const val ARG_BUNDLE_ID = "ARG_BUNDLE_ID"

@AndroidEntryPoint
class NfcTagDetailFragment : BaseComposeFragment<NfcTagDetailViewState, NfcTagDetailViewEvent>(), ToolbarItemsClickHandler {
  override val viewModel: NfcTagDetailViewModel by viewModels()

  @Inject
  lateinit var navigator: MainNavigator

  private val itemId: Long? by lazy { arguments?.getLong(ARG_BUNDLE_ID)?.let { if (it == 0L) null else it } }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    itemId?.let { viewModel.setItemId(it) }
  }

  override fun onResume() {
    super.onResume()
    setToolbarItemVisible(R.id.toolbar_delete, true)
  }

  override fun onPause() {
    super.onPause()
    setToolbarItemVisible(R.id.toolbar_delete, false)
  }

  @Composable
  override fun ComposableContent(modelState: NfcTagDetailViewState) {
    SuplaTheme {
      viewModel.View(modelState)
    }
  }

  override fun handleEvents(event: NfcTagDetailViewEvent) {
    when (event) {
      NfcTagDetailViewEvent.Close -> navigator.back()
      is NfcTagDetailViewEvent.SetToolbarTitle -> setToolbarTitle(event.tagName)
      NfcTagDetailViewEvent.EditTag ->
        itemId?.let { navigator.navigateTo(R.id.nfc_tag_edit_fragment, EditNfcTagFragment.bundle(it)) }
      NfcTagDetailViewEvent.LockTag ->
        itemId?.let { navigator.navigateTo(R.id.nfc_tag_lock_fragment, LockTagFragment.bundle(it)) }
    }
  }

  override fun onMenuItemClick(menuItem: MenuItem): Boolean {
    if (menuItem.itemId == R.id.toolbar_delete) {
      viewModel.delete()
      return true
    }

    return false
  }

  companion object {
    fun bundle(itemId: Long): Bundle = bundleOf(ARG_BUNDLE_ID to itemId)
  }
}
