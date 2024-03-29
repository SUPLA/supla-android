package org.supla.android.features.details.rollershutterdetail.general.ui.roofwindow
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

internal object RoofWindowDimensBuilder {

  fun windowJambPoints(
    canvasRect: Rect,
    windowFrameWidth: Float,
    windowTopCoverWidth: Float
  ): List<Offset> {
    val frameAndCoverWidth = windowFrameWidth + windowTopCoverWidth
    val windowLeftSide = canvasRect.left + canvasRect.width / -2f
    val windowRightSide = canvasRect.left + canvasRect.width / 2f
    val windowTopSide = canvasRect.top + canvasRect.height / -2f
    val windowBottomSide = canvasRect.top + canvasRect.height / 2f

    return listOf(
      Offset(windowLeftSide, canvasRect.top),
      Offset(windowLeftSide, windowTopSide),
      Offset(windowRightSide, windowTopSide),
      Offset(windowRightSide, windowBottomSide),
      Offset(windowLeftSide, windowBottomSide),
      Offset(windowLeftSide + windowFrameWidth, windowBottomSide - windowFrameWidth),
      Offset(windowRightSide - windowFrameWidth, windowBottomSide - windowFrameWidth),
      Offset(windowRightSide - windowFrameWidth, canvasRect.top),
      Offset(windowRightSide - frameAndCoverWidth, canvasRect.top),
      Offset(windowRightSide - frameAndCoverWidth, canvasRect.top),
      Offset(windowRightSide - frameAndCoverWidth, windowTopSide + frameAndCoverWidth),
      Offset(windowLeftSide + frameAndCoverWidth, windowTopSide + frameAndCoverWidth),
      Offset(windowLeftSide + frameAndCoverWidth, canvasRect.top)
    )
  }

  fun windowCoverableJambPoints(
    canvasRect: Rect,
    windowFrameWidth: Float,
  ): List<Offset> {
    val windowLeftSide = canvasRect.left + canvasRect.width / -2f
    val windowBottomSide = canvasRect.top + canvasRect.height / 2f

    return listOf(
      Offset(windowLeftSide + windowFrameWidth, canvasRect.top),
      Offset(windowLeftSide + windowFrameWidth, windowBottomSide - windowFrameWidth),
      Offset(windowLeftSide, windowBottomSide),
      Offset(windowLeftSide, canvasRect.top),
    )
  }

  fun windowSashOutsidePoints(
    canvasRect: Rect,
    windowFrameWidth: Float
  ): List<Offset> {
    val windowLeftSide = canvasRect.left + canvasRect.width / -2f
    val windowRightSide = canvasRect.left + canvasRect.width / 2f
    val windowTopSide = canvasRect.top + canvasRect.height / -2f
    val windowBottomSide = canvasRect.top + canvasRect.height / 2f

    return listOf(
      Offset(windowLeftSide + windowFrameWidth, windowTopSide + windowFrameWidth),
      Offset(windowRightSide - windowFrameWidth, windowTopSide + windowFrameWidth),
      Offset(windowRightSide - windowFrameWidth, windowBottomSide - windowFrameWidth),
      Offset(windowLeftSide + windowFrameWidth, windowBottomSide - windowFrameWidth)
    )
  }

  fun windowSashInsidePoints(
    canvasRect: Rect,
    windowFrameWidth: Float,
    windowTopCoverWidth: Float
  ): List<Offset> {
    val frameAndCoverWidth = windowFrameWidth + windowTopCoverWidth
    val windowLeftSide = canvasRect.left + canvasRect.width / -2f
    val windowRightSide = canvasRect.left + canvasRect.width / 2f
    val windowTopSide = canvasRect.top + canvasRect.height / -2f
    val windowBottomSide = canvasRect.top + canvasRect.height / 2f

    return listOf(
      Offset(windowLeftSide + frameAndCoverWidth, windowTopSide + frameAndCoverWidth),
      Offset(windowRightSide - frameAndCoverWidth, windowTopSide + frameAndCoverWidth),
      Offset(windowRightSide - frameAndCoverWidth, windowBottomSide - frameAndCoverWidth),
      Offset(windowLeftSide + frameAndCoverWidth, windowBottomSide - frameAndCoverWidth)
    )
  }

  fun framePoints(
    canvasRect: Rect
  ): List<Offset> {
    val windowLeftSide = canvasRect.left + canvasRect.width / -2f
    val windowRightSide = canvasRect.left + canvasRect.width / 2f
    val windowTopSide = canvasRect.top + canvasRect.height / -2f
    val windowBottomSide = canvasRect.top + canvasRect.height / 2f

    return listOf(
      Offset(windowLeftSide, windowTopSide),
      Offset(windowRightSide, windowTopSide),
      Offset(windowRightSide, windowBottomSide),
      Offset(windowLeftSide, windowBottomSide)
    )
  }
}
