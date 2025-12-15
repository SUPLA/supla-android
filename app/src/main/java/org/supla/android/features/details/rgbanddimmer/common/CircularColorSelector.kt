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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.ComponentPreview
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

private val TRACK_WIDTH = 24.dp

@Composable
fun CircularColorSelector(
  value: Float?,
  selectedColor: Color?,
  modifier: Modifier = Modifier,
  valueMarkers: List<Float> = emptyList(),
  enabled: Boolean = true,
  startColor: Color = Color.White,
  endColor: Color = Color.Black,
  onValueChangeStarted: () -> Unit = {},
  onValueChanging: (Float) -> Unit = {},
  onValueChanged: () -> Unit = {}
) {
  val surfaceColor = MaterialTheme.colorScheme.surface

  var gesturePrevValue by remember { mutableStateOf<Float?>(null) }
  var isActiveGesture by remember { mutableStateOf(false) }

  fun updateValueFromOffset(offset: Offset, center: IntOffset) {
    val dx = offset.x - center.x
    val dy = offset.y - center.y
    val angleDeg = atan2(dy, dx) * (180f / PI.toFloat())
    var newValue = angleToValue(angleDeg)

    val prev = gesturePrevValue
    if (prev != null) {
      val nearHigh = prev > 0.85f
      val nearLow = prev < 0.15f
      val jumpedHighToLow = nearHigh && newValue < 0.15f
      val jumpedLowToHigh = nearLow && newValue > 0.85f

      if (jumpedHighToLow) newValue = 1f
      if (jumpedLowToHigh) newValue = 0f
    }

    gesturePrevValue = newValue
    onValueChanging(newValue)
  }

  Canvas(
    modifier = modifier
      .pointerInput(enabled) {
        detectDragGestures(
          onDragStart = { startPos ->
            if (!enabled) return@detectDragGestures
            val center = size.center
            val outerMargin = OUTER_SURFACE_WIDTH.toPx()
            val trackWidthPx = TRACK_WIDTH.toPx()
            val minDim = min(size.width, size.height)
            val outerRadius = minDim / 2f
            val ringRadius = outerRadius - outerMargin - trackWidthPx / 2f

            isActiveGesture = isInRing(
              touch = startPos,
              center = center,
              ringRadius = ringRadius,
              trackWidthPx = trackWidthPx,
              slopPx = SELECTOR_RADIUS.toPx()
            )

            if (isActiveGesture) {
              gesturePrevValue = value?.coerceIn(0f, 1f)
              onValueChangeStarted()
              updateValueFromOffset(startPos, center)
            }
          },
          onDragEnd = {
            if (enabled && isActiveGesture) onValueChanged()
            isActiveGesture = false
            gesturePrevValue = null
          },
          onDragCancel = {
            isActiveGesture = false
            gesturePrevValue = null
          },
          onDrag = { change, _ ->
            if (!enabled || !isActiveGesture) return@detectDragGestures
            val center = size.center
            updateValueFromOffset(change.position, center)
          }
        )
      }
      .pointerInput(enabled) {
        detectTapGestures { pos ->
          if (!enabled) return@detectTapGestures
          val center = size.center
          val outerMargin = OUTER_SURFACE_WIDTH.toPx()
          val trackWidthPx = TRACK_WIDTH.toPx()
          val minDim = min(size.width, size.height)
          val outerRadius = minDim / 2f
          val ringRadius = outerRadius - outerMargin - trackWidthPx / 2f

          val slopPx = SELECTOR_RADIUS.toPx()
          if (!isInRing(pos, center, ringRadius, trackWidthPx, slopPx)) return@detectTapGestures

          gesturePrevValue = null
          updateValueFromOffset(pos, center)
          onValueChanged()
        }
      }
  ) {
    val outerMargin = OUTER_SURFACE_WIDTH.toPx()
    val trackWidthPx = TRACK_WIDTH.toPx()

    val minDim = min(size.width, size.height)
    val outerRadius = minDim / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    val ringRadius = outerRadius - outerMargin - trackWidthPx / 2f
    if (ringRadius <= 0f) return@Canvas

    drawCircle(
      color = surfaceColor,
      radius = outerRadius - outerMargin - trackWidthPx / 2f,
      center = center,
      style = Stroke(width = trackWidthPx + outerMargin * 2f)
    )

    rotate(270f) {
      val sweep = Brush.sweepGradient(
        colors = listOf(endColor, startColor),
        center = center
      )
      drawCircle(
        brush = sweep,
        radius = ringRadius,
        center = center,
        style = Stroke(width = trackWidthPx)
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

@ComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(Distance.small),
      modifier = Modifier.background(Color.LightGray)
    ) {
      CircularColorSelector(
        value = 0.3f,
        selectedColor = Color.Gray,
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth()
      )
      CircularColorSelector(
        value = null,
        selectedColor = Color.Gray,
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth()
      )
      CircularColorSelector(
        value = null,
        selectedColor = Color.Gray,
        modifier = Modifier
          .height(350.dp)
          .fillMaxWidth(),
        valueMarkers = listOf(0f, 0.5f, 1f)
      )
    }
  }
}
