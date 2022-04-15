package org.supla.android.widget
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
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Worker for handling widget removal. When widget is removed the preferences are cleaned up within this worker.
 */
class RemoveWidgetsWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private val preferences = WidgetPreferences(appContext)

    override fun doWork(): Result {
        val widgetIds: IntArray = inputData.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                ?: return Result.failure()

        for (widgetId in widgetIds) {
            preferences.removeWidgetConfiguration(widgetId)
        }

        return Result.success()
    }
}