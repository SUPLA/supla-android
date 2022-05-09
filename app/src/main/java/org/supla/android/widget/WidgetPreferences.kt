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


import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

const val INVALID_PROFILE_ID: Long = -111
private const val SHARED_PREFERENCES = "SwitchPreferences"

/**
 * Maintains widget specific preferences.
 */
class WidgetPreferences(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)

    fun setWidgetConfiguration(widgetId: Int, configuration: WidgetConfiguration) {
        with(preferences.edit()) {
            putInt(getKeyForChannelId(widgetId), configuration.channelId)
            putString(getKeyForChannelCaption(widgetId), configuration.channelCaption)
            putInt(getKeyForChannelFunction(widgetId), configuration.channelFunction)
            putInt(getKeyForChannelColor(widgetId), configuration.channelColor)
            putLong(getKeyForProfileId(widgetId), configuration.profileId)
            apply()
        }
    }

    fun getWidgetConfiguration(widgetId: Int): WidgetConfiguration? {
        val channelId = preferences.getInt(getKeyForChannelId(widgetId), -1)
        if (channelId == -1) {
            return null
        }

        val channelCaption = preferences.getString(getKeyForChannelCaption(widgetId), null)
        val channelFunction = preferences.getInt(getKeyForChannelFunction(widgetId), -1)
        val channelColor = preferences.getInt(getKeyForChannelColor(widgetId), Color.WHITE)
        val profileId = preferences.getLong(getKeyForProfileId(widgetId), INVALID_PROFILE_ID)
        return WidgetConfiguration(channelId, channelCaption, channelFunction, channelColor, profileId)
    }

    fun removeWidgetConfiguration(widgetId: Int) {
        with(preferences.edit()) {
            remove(getKeyForChannelId(widgetId))
            remove(getKeyForChannelCaption(widgetId))
            apply()
        }
    }
}

internal fun getKeyForChannelId(widgetId: Int): String {
    return "$SHARED_PREFERENCES.CHANNEL_ID.$widgetId"
}

internal fun getKeyForChannelCaption(widgetId: Int): String {
    return "$SHARED_PREFERENCES.CHANNEL_CAPTION.$widgetId"
}

internal fun getKeyForChannelFunction(widgetId: Int): String {
    return "$SHARED_PREFERENCES.CHANNEL_FUNCTION.$widgetId"
}

internal fun getKeyForChannelColor(widgetId: Int): String {
    return "$SHARED_PREFERENCES.CHANNEL_COLOR.$widgetId"
}

internal fun getKeyForProfileId(widgetId: Int): String {
    return "$SHARED_PREFERENCES.PROFILE_ID.$widgetId"
}

data class WidgetConfiguration(
        val channelId: Int,
        val channelCaption: String?,
        val channelFunction: Int,
        val channelColor: Int,
        val profileId: Long
)