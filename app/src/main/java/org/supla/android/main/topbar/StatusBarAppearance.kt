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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.supla.android.extensions.findActivity

@Stable
class StatusBarAppearanceController(
  private val defaultColor: StatusBarContentColor = StatusBarContentColor.LIGHT
) {

  private val entries = mutableMapOf<Any, Entry>()
  private var nextOrder = 0L

  var contentColor by mutableStateOf(defaultColor)
    private set

  fun register(
    key: Any,
    color: StatusBarContentColor,
  ) {
    entries[key] = Entry(
      order = nextOrder++,
      color = color,
    )

    updateContentColor()
  }

  fun update(
    key: Any,
    color: StatusBarContentColor,
  ) {
    val entry = entries[key] ?: return

    entries[key] = entry.copy(color = color)
    updateContentColor()
  }

  fun unregister(key: Any) {
    entries.remove(key)
    updateContentColor()
  }

  private fun updateContentColor() {
    contentColor = entries.values
      .maxByOrNull { it.order }
      ?.color
      ?: defaultColor
  }

  private data class Entry(
    val order: Long,
    val color: StatusBarContentColor,
  )
}

enum class StatusBarContentColor {
  LIGHT, DARK;

  val darkIcons: Boolean
    get() = when (this) {
      LIGHT -> false
      DARK -> true
    }
}

@Composable
fun SetStatusBarContentColor(
  color: StatusBarContentColor,
) {
  val controller = LocalStatusBarAppearanceController.current
  val key = LocalScreenKey.current

  DisposableEffect(controller, key) {
    controller.register(
      key = key,
      color = color,
    )

    onDispose {
      controller.unregister(key)
    }
  }

  SideEffect {
    controller.update(
      key = key,
      color = color,
    )
  }
}

@Composable
fun StatusBarAppearance() {
  val view = LocalView.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val controller = LocalStatusBarAppearanceController.current
  val contentColor = controller.contentColor

  val activity = view.context.findActivity() ?: return
  val insetsController = remember(activity.window, view) {
    WindowCompat.getInsetsController(activity.window, view)
  }

  val currentColor by rememberUpdatedState(contentColor)

  SideEffect {
    insetsController.isAppearanceLightStatusBars =
      contentColor.darkIcons
  }

  DisposableEffect(lifecycleOwner, insetsController) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        insetsController.isAppearanceLightStatusBars =
          currentColor.darkIcons
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
}

private val DefaultStatusBarAppearanceController = StatusBarAppearanceController()
private val LocalStatusBarAppearanceController = staticCompositionLocalOf { DefaultStatusBarAppearanceController }
