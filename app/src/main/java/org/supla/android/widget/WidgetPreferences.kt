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
import org.supla.android.profile.INVALID_PROFILE_ID
import org.supla.android.profile.INVALID_VALUE
import org.supla.android.widget.shared.configuration.ItemType

const val INVALID_CHANNEL_ID = -1
private const val SHARED_PREFERENCES = "SwitchPreferences"

/**
 * Maintains widget specific preferences.
 */
class WidgetPreferences(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)

    fun setWidgetConfiguration(widgetId: Int, configuration: WidgetConfiguration) {
        with(preferences.edit()) {
            putInt(getKeyForItemId(widgetId), configuration.itemId)
            putInt(getKeyForItemType(widgetId), configuration.itemType.getIntValue())
            putString(getKeyForItemCaption(widgetId), configuration.itemCaption)
            putInt(getKeyForItemFunction(widgetId), configuration.itemFunction)
            putInt(getKeyForChannelColor(widgetId), configuration.channelColor)
            putLong(getKeyForProfileId(widgetId), configuration.profileId)
            putBoolean(getKeyForWidgetVisibility(widgetId), configuration.visibility)
            putLong(getKeyForWidgetActionId(widgetId), configuration.actionId
                    ?: INVALID_VALUE)
            apply()
        }
    }

    fun getWidgetConfiguration(widgetId: Int): WidgetConfiguration? {
        val itemId = preferences.getInt(getKeyForItemId(widgetId), -1)
        if (itemId == -1) {
            return null
        }

        val itemType = ItemType.fromInt(preferences.getInt(getKeyForItemType(widgetId), 0))
                ?: ItemType.CHANNEL
        val itemCaption = preferences.getString(getKeyForItemCaption(widgetId), null)
        val itemFunction = preferences.getInt(getKeyForItemFunction(widgetId), -1)
        val channelColor = preferences.getInt(getKeyForChannelColor(widgetId), Color.WHITE)
        val profileId = preferences.getLong(getKeyForProfileId(widgetId), INVALID_PROFILE_ID)
        val visibility = preferences.getBoolean(getKeyForWidgetVisibility(widgetId), false)
        val actionId = preferences.getLong(getKeyForWidgetActionId(widgetId), INVALID_VALUE)
        return WidgetConfiguration(
                itemId,
                itemType,
                itemCaption,
                itemFunction,
                channelColor,
                profileId,
                visibility,
                actionId)
    }

    fun removeWidgetConfiguration(widgetId: Int) {
        with(preferences.edit()) {
            remove(getKeyForItemId(widgetId))
            remove(getKeyForItemType(widgetId))
            remove(getKeyForItemCaption(widgetId))
            remove(getKeyForItemFunction(widgetId))
            remove(getKeyForChannelColor(widgetId))
            remove(getKeyForProfileId(widgetId))
            remove(getKeyForWidgetVisibility(widgetId))
            remove(getKeyForWidgetActionId(widgetId))
            apply()
        }
    }
}

internal fun getKeyForItemId(widgetId: Int): String {
    return "$SHARED_PREFERENCES.ITEM_ID.$widgetId"
}

internal fun getKeyForItemType(widgetId: Int): String {
    return "$SHARED_PREFERENCES.ITEM_TYPE.$widgetId"
}

internal fun getKeyForItemCaption(widgetId: Int): String {
    return "$SHARED_PREFERENCES.ITEM_CAPTION.$widgetId"
}

internal fun getKeyForItemFunction(widgetId: Int): String {
    return "$SHARED_PREFERENCES.ITEM_FUNCTION.$widgetId"
}

internal fun getKeyForChannelColor(widgetId: Int): String {
    return "$SHARED_PREFERENCES.CHANNEL_COLOR.$widgetId"
}

internal fun getKeyForProfileId(widgetId: Int): String {
    return "$SHARED_PREFERENCES.PROFILE_ID.$widgetId"
}

internal fun getKeyForWidgetVisibility(widgetId: Int): String {
    return "$SHARED_PREFERENCES.VISIBILITY.$widgetId"
}

internal fun getKeyForWidgetActionId(widgetId: Int): String {
    return "$SHARED_PREFERENCES.ACTION_ID.$widgetId"
}

data class WidgetConfiguration(
        val itemId: Int,
        val itemType: ItemType,
        val itemCaption: String?,
        val itemFunction: Int,
        val channelColor: Int,
        val profileId: Long,
        val visibility: Boolean,
        val actionId: Long?
)