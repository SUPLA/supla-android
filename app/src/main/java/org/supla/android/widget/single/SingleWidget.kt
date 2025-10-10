package org.supla.android.widget.single
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
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaConst
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.widget.WidgetConfiguration
import org.supla.android.widget.shared.WidgetProviderBase
import org.supla.android.widget.shared.isWidgetValid
import timber.log.Timber
import javax.inject.Inject

private const val ACTION_PRESSED = "ACTION_PRESSED"

/**
 * Implementation of widgets for on-off operations. It is supporting turning on/off channels with functions of:
 * light switch [SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH],
 * dimmer [SuplaConst.SUPLA_CHANNELFNC_DIMMER],
 * RGB lightning [SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING],
 * dimmer with RGB lightning [SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING]
 */
@AndroidEntryPoint
class SingleWidget : WidgetProviderBase() {

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
      views.setTextViewText(R.id.single_widget_channel_name, configuration.caption)

      if (configuration.subjectType == SubjectType.SCENE) {
        val scene = Scene(
          profileId = configuration.profileId,
          altIcon = configuration.altIcon,
          userIcon = configuration.userIcon
        )

        val icon = scene.getImageId()
        ImageCache.loadBitmapForWidgetView(icon, views, R.id.single_widget_button, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          ImageCache.loadBitmapForWidgetView(icon, views, R.id.single_widget_button_night_mode, true)
          views.setViewVisibility(R.id.single_widget_button_night_mode, View.VISIBLE)
        } else {
          views.setViewVisibility(R.id.single_widget_button_night_mode, View.GONE)
        }
        views.setViewVisibility(R.id.single_widget_button, View.VISIBLE)
      } else {
        setChannelIcons(configuration, views)
      }

      views.setViewVisibility(R.id.single_widget_removed_label, View.GONE)
    } else {
      views.setViewVisibility(R.id.single_widget_text, View.GONE)
      views.setViewVisibility(R.id.single_widget_button, View.GONE)
      views.setViewVisibility(R.id.single_widget_button_night_mode, View.GONE)
      views.setViewVisibility(R.id.single_widget_removed_label, View.VISIBLE)
    }
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(widgetId, views)
  }

  override fun onReceive(context: Context, intent: Intent?) {
    super.onReceive(context, intent)
    Timber.i("[SingleWidget] Got intent with action: %s", intent?.action ?: "")

    if (intent?.action == ACTION_PRESSED) {
      val widgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: IntArray(0)
      SingleWidgetCommandWorker.enqueue(widgetIds, workManagerProxy)
    }
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

    if (channel.isValueWidget) {
      views.setTextViewText(R.id.single_widget_text, configuration.value)
      views.setViewVisibility(R.id.single_widget_button, View.GONE)
      views.setViewVisibility(R.id.single_widget_button_night_mode, View.GONE)
      views.setViewVisibility(R.id.single_widget_text, View.VISIBLE)
    } else {
      val state = if (turnOnOrClose(configuration)) {
        ChannelState.active(channel.function.value)
      } else {
        ChannelState.inactive(channel.function.value)
      }

      val icon = getChannelIconUseCase.forState(channel, state)
      ImageCache.loadBitmapForWidgetView(icon, views, R.id.single_widget_button, false)
      ImageCache.loadBitmapForWidgetView(icon, views, R.id.single_widget_button_night_mode, true)

      views.setViewVisibility(R.id.single_widget_button, View.VISIBLE)
      views.setViewVisibility(R.id.single_widget_button_night_mode, View.VISIBLE)
      views.setViewVisibility(R.id.single_widget_text, View.GONE)
    }
  }

  companion object {
    private val TAG = SingleWidget::class.simpleName
  }
}

internal fun buildWidget(context: Context, widgetId: Int): RemoteViews {
  val views = RemoteViews(context.packageName, R.layout.single_widget)
  val turnOnPendingIntent = pendingIntent(context, ACTION_PRESSED, widgetId)
  views.setOnClickPendingIntent(R.id.single_widget_button, turnOnPendingIntent)
  views.setOnClickPendingIntent(R.id.single_widget_button_night_mode, turnOnPendingIntent)
  views.setOnClickPendingIntent(R.id.single_widget_text, turnOnPendingIntent)

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

internal fun turnOnOrClose(configuration: WidgetConfiguration): Boolean =
  configuration.actionId == ActionId.TURN_ON ||
    configuration.actionId == ActionId.CLOSE ||
    configuration.actionId == ActionId.SHUT ||
    configuration.actionId == ActionId.EXPAND

fun updateSingleWidget(context: Context, widgetId: Int) =
  context.sendBroadcast(intent(context, AppWidgetManager.ACTION_APPWIDGET_UPDATE, widgetId))

fun updateSingleWidgets(context: Context, widgetIds: IntArray) =
  context.sendBroadcast(intent(context, AppWidgetManager.ACTION_APPWIDGET_UPDATE, widgetIds))

fun intent(context: Context, intentAction: String, widgetId: Int): Intent =
  intent(context, intentAction, intArrayOf(widgetId))

fun intent(context: Context, intentAction: String, widgetIds: IntArray): Intent {
  Timber.d("Creating intent with action: $intentAction")
  return Intent(context, SingleWidget::class.java).apply {
    action = intentAction
    flags = Intent.FLAG_RECEIVER_FOREGROUND
    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
  }
}
