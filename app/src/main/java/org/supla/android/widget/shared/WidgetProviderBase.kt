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
import org.supla.android.Trace
import org.supla.android.profile.INVALID_PROFILE_ID
import org.supla.android.widget.INVALID_CHANNEL_ID
import org.supla.android.widget.RemoveWidgetsWorker
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences

private const val WORK_ID_PREFIX = "ON_OF_WIDGET_"

abstract class WidgetProviderBase : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Trace.i(TAG, "Updating widgets with ids: " + appWidgetIds.toReadableString())

        val preferences = WidgetPreferences(context)
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val configuration = preferences.getWidgetConfiguration(appWidgetId)
            updateAppWidget(context, appWidgetManager, appWidgetId, configuration)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        if (appWidgetIds == null) {
            return
        }
        Trace.i(TAG, "Deleting widgets with ids: " + appWidgetIds.toReadableString())

        val removeWidgetsWork = OneTimeWorkRequestBuilder<RemoveWidgetsWorker>()
                .setInputData(Data.Builder().putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds).build())
                .build()
        WorkManager.getInstance().enqueue(removeWidgetsWork)
    }

    abstract fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            configuration: WidgetConfiguration?
    )

    companion object {
        private val TAG = WidgetProviderBase::javaClass.name
    }
}

internal fun isWidgetValid(configuration: WidgetConfiguration) = configuration.visibility &&
        configuration.profileId != INVALID_PROFILE_ID &&
        configuration.channelId != INVALID_CHANNEL_ID

internal fun getWorkId(widgetIds: IntArray): String {
    return if (widgetIds.size != 1) {
        WORK_ID_PREFIX
    } else {
        WORK_ID_PREFIX + widgetIds[0]
    }
}

fun IntArray.toReadableString(): String {
    return this.map { it.toString() }.reduce { acc, string -> "$acc $string" }
}