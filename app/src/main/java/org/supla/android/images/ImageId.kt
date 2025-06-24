package org.supla.android.images
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

import kotlinx.serialization.Serializable

@Suppress("DataClassPrivateConstructor")
@Serializable
data class ImageId private constructor(
  val id: Int,
  val subId: Int,
  val profileId: Long,
  val userImage: Boolean,
  private var nightMode: Boolean = false
) {

  constructor(id: Int) : this(id, 0, 0, false)
  constructor(id: Int, subId: Int, profileId: Long) : this(id, subId, profileId, true)

  fun setNightMode(nightMode: Boolean): ImageId {
    this.nightMode = nightMode
    return this
  }

  companion object {
    fun equals(id1: ImageId?, id2: ImageId?): Boolean {
      if (id1 == null || id2 == null) {
        return false
      }

      return id1 == id2
    }
  }
}
