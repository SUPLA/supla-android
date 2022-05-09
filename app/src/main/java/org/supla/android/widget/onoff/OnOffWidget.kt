package org.supla.android.widget.onoff
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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst
import org.supla.android.widget.RemoveWidgetsWorker
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.WidgetPreferences

private const val ACTION_TURN_ON = "ACTION_TURN_ON"
private const val ACTION_TURN_OFF = "ACTION_TURN_OFF"
private const val WORK_ID_PREFIX = "ON_OF_WIDGET_"

/**
 * Implementation of widgets for on-off operations. It is supporting turning on/off channels with functions of:
 * light switch [SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH],
 * dimmer [SuplaConst.SUPLA_CHANNELFNC_DIMMER],
 * RGB lightning [SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING],
 * dimmer with RGB lightning [SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING]
 */
class OnOffWidget : AppWidgetProvider() {

    companion object {
        private val TAG = OnOffWidget::javaClass.name
    }

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

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Trace.i(TAG, "Got intent with action: " + intent?.action)

        val intent: Intent = intent ?: return
        val turnOnOff = when (intent.action) {
            ACTION_TURN_ON -> true
            ACTION_TURN_OFF -> false
            else -> null
        }
        if (turnOnOff != null) {
            val widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                    ?: IntArray(0)
            val inputData = Data.Builder()
                    .putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                    .putBoolean(ARG_TURN_ON, turnOnOff)
                    .build()

            val removeWidgetsWork = OneTimeWorkRequestBuilder<OnOffWidgetCommandWorker>()
                    .setInputData(inputData)
                    .build()

            // Work for widget ID is unique, so no other worker for the same ID will be started
            WorkManager.getInstance().enqueueUniqueWork(getWorkId(widgetIds), ExistingWorkPolicy.KEEP, removeWidgetsWork)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int, configuration: WidgetConfiguration?) {
    // Construct the RemoteViews object
    val views = buildWidget(context, widgetId)
    if (configuration != null) {
        views.setTextViewText(R.id.on_off_widget_channel_name, configuration.channelCaption)

        val channel = Channel()
        channel.func = configuration.channelFunction
        views.setImageViewBitmap(R.id.on_off_widget_turn_on_button, ImageCache.getBitmap(context, channel.getImageIdx(ChannelBase.WhichOne.First, 1)))
        views.setImageViewBitmap(R.id.on_off_widget_turn_off_button, ImageCache.getBitmap(context, channel.getImageIdx(ChannelBase.WhichOne.First, 2)))
    }
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(widgetId, views)
}

internal fun buildWidget(context: Context, widgetId: Int): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.on_off_widget)
    val turnOnPendingIntent = pendingIntent(context, ACTION_TURN_ON, widgetId)
    views.setOnClickPendingIntent(R.id.on_off_widget_turn_on_button, turnOnPendingIntent)
    val turnOffPendingIntent = pendingIntent(context, ACTION_TURN_OFF, widgetId)
    views.setOnClickPendingIntent(R.id.on_off_widget_turn_off_button, turnOffPendingIntent)

    return views
}

internal fun pendingIntent(context: Context, intentAction: String, widgetId: Int): PendingIntent {
    return PendingIntent.getBroadcast(context, widgetId, intent(context, intentAction, widgetId), PendingIntent.FLAG_UPDATE_CURRENT)
}

internal fun getWorkId(widgetIds: IntArray): String {
    return if (widgetIds.size != 1) {
        WORK_ID_PREFIX
    } else {
        WORK_ID_PREFIX + widgetIds[0]
    }
}

fun intent(context: Context, intentAction: String, widgetId: Int): Intent {
    Trace.d(OnOffWidget::javaClass.name, "Creating intent with action: $intentAction")
    return Intent(context, OnOffWidget::class.java).apply {
        action = intentAction
        flags = Intent.FLAG_RECEIVER_FOREGROUND
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
    }
}

fun IntArray.toReadableString(): String {
    return this.map { it.toString() }.reduce { acc, string -> "$acc $string" }
}