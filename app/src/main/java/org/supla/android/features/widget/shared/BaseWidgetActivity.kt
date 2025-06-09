package org.supla.android.features.widget.shared
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

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope

abstract class BaseWidgetActivity : ComponentActivity() {
  protected abstract val viewModel: BaseWidgetViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // set default response
    setResult(RESULT_CANCELED)

    viewModel.setWidgetId(getWidgetId())

    setContent {
      val state by viewModel.getViewState().collectAsState()
      ComposableContent(state)
    }
    viewModel.onViewCreated()

    lifecycleScope.launchWhenStarted { viewModel.getViewEvents().collect { event -> handleEvent(event) } }
  }

  @Composable
  abstract fun ComposableContent(modelState: WidgetConfigurationViewModelState)

  override fun onStart() {
    super.onStart()
    viewModel.onStart()
  }

  override fun onStop() {
    super.onStop()
    viewModel.onStop()
  }

  protected abstract fun updateIntent(widgetId: Int): Intent

  protected fun getWidgetId(): Int? =
    intent?.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    )

  @SuppressLint("BatteryLife")
  protected open fun handleEvent(event: WidgetConfigurationViewEvent) {
    when (event) {
      WidgetConfigurationViewEvent.Close -> finish()
      WidgetConfigurationViewEvent.OpenSettings ->
        Intent().also {
          it.setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
          it.setData("package:$packageName".toUri())
          startActivity(it)
        }

      is WidgetConfigurationViewEvent.Finished -> {
        sendBroadcast(updateIntent(event.widgetId))
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, event.widgetId))
        finish()
      }
    }
  }
}
