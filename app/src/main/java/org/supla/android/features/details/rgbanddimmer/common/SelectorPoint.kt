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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

val SELECTOR_RADIUS = 12.dp
private val SELECTOR_SHADOW_RADIUS = 20.dp
private val MARKER_RADIUS = 4.dp

// Colors
private val POINTER_SHADOW_COLOR = Color(0x647E8082)

fun DrawScope.drawSelectorPoint(position: Offset, color: Color) {
  // Outer black ring
  drawCircle(
    color = POINTER_SHADOW_COLOR,
    radius = SELECTOR_SHADOW_RADIUS.toPx(),
    style = Fill,
    center = position
  )
  // Inner white ring
  drawCircle(
    color = color,
    radius = SELECTOR_RADIUS.toPx(),
    style = Fill,
    center = position
  )
  drawCircle(
    color = Color.White,
    radius = SELECTOR_RADIUS.toPx(),
    style = Stroke(width = 2.dp.toPx()),
    center = position
  )
}

fun DrawScope.drawMarkerPoint(position: Offset) {
  drawCircle(
    color = Color.White,
    radius = MARKER_RADIUS.toPx(),
    style = Fill,
    center = position
  )
  drawCircle(
    color = Color.Black,
    radius = MARKER_RADIUS.toPx(),
    style = Stroke(width = 1.dp.toPx()),
    center = position
  )
}
