package org.supla.android.core.ui
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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import org.supla.android.R
import org.supla.android.extensions.setStatusBarColor

abstract class BaseComposeActivity<S : ViewState, E : ViewEvent> : ComponentActivity() {
  protected abstract val viewModel: BaseViewModel<S, E>

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStatusBarColor()

    setContent {
      val state by viewModel.getViewState().collectAsState()
      ComposableContent(state)
    }

    viewModel.onViewCreated()

    lifecycleScope.launchWhenStarted { viewModel.getViewEvents().collect { event -> handleEvent(event) } }
  }

  @Composable
  abstract fun ComposableContent(modelState: S)

  protected open fun handleEvent(event: E) {}

  protected open fun setStatusBarColor() {
    setStatusBarColor(R.color.primary_container, R.color.surface, false)
  }

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  override fun onStop() {
    super.onStop()
    viewModel.onStop()
  }
}
