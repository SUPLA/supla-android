package org.supla.android.ui.views.tools
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
import androidx.compose.ui.unit.dp

private val defaultPointRadius = 8.dp
private val defaultShadowRadius = 4.dp

context(DrawScope)
fun drawControlPoint(
  position: Offset,
  pointColor: Color,
  pointShadowColor: Color,
  pointRadius: Float = defaultPointRadius.toPx(),
  shadowRadius: Float = defaultShadowRadius.toPx()
) {
  drawCircle(
    color = pointShadowColor,
    radius = pointRadius + shadowRadius,
    style = Fill,
    center = position
  )
  drawCircle(
    color = pointColor,
    radius = pointRadius,
    style = Fill,
    center = position
  )
}
