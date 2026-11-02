package org.supla.android.main.topbar
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.supla.android.core.ui.EventBasedViewModel
import org.supla.android.core.ui.ViewEvent

/**
 * Is called automatically inside [org.supla.android.main.ViewModelHostBase] when topBarState set.
 */
@Composable
fun ManageTopBar(
  viewModel: EventBasedViewModel<out ViewEvent>? = null,
  state: TopBarState? = TopBarState()
) {
  val scope = rememberCoroutineScope()
  val controller = LocalTopBarController.current
  val screenKey = LocalScreenKey.current
  val owner = remember(screenKey) { TopBarOwner(screenKey) }

  if (state != null) {
    DisposableEffect(state) {
      controller.setTopBar(
        owner = owner,
        state = state,
      )

      viewModel?.let {
        scope.launch {
          if (it.manageScreenTitle) {
            it.title.collect { title ->
              controller.updateTitle(title)
            }
          }
        }
      }

      onDispose { controller.clear(owner) }
    }
  } else if (viewModel?.manageScreenTitle == true) {
    DisposableEffect(viewModel) {
      controller.setTopBar(
        owner = owner,
        state = TopBarState(),
      )

      scope.launch {
        viewModel.title.collect { title ->
          controller.updateTitle(title)
        }
      }

      onDispose { controller.clear(owner) }
    }
  }
}
