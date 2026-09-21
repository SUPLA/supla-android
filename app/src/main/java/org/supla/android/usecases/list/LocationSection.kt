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

import org.supla.android.data.source.local.entity.LocationEntity

internal data class LocationSection<T>(
  val items: List<T>
)

private data class LocationBucket<T>(
  val location: LocationEntity,
  val items: MutableList<T>
)

internal fun <T> List<T>.toLocationSections(
  locationOf: (T) -> LocationEntity,
  positionOf: (T) -> Int
): List<LocationSection<T>> {
  if (isEmpty()) {
    return emptyList()
  }

  val buckets = mutableListOf<LocationBucket<T>>()

  forEach { item ->
    val location = locationOf(item)
    val currentBucket = buckets.lastOrNull()

    if (currentBucket?.location?.remoteId == location.remoteId) {
      currentBucket.items += item
    } else {
      buckets += LocationBucket(location, mutableListOf(item))
    }
  }

  val sections = mutableListOf<LocationSection<T>>()
  var previousLocation = buckets.first().location
  var sectionItems = buckets.first().items.toMutableList()

  buckets.drop(1).forEach { bucket ->
    val location = bucket.location
    val canMerge = previousLocation.caption == location.caption

    if (canMerge) {
      sectionItems += bucket.items
    } else {
      sections += LocationSection(sectionItems.sortedBy(positionOf))
      sectionItems = bucket.items.toMutableList()
    }

    previousLocation = location
  }

  sections += LocationSection(sectionItems.sortedBy(positionOf))
  return sections
}
