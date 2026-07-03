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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.supla.android.R
import org.supla.android.core.ui.EventBasedViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.core.shared.infrastructure.LocalizedString

class TopBarController(initialState: TopBarState = TopBarState()) {
  private val _state = MutableStateFlow(initialState)
  val state = _state.asStateFlow()

  private val _events = MutableSharedFlow<TopBarEvent>()
  val events = _events.asSharedFlow()

  suspend fun emit(event: TopBarEvent) {
    _events.emit(event)
  }

  fun update(updater: (TopBarState) -> TopBarState) {
    _state.value = updater(_state.value)
  }
}

sealed class TopBarIcon(val event: TopBarEvent, val iconRes: Int? = null, val descriptionRes: Int? = null) {
  data object ReloadHistory : TopBarIcon(TopBarEvent.ReloadChartHistory)
  data object OpenOcr : TopBarIcon(
    event = TopBarEvent.OpenOcr,
    iconRes = R.drawable.ic_ocr_photo,
    descriptionRes = R.string.toolbar_ocr
  )

  data object OpenSettings : TopBarIcon(
    event = TopBarEvent.OpenSettings,
    iconRes = R.drawable.ic_settings,
    descriptionRes = R.string.settings
  )
}

data class TopBarState(
  val icons: List<TopBarIcon> = emptyList(),
  val title: LocalizedString = LocalizedString.Empty
) {
  constructor(icon: TopBarIcon) : this(icons = listOf(icon))
}

sealed interface TopBarEvent {
  data object ReloadChartHistory : TopBarEvent
  data object OpenOcr : TopBarEvent
  data object OpenSettings : TopBarEvent
}

@Composable
fun RegisterTopBarIcon(
  icon: TopBarIcon,
  handler: () -> Unit
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val topBarController = LocalTopBarController.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START -> topBarController.update { it.copy(icons = listOf(icon)) }
        Lifecycle.Event.ON_STOP -> topBarController.update { it.copy(icons = emptyList()) }
        else -> Unit
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  HandleTopBarEvent(
    event = icon.event,
    handler = handler
  )
}

@Composable
fun RegisterStatedTopBarIcon(
  icon: TopBarIcon,
  visible: Boolean,
  handler: () -> Unit
) {
  val topBarController = LocalTopBarController.current

  LaunchedEffect(visible) {
    if (visible) {
      topBarController.update { it.copy(icons = listOf(icon)) }
    } else {
      topBarController.update { it.copy(icons = emptyList()) }
    }
  }

  DisposableEffect(Unit) {
    onDispose { topBarController.update { it.copy(icons = emptyList()) } }
  }

  HandleTopBarEvent(
    event = icon.event,
    handler = handler
  )
}

@Composable
fun HandleTopBarEvents(
  handler: (TopBarEvent) -> Unit
) {
  val topBarEvents = LocalTopBarController.current.events

  LaunchedEffect(Unit) {
    topBarEvents.collect { handler(it) }
  }
}

@Composable
fun HandleTopBarEvent(
  event: TopBarEvent,
  handler: () -> Unit
) {
  val topBarEvents = LocalTopBarController.current.events

  LaunchedEffect(Unit) {
    topBarEvents.collect {
      if (it == event) {
        handler()
      }
    }
  }
}

@Composable
fun ManageScreenTitle(viewModel: EventBasedViewModel<out ViewEvent>) {
  if (viewModel.manageScreenTitle) {
    val title by viewModel.title.collectAsState()
    SetScreenTitle(title)
  }
}

@Composable
fun SetScreenTitle(title: LocalizedString) {
  val topBarController = LocalTopBarController.current
  LaunchedEffect(title) {
    topBarController.update { it.copy(title = title) }
  }
}

private val DefaultTopBarController = TopBarController()
val LocalTopBarController = staticCompositionLocalOf { DefaultTopBarController }
