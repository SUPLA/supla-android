package org.supla.android.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import org.supla.android.widget.onoff.OnOffWidget
import org.supla.android.widget.single.SingleWidget

fun AppWidgetManager.getOnOffWidgetIds(context: Context): IntArray? =
  getAppWidgetIds(ComponentName(context, OnOffWidget::class.java))

fun AppWidgetManager.getSingleWidgetIds(context: Context): IntArray? =
  getAppWidgetIds(ComponentName(context, SingleWidget::class.java))

fun AppWidgetManager.getAllWidgetIds(context: Context): IntArray {
  val ids = mutableListOf<Int>()
  getOnOffWidgetIds(context)?.let { ids.addAll(it.toList()) }
  getSingleWidgetIds(context)?.let { ids.addAll(it.toList()) }
  return ids.toIntArray()
}
