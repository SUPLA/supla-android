package org.supla.android.features.widget.doublewidget
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
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.widget.shared.BaseWidgetActivity
import org.supla.android.features.widget.shared.View
import org.supla.android.features.widget.shared.WidgetConfigurationViewModelState
import org.supla.android.widget.onoff.intent

@AndroidEntryPoint
class DoubleWidgetConfigurationActivity : BaseWidgetActivity() {

  override val viewModel: DoubleWidgetConfigurationViewModel by viewModels()

  @Composable
  override fun ComposableContent(modelState: WidgetConfigurationViewModelState) {
    SuplaTheme {
      viewModel.View(modelState.viewState)
    }
  }

  override fun updateIntent(widgetId: Int): Intent =
    intent(this, AppWidgetManager.ACTION_APPWIDGET_UPDATE, widgetId)
}
