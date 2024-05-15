package org.supla.android.usecases.group.totalvalue
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

import org.supla.android.lib.SuplaConst

class GroupTotalValue {
  val online: Int
    get() = if (totalCounter == 0) 0 else onlineCounter * 100 / totalCounter

  private val values: MutableList<GroupValue> = mutableListOf()
  private var totalCounter = 0
  private var onlineCounter = 0

  fun add(value: GroupValue, online: Boolean) {
    if (online) {
      values.add(value)
      onlineCounter++
    }

    totalCounter++
  }

  fun clear() {
    values.clear()
    totalCounter = 0
    onlineCounter = 0
  }

  fun asString(): String = values.joinToString(separator = SEPARATOR) { it.asString() }

  companion object {
    const val SEPARATOR = "|"

    fun parse(function: Int, string: String?): List<GroupValue> {
      val valueString = string?.split(SEPARATOR)
      if (valueString.isNullOrEmpty()) {
        return emptyList()
      }
      // Split with empty string gives list with one empty item
      if (valueString.size == 1 && valueString[0].isEmpty()) {
        return emptyList()
      }

      return valueString.map {
        when (function) {
          SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
          SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW ->
            RollerShutterGroupValue(it)

          SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND ->
            FacadeBlindGroupValue(it)

          else -> throw IllegalStateException("Parse not supported for function `$function`")
        }
      }
    }
  }
}
