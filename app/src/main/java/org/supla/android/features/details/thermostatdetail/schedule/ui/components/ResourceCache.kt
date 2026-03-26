package org.supla.android.features.details.thermostatdetail.schedule.ui.components
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

import android.content.res.Resources
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.compose.ui.graphics.Color

class ResourceCache(private val resources: Resources) {

  private val colorCache: MutableMap<Int, Color> = mutableMapOf()
  private val dimenCache: MutableMap<Int, Float> = mutableMapOf()

  fun color(@ColorRes colorRes: Int): Color {
    colorCache[colorRes]?.let {
      return it
    }

    val color = Color(resources.getColor(colorRes, null))
    colorCache[colorRes] = color
    return color
  }

  fun dimen(@DimenRes dimenRes: Int): Float {
    dimenCache[dimenRes]?.let {
      return it
    }

    val dimen = resources.getDimension(dimenRes)
    dimenCache[dimenRes] = dimen
    return dimen
  }
}
