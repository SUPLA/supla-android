package org.supla.android.usecases.list
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

import org.supla.android.ui.lists.ListItem

internal fun <T : ListItem> List<ListItem>.findReorderableSectionItems(
  movedItemId: Int,
  itemOf: (ListItem) -> T?
): List<T>? {
  val movedItemIndex = indexOfFirst { itemOf(it)?.remoteId == movedItemId }
  if (movedItemIndex < 0) {
    return null
  }

  val headerIndex = locationHeaderIndexFor(movedItemIndex) ?: return null
  val endIndex = ((headerIndex + 1)..lastIndex)
    .firstOrNull { this[it] is ListItem.LocationItem }
    ?: size

  return subList(headerIndex + 1, endIndex).mapNotNull(itemOf)
}

internal fun List<ListItem>.canMoveItemWithinSection(
  from: Int,
  to: Int,
  isItem: (ListItem) -> Boolean
): Boolean {
  val fromItem = getOrNull(from) ?: return false
  val toItem = getOrNull(to) ?: return false

  return isItem(fromItem) &&
    isItem(toItem) &&
    locationHeaderIndexFor(from) == locationHeaderIndexFor(to)
}

private fun List<ListItem>.locationHeaderIndexFor(itemIndex: Int): Int? =
  (itemIndex downTo 0).firstOrNull { this[it] is ListItem.LocationItem }
