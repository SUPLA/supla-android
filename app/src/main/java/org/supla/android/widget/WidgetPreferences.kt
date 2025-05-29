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
import androidx.core.content.edit
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.core.shared.data.model.general.SuplaFunction

private const val SHARED_PREFERENCES = "SwitchPreferences"
const val INVALID_LONG = -1L
const val INVALID_INT = -1
private const val NO_ACTION = -2

/**
 * Maintains widget specific preferences.
 */
class WidgetPreferences(context: Context) {

  private val preferences: SharedPreferences = context.getSharedPreferences(
    SHARED_PREFERENCES,
    Context.MODE_PRIVATE
  )

  fun setWidgetConfiguration(widgetId: Int, configuration: WidgetConfiguration) {
    preferences.edit {
      putInt(getKeyForItemId(widgetId), configuration.itemId)
      putInt(getKeyForSubjectType(widgetId), configuration.subjectType.value)
      putString(getKeyForItemCaption(widgetId), configuration.caption)
      putInt(getKeyForItemFunction(widgetId), configuration.subjectFunction.value)
      putString(getKeyForValue(widgetId), configuration.value)
      putLong(getKeyForProfileId(widgetId), configuration.profileId)
      putBoolean(getKeyForWidgetVisibility(widgetId), configuration.visibility)
      putInt(getKeyForActionId(widgetId), configuration.actionId?.value ?: NO_ACTION)
      putInt(getKeyForWidgetAltIcon(widgetId), configuration.altIcon)
      putInt(getKeyForWidgetUserIcon(widgetId), configuration.userIcon)
      apply()
    }
  }

  fun getWidgetConfiguration(widgetId: Int): WidgetConfiguration? {
    val itemId = preferences.getInt(getKeyForItemId(widgetId), INVALID_INT)
    if (itemId == INVALID_INT) {
      return null
    }

    val subjectTypeId = preferences.getInt(getKeyForSubjectType(widgetId), INVALID_INT)
    val subjectType =
      if (subjectTypeId == INVALID_INT) {
        ItemType.fromInt(preferences.getInt(getKeyForItemType(widgetId), INVALID_INT))?.subjectType ?: SubjectType.CHANNEL
      } else {
        SubjectType.from(subjectTypeId)
      }
    val itemCaption = preferences.getString(getKeyForItemCaption(widgetId), null)
    val itemFunction = SuplaFunction.from(preferences.getInt(getKeyForItemFunction(widgetId), SuplaFunction.NONE.value))
    val value = preferences.getString(getKeyForValue(widgetId), null)
    val profileId = preferences.getLong(getKeyForProfileId(widgetId), INVALID_LONG)
    val visibility = preferences.getBoolean(getKeyForWidgetVisibility(widgetId), false)
    val actionIdInt = preferences.getInt(getKeyForActionId(widgetId), INVALID_INT)
    val actionId =
      if (actionIdInt == INVALID_INT) {
        WidgetAction.fromId(preferences.getLong(getKeyForWidgetActionId(widgetId), INVALID_LONG))?.suplaAction
      } else if (actionIdInt != NO_ACTION) {
        ActionId.from(actionIdInt)
      } else {
        null
      }
    val altIcon = preferences.getInt(getKeyForWidgetAltIcon(widgetId), -1)
    val userIcon = preferences.getInt(getKeyForWidgetUserIcon(widgetId), -1)

    return WidgetConfiguration(
      itemId,
      subjectType,
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
    preferences.edit {
      remove(getKeyForItemId(widgetId))
      remove(getKeyForItemType(widgetId))
      remove(getKeyForSubjectType(widgetId))
      remove(getKeyForItemCaption(widgetId))
      remove(getKeyForItemFunction(widgetId))
      remove(getKeyForValue(widgetId))
      remove(getKeyForProfileId(widgetId))
      remove(getKeyForWidgetVisibility(widgetId))
      remove(getKeyForWidgetActionId(widgetId))
      remove(getKeyForActionId(widgetId))
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

internal fun getKeyForSubjectType(widgetId: Int): String {
  return "$SHARED_PREFERENCES.SUBJECT_TYPE.$widgetId"
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

internal fun getKeyForActionId(widgetId: Int): String {
  return "$SHARED_PREFERENCES.ACTION.$widgetId"
}

internal fun getKeyForWidgetAltIcon(widgetId: Int): String {
  return "$SHARED_PREFERENCES.ALT_ICON.$widgetId"
}

internal fun getKeyForWidgetUserIcon(widgetId: Int): String {
  return "$SHARED_PREFERENCES.USER_ICON.$widgetId"
}

data class WidgetConfiguration(
  val itemId: Int,
  val subjectType: SubjectType,
  val caption: String?,
  val subjectFunction: SuplaFunction,
  val value: String?,
  val profileId: Long,
  val visibility: Boolean,
  val actionId: ActionId?,
  val altIcon: Int,
  val userIcon: Int
)

private enum class ItemType(val id: Int) {
  CHANNEL(0), GROUP(1), SCENE(2);

  val subjectType: SubjectType
    get() = when (this) {
      CHANNEL -> SubjectType.CHANNEL
      GROUP -> SubjectType.GROUP
      SCENE -> SubjectType.SCENE
    }

  companion object {
    fun fromInt(value: Int): ItemType? =
      if (value < 0 || value >= entries.size) {
        null
      } else {
        entries[value]
      }
  }
}

private enum class WidgetAction(
  val actionId: Long,
  val suplaAction: ActionId
) {
  TURN_ON(1, ActionId.TURN_ON),
  TURN_OFF(2, ActionId.TURN_OFF),
  MOVE_UP(3, ActionId.REVEAL),
  MOVE_DOWN(4, ActionId.SHUT),
  TOGGLE(5, ActionId.TOGGLE),
  EXECUTE(6, ActionId.EXECUTE),
  INTERRUPT(7, ActionId.INTERRUPT),
  INTERRUPT_AND_EXECUTE(8, ActionId.INTERRUPT_AND_EXECUTE),
  OPEN(9, ActionId.OPEN),
  CLOSE(10, ActionId.CLOSE),
  OPEN_CLOSE(11, ActionId.OPEN_CLOSE);

  companion object {
    fun fromId(actionId: Long?): WidgetAction? {
      return entries.firstOrNull { it.actionId == actionId }
    }
  }
}
