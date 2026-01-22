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
import androidx.core.net.toUri
import org.supla.android.core.ui.BaseComposeActivity

abstract class BaseWidgetActivity : BaseComposeActivity<WidgetConfigurationViewModelState, WidgetConfigurationViewEvent>() {
  abstract override val viewModel: BaseWidgetViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // set default response
    setResult(RESULT_CANCELED)

    viewModel.setWidgetId(getWidgetId())
  }

  protected abstract fun updateIntent(widgetId: Int): Intent

  protected fun getWidgetId(): Int? =
    intent?.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    )

  @SuppressLint("BatteryLife")
  override fun handleEvent(event: WidgetConfigurationViewEvent) {
    when (event) {
      WidgetConfigurationViewEvent.Close -> finish()
      WidgetConfigurationViewEvent.OpenSettings ->
        Intent().also {
          it.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
          it.data = "package:$packageName".toUri()
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
