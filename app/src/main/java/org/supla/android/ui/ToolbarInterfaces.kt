package org.supla.android.ui
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

import android.content.Context
import android.content.ContextWrapper
import android.view.MenuItem
import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.supla.android.R

interface ToolbarTitleController {
  fun setToolbarTitle(title: AppBar.Title)
}

interface ToolbarItemsController {
  fun setToolbarItemVisible(itemId: Int, visible: Boolean)
}

interface ToolbarVisibilityController {
  fun setToolbarVisible(visibility: ToolbarVisibility)

  data class ToolbarVisibility(
    val visible: Boolean,
    @param:ColorRes val toolbarColorRes: Int = if (visible) R.color.primary_container else R.color.background,
    @param:ColorRes val navigationBarColorRes: Int = R.color.surface,
    val isLight: Boolean = visible.not()
  )
}

interface ToolbarItemsClickHandler {
  fun onMenuItemClick(menuItem: MenuItem): Boolean
}

@Composable
fun ToolbarVisibility(
  visible: Boolean,
  toolbarColorRes: Int = if (visible) R.color.primary_container else R.color.background,
  navigationBarColorRes: Int = R.color.surface,
  isLight: Boolean = visible.not()
) {
  val controller = LocalContext.current.findVisibilityController()
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(Unit) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START ->
          controller?.setToolbarVisible(
            ToolbarVisibilityController.ToolbarVisibility(
              visible = visible,
              toolbarColorRes = toolbarColorRes,
              navigationBarColorRes = navigationBarColorRes,
              isLight = isLight
            )
          )
        Lifecycle.Event.ON_STOP ->
          controller?.setToolbarVisible(
            ToolbarVisibilityController.ToolbarVisibility(
              visible = true,
              isLight = true
            )
          )
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}

fun Context.findVisibilityController(): ToolbarVisibilityController? {
  var context = this

  while (context is ContextWrapper) {
    if (context is ToolbarVisibilityController) {
      return context
    }

    context = context.baseContext
  }

  return null
}
