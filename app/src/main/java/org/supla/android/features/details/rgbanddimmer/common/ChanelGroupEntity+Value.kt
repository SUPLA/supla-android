package org.supla.android.features.details.rgbanddimmer.common
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

import androidx.compose.ui.graphics.Color
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.extensions.HsvColor
import org.supla.android.extensions.toHsv
import org.supla.android.usecases.group.totalvalue.DimmerAndRgbGroupValue
import org.supla.android.usecases.group.totalvalue.DimmerGroupValue
import org.supla.android.usecases.group.totalvalue.RgbGroupValue

val ChannelGroupEntity.dimmerValues: List<Int>
  get() = groupTotalValues
    .mapNotNull {
      when (it) {
        is DimmerGroupValue -> it.brightness
        is DimmerAndRgbGroupValue -> it.brightness
        else -> null
      }
    }
    .toSet() // To eliminate duplicates
    .toList()

val ChannelGroupEntity.rgbValues: List<HsvColor>
  get() = groupTotalValues
    .mapNotNull {
      when (it) {
        is RgbGroupValue -> Color(it.color).toHsv(it.brightness)
        is DimmerAndRgbGroupValue -> Color(it.color).toHsv(it.brightnessColor)
        else -> null
      }
    }
    .toSet() // To eliminate duplicates
    .toList()
