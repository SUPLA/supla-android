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
import org.supla.android.widget.shared.configuration.ItemType

const val INVALID_CHANNEL_ID = -1
private const val SHARED_PREFERENCES = "SwitchPreferences"
private const val INVALID_VALUE = -1L
internal const val INVALID_PROFILE_ID = -1L

/**
 * Maintains widget specific preferences.
 */
class WidgetPreferences(context: Context) {

  private val preferences: SharedPreferences = context.getSharedPreferences(
    SHARED_PREFERENCES,
    Context.MODE_PRIVATE
  )

  fun setWidgetConfiguration(widgetId: Int, configuration: WidgetConfiguration) {
    with(preferences.edit()) {
      putInt(getKeyForItemId(widgetId), configuration.itemId)
      putInt(getKeyForItemType(widgetId), configuration.itemType.getIntValue())
      putString(getKeyForItemCaption(widgetId), configuration.itemCaption)
      putInt(getKeyForItemFunction(widgetId), configuration.itemFunction)
      putString(getKeyForValue(widgetId), configuration.value)
      putLong(getKeyForProfileId(widgetId), configuration.profileId)
      putBoolean(getKeyForWidgetVisibility(widgetId), configuration.visibility)
      putLong(getKeyForWidgetActionId(widgetId), configuration.actionId ?: INVALID_VALUE)
      putInt(getKeyForWidgetAltIcon(widgetId), configuration.altIcon)
      putInt(getKeyForWidgetUserIcon(widgetId), configuration.userIcon)
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
    val value = preferences.getString(getKeyForValue(widgetId), null)
    val profileId = preferences.getLong(getKeyForProfileId(widgetId), INVALID_PROFILE_ID)
    val visibility = preferences.getBoolean(getKeyForWidgetVisibility(widgetId), false)
    val actionId = preferences.getLong(getKeyForWidgetActionId(widgetId), INVALID_VALUE)
    val altIcon = preferences.getInt(getKeyForWidgetAltIcon(widgetId), -1)
    val userIcon = preferences.getInt(getKeyForWidgetUserIcon(widgetId), -1)
    return WidgetConfiguration(
      itemId,
      itemType,
      itemCaption,
      itemFunction,
      value,
      profileId,
      visibility,
      actionId,
      altIcon,
      userIcon
    )
  }

  fun removeWidgetConfiguration(widgetId: Int) {
    with(preferences.edit()) {
      remove(getKeyForItemId(widgetId))
      remove(getKeyForItemType(widgetId))
      remove(getKeyForItemCaption(widgetId))
      remove(getKeyForItemFunction(widgetId))
      remove(getKeyForValue(widgetId))
      remove(getKeyForProfileId(widgetId))
      remove(getKeyForWidgetVisibility(widgetId))
      remove(getKeyForWidgetActionId(widgetId))
      remove(getKeyForWidgetAltIcon(widgetId))
      remove(getKeyForWidgetUserIcon(widgetId))
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

internal fun getKeyForValue(widgetId: Int): String {
  return "$SHARED_PREFERENCES.VALUE.$widgetId"
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

internal fun getKeyForWidgetAltIcon(widgetId: Int): String {
  return "$SHARED_PREFERENCES.ALT_ICON.$widgetId"
}

internal fun getKeyForWidgetUserIcon(widgetId: Int): String {
  return "$SHARED_PREFERENCES.USER_ICON.$widgetId"
}

data class WidgetConfiguration(
  val itemId: Int,
  val itemType: ItemType,
  val itemCaption: String?,
  val itemFunction: Int,
  val value: String?,
  val profileId: Long,
  val visibility: Boolean,
  val actionId: Long?,
  val altIcon: Int,
  val userIcon: Int
)
