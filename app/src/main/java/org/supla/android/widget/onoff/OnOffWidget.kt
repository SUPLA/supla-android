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
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.View
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
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.shared.WidgetProviderBase
import org.supla.android.widget.shared.configuration.isThermometer
import org.supla.android.widget.shared.getWorkId
import org.supla.android.widget.shared.isWidgetValid

private const val ACTION_TURN_ON = "ACTION_TURN_ON"
private const val ACTION_TURN_OFF = "ACTION_TURN_OFF"
private const val ACTION_UPDATE = "ACTION_UPDATE"

/**
 * Implementation of widgets for on-off operations. It is supporting turning on/off channels with functions of:
 * light switch [SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH],
 * dimmer [SuplaConst.SUPLA_CHANNELFNC_DIMMER],
 * RGB lightning [SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING],
 * dimmer with RGB lightning [SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING]
 */
class OnOffWidget : WidgetProviderBase() {

  override fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int,
    configuration: WidgetConfiguration?
  ): RemoteViews {
    // Construct the RemoteViews object
    val views = buildWidget(context, widgetId)
    if (configuration != null && isWidgetValid(configuration)) {
      views.setTextViewText(R.id.on_off_widget_channel_name, configuration.itemCaption)

      setChannelIcons(configuration, views, context)

      views.setViewVisibility(R.id.on_off_widget_removed_label, View.GONE)
    } else {
      views.setViewVisibility(R.id.on_off_widget_buttons, View.GONE)
      views.setViewVisibility(R.id.on_off_widget_value, View.GONE)
      views.setViewVisibility(R.id.on_off_widget_removed_label, View.VISIBLE)
    }
    return views
  }

  override fun onReceive(context: Context?, intent: Intent?) {
    super.onReceive(context, intent)
    Trace.i(TAG, "Got intent with action: " + intent?.action)

    val turnOnOff = when (intent?.action) {
      ACTION_TURN_ON -> true
      ACTION_TURN_OFF -> false
      else -> null
    }
    if (turnOnOff == null && intent?.action != ACTION_UPDATE) {
      return
    }

    val widgetIds = intent?.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
      ?: IntArray(0)
    val inputData = if (turnOnOff == null) {
      Data.Builder().putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds).build()
    } else {
      Data.Builder()
        .putBoolean(ARG_TURN_ON, turnOnOff)
        .putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
        .build()
    }

    val removeWidgetsWork = OneTimeWorkRequestBuilder<OnOffWidgetCommandWorker>()
      .setInputData(inputData)
      .build()

    // Work for widget ID is unique, so no other worker for the same ID will be started
    WorkManager.getInstance()
      .enqueueUniqueWork(getWorkId(widgetIds), ExistingWorkPolicy.KEEP, removeWidgetsWork)
  }

  private fun setChannelIcons(
    configuration: WidgetConfiguration,
    views: RemoteViews,
    context: Context
  ) {
    val channel = Channel()
    channel.func = configuration.itemFunction
    channel.altIcon = configuration.altIcon
    channel.userIconId = configuration.userIcon

    val inNightMode = when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
      Configuration.UI_MODE_NIGHT_YES -> true
      else -> false
    }

    if (channel.isThermometer()) {
      views.setImageViewResource(R.id.on_off_widget_value_icon, R.drawable.thermometer)
      views.setTextViewText(R.id.on_off_widget_value_text, configuration.value)
      views.setViewVisibility(R.id.on_off_widget_buttons, View.GONE)
      views.setViewVisibility(R.id.on_off_widget_value, View.VISIBLE)
    } else {
      val activeValue = getActiveValue(configuration.itemFunction)
      views.setImageViewBitmap(
        R.id.on_off_widget_turn_on_button,
        ImageCache.getBitmap(context, channel.getImageIdx(inNightMode, ChannelBase.WhichOne.First, activeValue))
      )
      views.setImageViewBitmap(
        R.id.on_off_widget_turn_off_button,
        ImageCache.getBitmap(context, channel.getImageIdx(inNightMode, ChannelBase.WhichOne.First, 0))
      )
      views.setViewVisibility(R.id.on_off_widget_buttons, View.VISIBLE)
      views.setViewVisibility(R.id.on_off_widget_value, View.GONE)
    }
  }

  companion object {
    private val TAG = OnOffWidget::javaClass.name
  }
}

fun getActiveValue(channelFunction: Int) =
  if (channelFunction == SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING) {
    3
  } else {
    1
  }

internal fun buildWidget(context: Context, widgetId: Int): RemoteViews {
  val views = RemoteViews(context.packageName, R.layout.on_off_widget)
  val turnOnPendingIntent = pendingIntent(context, ACTION_TURN_ON, widgetId)
  views.setOnClickPendingIntent(R.id.on_off_widget_turn_on_button, turnOnPendingIntent)
  val turnOffPendingIntent = pendingIntent(context, ACTION_TURN_OFF, widgetId)
  views.setOnClickPendingIntent(R.id.on_off_widget_turn_off_button, turnOffPendingIntent)
  val updatePendingIntent = pendingIntent(context, ACTION_UPDATE, widgetId)
  views.setOnClickPendingIntent(R.id.on_off_widget_value_text, updatePendingIntent)
  views.setOnClickPendingIntent(R.id.on_off_widget_value_icon, updatePendingIntent)

  return views
}

internal fun pendingIntent(context: Context, intentAction: String, widgetId: Int): PendingIntent {
  return PendingIntent.getBroadcast(
    context,
    widgetId,
    intent(context, intentAction, widgetId),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )
}

fun updateOnOffWidget(context: Context, widgetId: Int) =
  context.sendBroadcast(intent(context, AppWidgetManager.ACTION_APPWIDGET_UPDATE, widgetId))

fun intent(context: Context, intentAction: String, widgetId: Int): Intent {
  Trace.d(OnOffWidget::javaClass.name, "Creating intent with action: $intentAction")
  return Intent(context, OnOffWidget::class.java).apply {
    action = intentAction
    flags = Intent.FLAG_RECEIVER_FOREGROUND
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
  }
}
