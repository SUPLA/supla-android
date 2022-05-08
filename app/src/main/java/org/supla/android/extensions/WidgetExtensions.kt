package org.supla.android.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import org.supla.android.widget.onoff.OnOffWidget

fun AppWidgetManager.getAllWidgetIds(context: Context): IntArray =
        getAppWidgetIds(ComponentName(context, OnOffWidget::class.java))