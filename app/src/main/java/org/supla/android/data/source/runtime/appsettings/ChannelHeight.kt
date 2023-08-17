package org.supla.android.data.source.runtime.appsettings
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

enum class ChannelHeight(val percent: Int) {
  HEIGHT_60(60),
  HEIGHT_100(100),
  HEIGHT_150(150);

  fun position(): Int {
    for ((position, height) in ChannelHeight.values().withIndex()) {
      if (height == this) {
        return position
      }
    }

    throw IllegalStateException("Position not found!")
  }

  companion object {
    fun forPosition(position: Int): ChannelHeight {
      for ((i, height) in ChannelHeight.values().withIndex()) {
        if (i == position) {
          return height
        }
      }

      throw IllegalStateException("Position not found!")
    }
  }
}
