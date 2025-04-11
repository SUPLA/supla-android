package org.supla.android.features.androidauto
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
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseComposeFragment
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.androidauto.add.AddAndroidAutoItemFragment.Companion.bundle
import org.supla.android.navigator.MainNavigator
import javax.inject.Inject

@AndroidEntryPoint
class AndroidAutoItemsFragment : BaseComposeFragment<AndroidAutoItemsViewModelState, AndroidAutoItemsViewEvent>() {

  override val viewModel: AndroidAutoItemsViewModel by viewModels()

  @Inject
  lateinit var navigator: MainNavigator

  @Composable
  override fun ComposableContent(modelState: AndroidAutoItemsViewModelState) {
    SuplaTheme {
      viewModel.View(viewState = modelState.viewState)
    }
  }

  override fun handleEvents(event: AndroidAutoItemsViewEvent) {
    when (event) {
      AndroidAutoItemsViewEvent.AddItem -> navigator.navigateTo(R.id.android_auto_add_item_fragment)
      is AndroidAutoItemsViewEvent.EditItem -> navigator.navigateTo(R.id.android_auto_add_item_fragment, bundle(event.itemId))
    }
  }
}
