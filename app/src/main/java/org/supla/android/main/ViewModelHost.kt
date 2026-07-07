package org.supla.android.main
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.EventBasedViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.main.scaffold.LocalScaffoldPadding
import org.supla.android.main.topbar.ManageTopBar
import org.supla.android.main.topbar.TopBarState

@Composable
fun <S : ViewState, E : ViewEvent> ViewModelHost(
  viewModel: BaseViewModel<S, E>,
  topBarState: TopBarState? = null,
  eventHandler: (E) -> Unit = {},
  onCreate: () -> Unit = {},
  onResume: () -> Unit = {},
  onStart: () -> Unit = {},
  onStop: () -> Unit = {},
  content: @Composable BoxScope.(S) -> Unit
) {
  ViewModelHostBase(
    viewModel = viewModel,
    topBarState = topBarState,
    eventHandler = eventHandler,
    onCreate = onCreate,
    onResume = onResume,
    onStart = onStart,
    onStop = onStop
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(LocalScaffoldPadding.current)
    ) {
      content(it)
    }
  }
}

@Composable
fun <S : ViewState, E : ViewEvent> ViewModelHostBase(
  viewModel: BaseViewModel<S, E>,
  topBarState: TopBarState? = null,
  eventHandler: (E) -> Unit = {},
  onCreate: () -> Unit = {},
  onResume: () -> Unit = {},
  onStart: () -> Unit = {},
  onStop: () -> Unit = {},
  content: @Composable (S) -> Unit
) {
  EventBasedViewModelHost(
    viewModel = viewModel,
    topBarState = topBarState,
    eventHandler = eventHandler,
    onCreate = onCreate,
    onResume = onResume,
    onStart = onStart,
    onStop = onStop
  ) {
    val state by viewModel.getViewState().collectAsState()
    content(state)
  }
}

@Composable
fun <E : ViewEvent> EventBasedViewModelHost(
  viewModel: EventBasedViewModel<E>,
  topBarState: TopBarState? = null,
  eventHandler: (E) -> Unit = {},
  onCreate: () -> Unit = {},
  onResume: () -> Unit = {},
  onStart: () -> Unit = {},
  onStop: () -> Unit = {},
  content: @Composable () -> Unit
) {
  val lifecycleOwner = LocalLifecycleOwner.current

  viewModel.LifeCycleObserver(
    onCreate = onCreate,
    onResume = onResume,
    onStart = onStart,
    onStop = onStop
  )

  LaunchedEffect(viewModel, lifecycleOwner) {
    lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
      viewModel.getViewEvents().collect { eventHandler(it) }
    }
  }

  ManageTopBar(viewModel, topBarState)

  content()
}

@Composable
fun EventBasedViewModel<out ViewEvent>.LifeCycleObserver(
  onCreate: () -> Unit = {},
  onResume: () -> Unit = {},
  onStart: () -> Unit = {},
  onStop: () -> Unit = {},
) {
  val viewModel = this
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_CREATE -> {
          viewModel.onViewCreated()
          onCreate()
        }
        Lifecycle.Event.ON_START -> {
          onStart()
          viewModel.onStart()
        }
        Lifecycle.Event.ON_RESUME -> onResume()
        Lifecycle.Event.ON_STOP -> {
          viewModel.onStop()
          onStop()
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
