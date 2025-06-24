package org.supla.android.widget

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
import android.content.ComponentName
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.extensions.getAllWidgetIds
import org.supla.android.extensions.getOnOffWidgetIds
import org.supla.android.extensions.getSingleWidgetIds
import org.supla.android.lib.actions.SubjectType
import org.supla.android.widget.onoff.OnOffWidget
import org.supla.android.widget.onoff.updateOnOffWidget
import org.supla.android.widget.onoff.updateOnOffWidgets
import org.supla.android.widget.single.updateSingleWidget
import org.supla.android.widget.single.updateSingleWidgets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetManager @Inject constructor(
  @ApplicationContext private val context: Context,
  private val appWidgetManager: AppWidgetManager,
  private val widgetPreferences: WidgetPreferences
) {

  fun findWidgetConfig(profileId: Long, channelId: Int): Pair<Int, WidgetConfiguration>? {
    appWidgetManager.getAllWidgetIds(context).forEach { widgetId ->
      widgetPreferences.getWidgetConfiguration(widgetId)?.let { configuration ->
        if (configuration.profileId == profileId && configuration.itemId == channelId && configuration.subjectType == SubjectType.CHANNEL) {
          return Pair(widgetId, configuration)
        }
      }
    }

    return null
  }

  fun updateWidget(widgetId: Int) {
    appWidgetManager.getSingleWidgetIds(context)?.let {
      if (it.contains(widgetId)) {
        updateSingleWidget(context, widgetId)
        return
      }
    }

    appWidgetManager.getOnOffWidgetIds(context)?.let {
      if (it.contains(widgetId)) {
        updateOnOffWidget(context, widgetId)
        return
      }
    }
  }

  fun updateAllWidgets() {
    appWidgetManager.getAppWidgetIds(ComponentName(context, OnOffWidget::class.java))?.let {
      updateOnOffWidgets(context, it)
    }
    appWidgetManager.getSingleWidgetIds(context)?.let {
      updateSingleWidgets(context, it)
    }
  }

  fun onProfileRemoved(profileId: Long) {
    appWidgetManager.getAllWidgetIds(context).forEach {
      val widgetConfig = widgetPreferences.getWidgetConfiguration(it) ?: return@forEach
      if (widgetConfig.profileId == profileId) {
        widgetPreferences.setWidgetConfiguration(it, widgetConfig.copy(profileId = INVALID_LONG))
        updateWidget(it)
      }
    }
  }
}
