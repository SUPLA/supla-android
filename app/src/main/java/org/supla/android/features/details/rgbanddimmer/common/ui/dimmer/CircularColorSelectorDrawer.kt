package org.supla.android.features.details.rgbanddimmer.common.ui.dimmer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import org.supla.android.features.details.rgbanddimmer.common.ui.OUTER_SURFACE_WIDTH
import org.supla.android.features.details.rgbanddimmer.common.ui.SELECTOR_RADIUS
import org.supla.android.features.details.rgbanddimmer.common.ui.drawMarkerPoint
import org.supla.android.features.details.rgbanddimmer.common.ui.drawSelectorPoint
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

private val TRACK_WIDTH = 30.dp

class CircularColorSelectorDrawer(
  private val outerMarginDp: Dp,
  private val surfaceColor: Color,
  private val onValueChangeStarted: () -> Unit = {},
  private val onValueChanging: (Float) -> Unit = {},
  private val onValueChanged: () -> Unit = {}
) {

  private var isActiveGesture: Boolean = false
  private var gesturePrevValue: Float? = null
  private var initialValue: Float? = null
  private var valueDiff: Float? = null

  fun handleDragStart(scope: PointerInputScope, enabled: Boolean, startPosition: Offset) {
    with(scope) {
      if (!enabled) return
      val center = size.center
      val outerMargin = outerMarginDp.toPx()
      val trackWidthPx = TRACK_WIDTH.toPx()
      val minDim = min(size.width, size.height)
      val outerRadius = minDim / 2f
      val ringRadius = outerRadius - outerMargin - trackWidthPx / 2f

      isActiveGesture = isInRing(
        touch = startPosition,
        center = center,
        ringRadius = ringRadius,
        trackWidthPx = trackWidthPx,
        slopPx = OUTER_SURFACE_WIDTH.toPx()
      )

      if (isActiveGesture) {
        initialValue = null
        gesturePrevValue = null
        valueDiff = 0f
        onValueChangeStarted()
        updateValueFromOffset(startPosition, center)
      }
    }
  }

  fun handleDragEnd(enabled: Boolean) {
    if (enabled && isActiveGesture) onValueChanged()
    initialValue = null
    isActiveGesture = false
    gesturePrevValue = null
  }

  fun handleDragCancel() {
    initialValue = null
    isActiveGesture = false
    gesturePrevValue = null
  }

  fun handleDrag(scope: PointerInputScope, change: PointerInputChange, enabled: Boolean) {
    if (!enabled || !isActiveGesture) return

    with(scope) {
      val center = size.center
      updateValueFromOffset(change.position, center)
    }
  }

  fun handleTap(scope: PointerInputScope, enabled: Boolean, position: Offset) {
    if (!enabled) return
    with(scope) {
      val center = size.center
      val outerMargin = outerMarginDp.toPx()
      val trackWidthPx = TRACK_WIDTH.toPx()
      val minDim = min(size.width, size.height)
      val outerRadius = minDim / 2f
      val ringRadius = outerRadius - outerMargin - trackWidthPx / 2f

      val slopPx = SELECTOR_RADIUS.toPx()
      if (!isInRing(position, center, ringRadius, trackWidthPx, slopPx)) return

      gesturePrevValue = null
      onValueChanging(getValueFromPosition(position, center))
      onValueChanged()
    }
  }

  fun drawCircularSelector(
    scope: DrawScope,
    value: Float?,
    selectedColor: Color?,
    colors: List<Pair<Float, Color>>,
    valueMarkers: List<Float> = emptyList(),
    enabled: Boolean = true,
  ) {
    with(scope) {
      val outerMargin = outerMarginDp.toPx()
      val trackWidth = TRACK_WIDTH.toPx()
      val minDim = min(size.width, size.height)
      val outerRadius = minDim / 2f
      val center = Offset(size.width / 2f, size.height / 2f)

      val ringRadius = outerRadius - outerMargin - trackWidth / 2f
      if (ringRadius <= 0f) return

      drawCircle(
        color = surfaceColor,
        radius = outerRadius - outerMargin - trackWidth / 2f,
        center = center,
        style = Stroke(width = trackWidth + OUTER_SURFACE_WIDTH.toPx() * 2f)
      )

      rotate(270f) {
        val sweep = Brush.sweepGradient(
          colorStops = colors.toTypedArray(),
          center = center
        )
        drawCircle(
          brush = sweep,
          radius = ringRadius,
          center = center,
          style = Stroke(width = trackWidth)
        )
      }

      valueMarkers.forEach { markerValue ->
        val a = valueToAngle(markerValue)
        val p = pointOnCircle(center, ringRadius, a)
        drawMarkerPoint(p)
      }

      if (enabled && value != null && selectedColor != null) {
        val a = valueToAngle(value)
        val p = pointOnCircle(center, ringRadius, a)
        drawSelectorPoint(position = p, color = selectedColor)
      }
    }
  }

  private fun updateValueFromOffset(offset: Offset, center: IntOffset) {
    val newValue = getValueFromPosition(offset, center)

    if (initialValue == null) {
      initialValue = newValue
    }

    val prev = gesturePrevValue
    if (prev != null) {
      val currentDiff = prev - newValue
      valueDiff = valueDiff?.let { it + calculateDiffIncrement(currentDiff, newValue, prev) }

      val currentValue = initialValue?.minus(valueDiff ?: 0f)
      if (currentValue != null) {
        if (currentValue > 1) {
          onValueChanging(1f)
        } else if (currentValue < 0) {
          onValueChanging(0f)
        } else {
          onValueChanging(currentValue)
        }

        valueDiff = valueDiff.coerceValue(currentValue)
      }
    }

    gesturePrevValue = newValue
  }
}

private fun calculateDiffIncrement(currentDiff: Float, currentValue: Float, previousValue: Float): Float =
  if ((currentDiff > 0f && currentDiff < 0.8f) || (currentDiff < 0f && currentDiff > -0.8f)) {
    currentDiff
  } else if (currentDiff > 0f) {
    previousValue - 1 - currentValue
  } else if (currentDiff < 0f) {
    previousValue + 1 - currentValue
  } else {
    0f
  }

private fun Float?.coerceValue(currentValue: Float): Float? =
  this?.let {
    if (currentValue < -1) {
      it - 1f
    } else if (currentValue > 2) {
      it + 1f
    } else {
      it
    }
  }

private fun getValueFromPosition(offset: Offset, center: IntOffset): Float {
  val dx = offset.x - center.x
  val dy = offset.y - center.y
  val angleDeg = atan2(dy, dx) * (180f / PI.toFloat())
  return angleToValue(angleDeg)
}

private fun angleToValue(angleDeg: Float): Float {
  val normalizedFromTop = (angleDeg + 90f).normalizeDegrees() // 0..360
  return (normalizedFromTop / 360f).coerceIn(0f, 1f)
}

private fun Float.normalizeDegrees(): Float {
  var d = this % 360f
  if (d < 0f) d += 360f
  return d
}

private fun valueToAngle(v: Float): Float {
  return -90f + (v.coerceIn(0f, 1f)) * 360f
}

private fun pointOnCircle(center: Offset, radius: Float, angleDeg: Float): Offset {
  val r = angleDeg * (PI.toFloat() / 180f)
  return Offset(
    x = center.x + cos(r) * radius,
    y = center.y + sin(r) * radius
  )
}

private fun isInRing(touch: Offset, center: IntOffset, ringRadius: Float, trackWidthPx: Float, slopPx: Float): Boolean {
  val d = hypot(touch.x - center.x, touch.y - center.y)
  val inner = ringRadius - trackWidthPx / 2f - slopPx
  val outer = ringRadius + trackWidthPx / 2f + slopPx
  return d in inner..outer
}
