package org.supla.android.features.details.windowdetail.base.ui.windowview
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import org.supla.android.features.details.windowdetail.base.data.WindowState
import org.supla.android.features.details.windowdetail.base.ui.applyForWindow

abstract class WindowDrawerBase<D : WindowDimensBase, S : WindowState, C : WindowColorsBase> {

  protected val paint = Paint().asFrameworkPaint()
  protected val path = Path()

  context(DrawScope)
  protected abstract fun drawShadowingElements(windowState: S, runtimeDimens: D, colors: C)

  context(DrawScope)
  protected abstract fun drawMarkers(windowState: S, runtimeDimens: D, colors: C)

  context(DrawScope)
  fun drawWindow(runtimeDimens: D, colors: C, windowState: S) {
    val windowFrameRadius = WINDOW_FRAME_RADIUS.dp.toPx().times(runtimeDimens.scale)

    paint.applyForWindow(colors.window, colors.shadow)
    drawContext.canvas.nativeCanvas.drawRoundRect(
      runtimeDimens.windowRect.left,
      runtimeDimens.windowRect.top,
      runtimeDimens.windowRect.right,
      runtimeDimens.windowRect.bottom,
      windowFrameRadius,
      windowFrameRadius,
      paint
    )

    drawGlasses(runtimeDimens, colors)
    drawShadowingElements(windowState, runtimeDimens, colors)

    paint.applyForWindow(colors.window, colors.shadow)
    drawContext.canvas.nativeCanvas.drawRect(
      runtimeDimens.topLineRect.left,
      runtimeDimens.topLineRect.top,
      runtimeDimens.topLineRect.right,
      runtimeDimens.topLineRect.bottom,
      paint
    )

    drawMarkers(windowState, runtimeDimens, colors)
  }

  context(DrawScope)
  private fun drawGlasses(dimens: D, colors: C) {
    val glassHorizontalMargin = WindowDimens.GLASS_HORIZONTAL_MARGIN.times(dimens.scale)
    val glassVerticalMargin = WindowDimens.GLASS_VERTICAL_MARGIN.times(dimens.scale)
    val glassMiddleMargin = WindowDimens.GLASS_MIDDLE_MARGIN.times(dimens.scale)
    val singleGlassWidth = dimens.windowRect.width
      .minus(glassHorizontalMargin.times(2))
      .minus(glassMiddleMargin)
      .div(2f)

    val brush = Brush.verticalGradient(listOf(colors.glassTop, colors.glassBottom))
    val left = dimens.windowRect.left.plus(glassHorizontalMargin)
    val height = dimens.canvasRect.height.minus(glassVerticalMargin.times(2))

    drawRect(
      brush = brush,
      topLeft = Offset(left, glassVerticalMargin),
      size = Size(singleGlassWidth, height)
    )
    drawRect(
      brush = brush,
      topLeft = Offset(
        left.plus(singleGlassWidth).plus(glassMiddleMargin),
        glassVerticalMargin
      ),
      size = Size(singleGlassWidth, height)
    )
  }
}
