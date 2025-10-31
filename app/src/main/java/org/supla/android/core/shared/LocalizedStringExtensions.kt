package org.supla.android.core.shared
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
import android.text.format.DateFormat
import org.supla.android.R
import org.supla.core.shared.infrastructure.LocalizedString
import java.util.Date

operator fun LocalizedString.invoke(context: Context): String {
  return when (this) {
    is LocalizedString.Constant -> text
    LocalizedString.Empty -> ""
    is LocalizedString.WithId -> {
      val parsed = arguments.map { if (it is LocalizedString) it(context) else it }
      if (parsed.hasAllowedTypes) {
        when (parsed.size) {
          0 -> context.getString(id.resourceId)
          1 -> context.getString(id.resourceId, parsed[0])
          2 -> context.getString(id.resourceId, parsed[0], parsed[1])
          3 -> context.getString(id.resourceId, parsed[0], parsed[1], parsed[2])
          4 -> context.getString(id.resourceId, parsed[0], parsed[1], parsed[2], parsed[3])
          5 -> context.getString(id.resourceId, parsed[0], parsed[1], parsed[2], parsed[3], parsed[4])
          6 -> context.getString(id.resourceId, parsed[0], parsed[1], parsed[2], parsed[3], parsed[4], parsed[5])
          else -> throw IllegalStateException("To many arguments: ${arguments.size}")
        }
      } else {
        throw IllegalStateException("Arguments contain unsupported type: $arguments")
      }
    }

    is LocalizedString.WithIdAndString -> "${context.getString(id.resourceId)} $text"

    is LocalizedString.WithIdIntStringInt -> context.getString(id.resourceId, arg1, arg2(context), arg3)
    is LocalizedString.WithResourceAndString -> "${context.getString(id)} $value"
    is LocalizedString.WithResourceAndArguments -> {
      val parsed = arguments.map { if (it is LocalizedString) it(context) else it }
      if (parsed.hasAllowedTypes) {
        when (parsed.size) {
          0 -> context.getString(id)
          1 -> context.getString(id, parsed[0])
          2 -> context.getString(id, parsed[0], parsed[1])
          3 -> context.getString(id, parsed[0], parsed[1], parsed[2])
          4 -> context.getString(id, parsed[0], parsed[1], parsed[2], parsed[3])
          5 -> context.getString(id, parsed[0], parsed[1], parsed[2], parsed[3], parsed[4])
          6 -> context.getString(id, parsed[0], parsed[1], parsed[2], parsed[3], parsed[4], parsed[5])
          else -> throw IllegalStateException("To many arguments: ${arguments.size}")
        }
      } else {
        throw IllegalStateException("Arguments contain unsupported type: $arguments")
      }
    }

    is LocalizedString.WithResourceAndDate -> {
      val format = context.getString(R.string.hour_string_format)
      context.getString(id, DateFormat.format(format, Date(timestamp)))
    }

    is LocalizedString.Merge -> texts.joinToString(delimiter) { it(context) }
    is LocalizedString.Quantity -> context.resources.getQuantityString(id, quantity, quantity)
  }
}

private val List<Any>.hasAllowedTypes: Boolean
  get() = fold(true) { acc, item ->
    acc && (item is Int || item is Long || item is String || item is Double || item is Float)
  }
