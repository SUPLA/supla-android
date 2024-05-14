package org.supla.android.features.details.windowdetail.base.ui
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

import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import org.supla.android.features.details.windowdetail.base.ui.rollershutter.slatShadowRadius

private val windowShadowRadius = 4.dp

context(DrawScope)
fun NativePaint.applyForWindow(colors: WindowColors) {
  style = android.graphics.Paint.Style.FILL
  strokeCap = android.graphics.Paint.Cap.SQUARE
  color = colors.window.toArgb()
  alpha = 255
  setShadowLayer(windowShadowRadius.toPx(), 0f, 2.dp.toPx(), colors.shadow.toArgb())
}

context(DrawScope)
fun NativePaint.applyForSlat(colors: WindowColors) {
  style = android.graphics.Paint.Style.FILL
  strokeCap = android.graphics.Paint.Cap.SQUARE
  color = colors.slatBackground.toArgb()
  alpha = 255
  setShadowLayer(slatShadowRadius.toPx(), 0f, 1.5.dp.toPx(), colors.shadow.toArgb())
}

context(DrawScope)
fun NativePaint.applyForShadow(colors: WindowColors) {
  style = android.graphics.Paint.Style.FILL
  strokeCap = android.graphics.Paint.Cap.SQUARE
  color = colors.slatBackground.toArgb()
  alpha = 61
  setShadowLayer(0f, 0f, 0f, colors.shadow.toArgb())
}

context(DrawScope)
fun NativePaint.applyForAwningMarker(colors: WindowColors) {
  style = android.graphics.Paint.Style.FILL
  strokeCap = android.graphics.Paint.Cap.SQUARE
  color = colors.slatBackground.toArgb()
  alpha = 100
  setShadowLayer(0f, 0f, 0f, colors.shadow.toArgb())
}
