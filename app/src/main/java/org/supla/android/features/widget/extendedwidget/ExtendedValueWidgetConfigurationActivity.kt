package org.supla.android.features.widget.extendedwidget
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

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.widget.shared.BaseWidgetActivity
import org.supla.android.features.widget.shared.View
import org.supla.android.features.widget.shared.WidgetConfigurationViewEvent
import org.supla.android.features.widget.shared.WidgetConfigurationViewModelState
import org.supla.android.widget.extended.ExtendedValueWidgetWorker

@AndroidEntryPoint
class ExtendedValueWidgetConfigurationActivity : BaseWidgetActivity() {

  override val viewModel: ExtendedValueWidgetConfigurationViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val widgetId = getWidgetId()
    if (widgetId != null && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
      val glanceAppWidgetManager = GlanceAppWidgetManager(this)
      val glanceId = glanceAppWidgetManager.getGlanceIdBy(widgetId)
      viewModel.onViewCreated(glanceId)
    } else {
      finish()
    }
  }

  @Composable
  override fun ComposableContent(modelState: WidgetConfigurationViewModelState) {
    SuplaTheme {
      viewModel.View(modelState.viewState)
    }
  }

  override fun handleEvent(event: WidgetConfigurationViewEvent) {
    when (event) {
      is WidgetConfigurationViewEvent.Finished -> {
        val workManager = WorkManager.getInstance(this@ExtendedValueWidgetConfigurationActivity)
        ExtendedValueWidgetWorker.singleRun(workManager, event.glanceId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, event.widgetId))
        finish()
      }

      else -> super.handleEvent(event)
    }
  }

  override fun updateIntent(widgetId: Int): Intent {
    throw IllegalStateException("Should not be called!")
  }
}
