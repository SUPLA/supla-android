package org.supla.android.main.view
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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.LoadingScrim

@Composable
fun MainOverlays(
  notificationState: EventNotificationState? = null,
  onEventRemoved: () -> Unit = {},
  content: @Composable () -> Unit
) {
  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    content()
    LoadingOverlayScrim()
    notificationState?.let {
      EventNotificationOverlay(
        state = it,
        modifier = Modifier.align(Alignment.BottomCenter),
        onEventRemoved = onEventRemoved
      )
    }
  }
}

@Composable
private fun LoadingOverlayScrim() {
  LocalLoadingController.current.loading.ifTrue {
    LoadingScrim()
  }
}

class LoadingController {
  var loading by mutableStateOf(false)
}

val DefaultLoadingController = LoadingController()
val LocalLoadingController = staticCompositionLocalOf { DefaultLoadingController }
