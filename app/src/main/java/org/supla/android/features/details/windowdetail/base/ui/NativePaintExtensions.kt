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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import org.supla.android.features.details.windowdetail.base.ui.rollershutter.slatShadowRadius

private val windowShadowRadius = 4.dp

context(DrawScope)
fun NativePaint.applyForWindow(color: Color, shadow: Color) {
  this.style = android.graphics.Paint.Style.FILL
  this.strokeCap = android.graphics.Paint.Cap.SQUARE
  this.color = color.toArgb()
  this.alpha = 255
  setShadowLayer(windowShadowRadius.toPx(), 0f, 2.dp.toPx(), shadow.toArgb())
}

context(DrawScope)
fun NativePaint.applyForSlat(color: Color, shadow: Color) {
  this.style = android.graphics.Paint.Style.FILL
  this.strokeCap = android.graphics.Paint.Cap.SQUARE
  this.color = color.toArgb()
  this.alpha = 255
  setShadowLayer(slatShadowRadius.toPx(), 0f, 1.5.dp.toPx(), shadow.toArgb())
}

context(DrawScope)
fun NativePaint.applyForShadow(color: Color) {
  this.style = android.graphics.Paint.Style.FILL
  this.strokeCap = android.graphics.Paint.Cap.SQUARE
  this.color = color.toArgb()
  this.alpha = 61
  setShadowLayer(0f, 0f, 0f, Color.White.toArgb())
}

context(DrawScope)
fun NativePaint.applyForAwningMarker(color: Color) {
  this.style = android.graphics.Paint.Style.FILL
  this.strokeCap = android.graphics.Paint.Cap.SQUARE
  this.color = color.toArgb()
  this.alpha = 100
  setShadowLayer(0f, 0f, 0f, Color.White.toArgb())
}
