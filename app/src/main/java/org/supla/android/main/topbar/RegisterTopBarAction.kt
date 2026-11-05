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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun RegisterTopBarAction(
  action: TopBarAction,
  visible: Boolean = true,
) {
  val controller = LocalTopBarController.current

  DisposableEffect(controller, action, visible) {
    if (visible) {
      controller.registerAction(action)
    }

    onDispose {
      if (visible) {
        controller.unregisterAction(action)
      }
    }
  }
}

@Composable
fun RegisterTopBarAction(
  action: TopBarAction,
  handler: () -> Unit,
  visible: Boolean = true,
) {
  RegisterTopBarAction(action, visible)
  HandleTopBarEvent(action.event, handler)
}

@Composable
fun HandleTopBarEvent(event: TopBarEvent, handler: () -> Unit) {
  val events = LocalTopBarController.current.events
  val currentHandler = rememberUpdatedState(handler)

  LaunchedEffect(events, event) {
    events.collect { emittedEvent ->
      if (emittedEvent == event) {
        currentHandler.value()
      }
    }
  }
}
