package org.supla.android.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.extensions.getAllWidgetIds
import org.supla.android.profile.INVALID_PROFILE_ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetVisibilityHandler @Inject constructor(
        @ApplicationContext private val context: Context,
        private val appWidgetManager: AppWidgetManager,
        private val widgetPreferences: WidgetPreferences
) {

    fun onProfileRemoved(profileId: Long) {
        appWidgetManager.getAllWidgetIds(context).forEach {
            val widgetConfig = widgetPreferences.getWidgetConfiguration(it) ?: return@forEach
            if (widgetConfig.profileId == profileId) {
                widgetPreferences.setWidgetConfiguration(it, widgetConfig.copy(profileId = INVALID_PROFILE_ID))
                updateWidget(it)
            }
        }
    }

    private fun updateWidget(widgetId: Int) {
        val intent = Intent().apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
        }
        context.sendBroadcast(intent)
    }
}