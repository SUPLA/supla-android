package org.supla.android.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import org.supla.android.widget.onoff.OnOffWidget
import org.supla.android.widget.single.SingleWidget

fun AppWidgetManager.getAllWidgetIds(context: Context): IntArray =
  getAppWidgetIds(ComponentName(context, OnOffWidget::class.java)) +
    getAppWidgetIds(ComponentName(context, SingleWidget::class.java))
