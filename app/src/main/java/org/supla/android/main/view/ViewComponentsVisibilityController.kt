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

import android.view.Surface
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView

class ViewComponentsVisibilityController {
  var detailRailVisible by mutableStateOf(false)
}

@Composable
fun SetDetailRailVisible(visible: Boolean) {
  val visibilityController = LocalViewComponentsVisibilityController.current
  SideEffect {
    visibilityController.detailRailVisible = visible
  }
}

@Composable
fun Modifier.detailHorizontalPadding(): Modifier {
  val rotation = LocalView.current.display?.rotation
  val railVisible = LocalViewComponentsVisibilityController.current.detailRailVisible

  return when (rotation) {
    Surface.ROTATION_270 -> windowInsetsPadding(WindowInsets.displayCutout)
    Surface.ROTATION_90 if !railVisible -> windowInsetsPadding(WindowInsets.displayCutout)
    else -> this
  }
}

val DefaultViewComponentsVisibilityController = ViewComponentsVisibilityController()
val LocalViewComponentsVisibilityController = staticCompositionLocalOf { DefaultViewComponentsVisibilityController }
