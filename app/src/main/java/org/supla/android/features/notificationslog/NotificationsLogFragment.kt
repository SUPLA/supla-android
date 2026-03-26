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

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.infrastructure.navigation.ToolbarItemsVisibilityController
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.UpHandler
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.databinding.FragmentComposeBinding
import org.supla.android.navigator.MainNavigator
import org.supla.android.ui.ToolbarItemsClickHandler
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsLogFragment :
  BaseComposeFragment<NotificationsLogViewState, NotificationsLogViewEvent>(),
  ToolbarItemsClickHandler,
  ToolbarItemsVisibilityController,
  UpHandler {

  override val viewModel: NotificationsLogViewModel by viewModels()
  private val binding by viewBinding(FragmentComposeBinding::bind)

  override val toolbarItems = listOf(R.id.toolbar_delete_all, R.id.toolbar_delete_older_than_month, R.id.toolbar_search)

  @Inject
  lateinit var navigator: MainNavigator

  private val onBackPressedCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      if (toolbar?.inSearchMode() == true) {
        toolbar?.hideSearch()
      } else {
        navigator.back()
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
  }

  @Composable
  override fun ComposableContent(viewState: NotificationsLogViewState) {
    SuplaTheme {
      viewModel.View(viewState)
    }
  }

  override fun handleEvents(event: NotificationsLogViewEvent) {
    when (event) {
      is NotificationsLogViewEvent.ShowDeleteNotification -> {
        Snackbar.make(binding.root, R.string.notification_deleted, DELETE_DELAY_SECS.times(1000).toInt()).apply {
          setAction(getText(R.string.cancel)) { viewModel.cancelDeletion(event.id) }
          show()
        }
      }
    }
  }

  override fun handleViewState(state: NotificationsLogViewState) {
  }

  override fun onMenuItemClick(menuItem: MenuItem): Boolean {
    if (menuItem.itemId == R.id.toolbar_delete_all) {
      viewModel.askDeleteAll()
      return true
    }
    if (menuItem.itemId == R.id.toolbar_delete_older_than_month) {
      viewModel.askDeleteOlderThanMonth()
      return true
    }
    if (menuItem.itemId == R.id.toolbar_search) {
      toolbar?.showSearch(
        onTextChanged = { viewModel.search(it) },
        onSearchClosed = { viewModel.loadAll() }
      )
    }

    return false
  }

  override fun onUpPressed(): Boolean {
    if (toolbar?.inSearchMode() == true) {
      toolbar?.hideSearch()
      return true
    }

    return false
  }
}
