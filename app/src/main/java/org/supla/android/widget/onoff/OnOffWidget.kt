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
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.shared.WidgetProviderBase
import org.supla.android.widget.shared.isWidgetValid
import timber.log.Timber
import javax.inject.Inject

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
@AndroidEntryPoint
class OnOffWidget : WidgetProviderBase() {

  @Inject
  lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Inject
  lateinit var workManagerProxy: WorkManagerProxy

  override fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int,
    configuration: WidgetConfiguration?
  ) {
    // Construct the RemoteViews object
    val views = buildWidget(context, widgetId)
    if (configuration != null && isWidgetValid(configuration)) {
      views.setTextViewText(R.id.on_off_widget_channel_name, configuration.caption)

      setChannelIcons(configuration, views)

      views.setViewVisibility(R.id.on_off_widget_removed_label, View.GONE)
    } else {
      views.setViewVisibility(R.id.on_off_widget_buttons, View.GONE)
      views.setViewVisibility(R.id.on_off_widget_value, View.GONE)
      views.setViewVisibility(R.id.on_off_widget_removed_label, View.VISIBLE)
    }
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(widgetId, views)
  }

  override fun onReceive(context: Context, intent: Intent?) {
    super.onReceive(context, intent)
    Timber.i("[DoubleWidget] Got intent with action: %s", intent?.action ?: "")

    val turnOnOff = when (intent?.action) {
      ACTION_TURN_ON -> true
      ACTION_TURN_OFF -> false
      else -> null
    }

    if (turnOnOff == null && intent?.action != ACTION_UPDATE) {
      return
    }

    val widgetIds = intent?.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: IntArray(0)
    OnOffWidgetCommandWorker.enqueue(widgetIds, turnOnOff, workManagerProxy)
  }

  private fun setChannelIcons(
    configuration: WidgetConfiguration,
    views: RemoteViews
  ) {
    val channel = ChannelEntity.create(
      function = configuration.subjectFunction,
      altIcon = configuration.altIcon,
      userIcon = configuration.userIcon,
      profileId = configuration.profileId
    )

    val iconViewId = if (channel.isValueWidget) {
      R.id.on_off_widget_value_icon
    } else {
      R.id.on_off_widget_turn_on_button
    }

    val activeIcon = getChannelIconUseCase.forState(channel, ChannelState.active(channel.function.value))
    ImageCache.loadBitmapForWidgetView(activeIcon, views, iconViewId, false)

    val viewIdNightMode = if (channel.isValueWidget) {
      R.id.on_off_widget_value_icon_night_mode
    } else {
      R.id.on_off_widget_turn_on_button_night_mode
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      ImageCache.loadBitmapForWidgetView(activeIcon, views, viewIdNightMode, true)
    }

    if (channel.isValueWidget) {
      views.setTextViewText(R.id.on_off_widget_value_text, configuration.value)
      views.setViewVisibility(R.id.on_off_widget_buttons, View.GONE)
      views.setViewVisibility(R.id.on_off_widget_value, View.VISIBLE)
    } else {
      val inactiveIcon = getChannelIconUseCase.forState(channel, ChannelState.inactive(channel.function.value))
      ImageCache.loadBitmapForWidgetView(inactiveIcon, views, R.id.on_off_widget_turn_off_button, false)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ImageCache.loadBitmapForWidgetView(inactiveIcon, views, R.id.on_off_widget_turn_off_button_night_mode, true)
      }
      views.setViewVisibility(R.id.on_off_widget_buttons, View.VISIBLE)
      views.setViewVisibility(R.id.on_off_widget_value, View.GONE)
    }
  }

  companion object {
    private val TAG = OnOffWidget::class.simpleName
  }
}

internal fun buildWidget(context: Context, widgetId: Int): RemoteViews {
  val views = RemoteViews(context.packageName, R.layout.on_off_widget)
  val turnOnPendingIntent = pendingIntent(context, ACTION_TURN_ON, widgetId)
  views.setOnClickPendingIntent(R.id.on_off_widget_turn_on_button, turnOnPendingIntent)
  views.setOnClickPendingIntent(R.id.on_off_widget_turn_on_button_night_mode, turnOnPendingIntent)
  val turnOffPendingIntent = pendingIntent(context, ACTION_TURN_OFF, widgetId)
  views.setOnClickPendingIntent(R.id.on_off_widget_turn_off_button, turnOffPendingIntent)
  views.setOnClickPendingIntent(R.id.on_off_widget_turn_off_button_night_mode, turnOffPendingIntent)
  val updatePendingIntent = pendingIntent(context, ACTION_UPDATE, widgetId)
  views.setOnClickPendingIntent(R.id.on_off_widget_value_text, updatePendingIntent)
  views.setOnClickPendingIntent(R.id.on_off_widget_value_icon, updatePendingIntent)
  views.setOnClickPendingIntent(R.id.on_off_widget_value_icon_night_mode, updatePendingIntent)

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

fun updateOnOffWidgets(context: Context, widgetIds: IntArray) =
  context.sendBroadcast(intent(context, AppWidgetManager.ACTION_APPWIDGET_UPDATE, widgetIds))

fun intent(context: Context, intentAction: String, widgetId: Int): Intent =
  intent(context, intentAction, intArrayOf(widgetId))

fun intent(context: Context, intentAction: String, widgetIds: IntArray): Intent {
  Timber.d("Creating intent with action: $intentAction")
  return Intent(context, OnOffWidget::class.java).apply {
    action = intentAction
    flags = Intent.FLAG_RECEIVER_FOREGROUND
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
  }
}
