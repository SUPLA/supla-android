package org.supla.android.extensions
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
