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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.ui.MoveState

interface WindowDimensBase {
  val canvasRect: Rect
  val topLineRect: Rect
  val windowRect: Rect
  val scale: Float

  val movementLimit: Float
    get() = canvasRect.height - topLineRect.height

  fun getPositionFromState(state: MoveState): Offset? {
    val (initial) = guardLet(state.initialPoint) { return null }

    val horizontalDiffAsPercentage =
      state.lastPoint.x.minus(initial.x)
        .div(windowRect.width.div(2))
        .times(100f)

    val verticalDiffAsPercentage =
      state.lastPoint.y.minus(initial.y)
        .div(movementLimit)
        .times(100f)

    val x = state.initialHorizontalPercentage.plus(horizontalDiffAsPercentage).let {
      // Trim to 0% - 100%
      if (it < 0f) {
        0f
      } else if (it > 100f) {
        100f
      } else {
        it
      }
    }

    val y = state.initialVerticalPercentage.plus(verticalDiffAsPercentage).let {
      // Trim to 0% - 100%
      if (it < 0f) {
        0f
      } else if (it > 100f) {
        100f
      } else {
        it
      }
    }

    return if (state.horizontalAllowed) {
      Offset(x, y)
    } else {
      Offset(state.initialHorizontalPercentage, y)
    }
  }

  companion object {
    fun canvasRect(viewSize: IntSize): Rect {
      val size = getSize(viewSize = viewSize)
      return Rect(
        Offset(viewSize.width.minus(size.width).div(2), 0f),
        size
      )
    }

    fun windowRect(scale: Float, canvasRect: Rect, topLineRect: Rect): Rect {
      val windowHorizontalMargin = WindowDimens.WINDOW_HORIZONTAL_MARGIN.times(scale)
      val windowTop = topLineRect.height.div(2)
      val windowSize = Size(
        canvasRect.width.minus(windowHorizontalMargin.times(2)),
        canvasRect.height.minus(windowTop)
      )
      val windowOffset = Offset(canvasRect.left.plus(windowHorizontalMargin), windowTop)

      return Rect(windowOffset, windowSize)
    }

    private fun getSize(viewSize: IntSize): Size {
      val canvasRatio = viewSize.width / viewSize.height.toFloat()
      return if (canvasRatio > WindowDimens.RATIO) {
        Size(viewSize.height.times(WindowDimens.RATIO), viewSize.height.toFloat())
      } else {
        Size(viewSize.width.toFloat(), viewSize.width.div(WindowDimens.RATIO))
      }
    }
  }
}
