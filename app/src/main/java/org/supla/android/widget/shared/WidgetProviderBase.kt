package org.supla.android.widget.shared
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
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.supla.android.extensions.getAllWidgetIds
import org.supla.android.features.icons.LoadUserIconsIntoCacheWorker
import org.supla.android.images.ImageCache
import org.supla.android.widget.INVALID_INT
import org.supla.android.widget.INVALID_LONG
import org.supla.android.widget.RemoveWidgetsWorker
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences
import timber.log.Timber

/**
 * IMPORTANT: Always when adding new widget, please adapt [getAllWidgetIds].
 */
abstract class WidgetProviderBase : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    Timber.i("Updating widgets with ids: %s", appWidgetIds.toReadableString())

    if (ImageCache.size() == 0) {
      // It seems that after some time when the application is in the background, the cache is destroyed.
      // https://forum.supla.org/viewtopic.php?p=138424#p138424
      LoadUserIconsIntoCacheWorker.start(context)
    }

    val preferences = WidgetPreferences(context)
    // There may be multiple widgets active, so update all of them
    for (appWidgetId in appWidgetIds) {
      val configuration = preferences.getWidgetConfiguration(appWidgetId)
      updateAppWidget(context, appWidgetManager, appWidgetId, configuration)
    }
  }

  override fun onDeleted(context: Context, appWidgetIds: IntArray?) {
    if (appWidgetIds == null) {
      return
    }
    Timber.i("Deleting widgets with ids: %s", appWidgetIds.toReadableString())

    val removeWidgetsWork = OneTimeWorkRequestBuilder<RemoveWidgetsWorker>()
      .setInputData(
        Data.Builder().putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds).build()
      )
      .build()
    WorkManager.getInstance(context).enqueue(removeWidgetsWork)
  }

  abstract fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int,
    configuration: WidgetConfiguration?
  )
}

internal fun isWidgetValid(configuration: WidgetConfiguration) = configuration.visibility &&
  configuration.profileId != INVALID_LONG &&
  configuration.itemId != INVALID_INT

internal fun getWorkId(prefix: String, widgetIds: IntArray): String {
  return prefix + widgetIds.toWorkIdSuffix()
}

fun IntArray.toReadableString(): String {
  return this.map { it.toString() }.reduce { acc, string -> "$acc $string" }
}

private fun IntArray.toWorkIdSuffix(): String {
  return this.map { it.toString() }.reduce { acc, string -> "${acc}_$string" }
}
