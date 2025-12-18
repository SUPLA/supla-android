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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaComponentPreview

// Dimensions
val OUTER_SURFACE_WIDTH = 5.dp

@Composable
fun LinearColorSelector(
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

  fun PointerInputScope.updateValue(offset: Offset) {
    val position = 1f - (offset.y / size.height).coerceIn(0f, 1f)
    onValueChanging(position)
  }

  Canvas(
    modifier = modifier
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { onValueChangeStarted() },
          onDragEnd = {
            if (enabled) {
              onValueChanged()
            }
          },
          onDrag = { change, _ ->
            if (enabled) {
              updateValue(change.position)
            }
          }
        )
      }
      .pointerInput(Unit) {
        detectTapGestures {
          if (enabled) {
            updateValue(it)
            onValueChanged()
          }
        }
      }
  ) {
    val outerMargin = OUTER_SURFACE_WIDTH.toPx()
    val sliderWidth = size.width - outerMargin * 2
    val sliderHeight = size.height - outerMargin * 2
    val sliderSize = Size(sliderWidth, sliderHeight)

    // Background gradient for Value: Top = Full Hue/Sat/Value, Bottom = Black
    val valueGradientBrush = Brush.verticalGradient(
      colors = listOf(startColor, endColor),
      startY = 0f,
      endY = sliderHeight
    )
    drawRoundRect(
      color = surfaceColor,
      size = size,
      cornerRadius = CornerRadius(9.dp.toPx())
    )
    drawRoundRect(
      brush = valueGradientBrush,
      size = sliderSize,
      topLeft = Offset(outerMargin, outerMargin),
      cornerRadius = CornerRadius(5.dp.toPx())
    )

    valueMarkers.forEach {
      val selectorRadius = SELECTOR_RADIUS.toPx()
      val selectorY = outerMargin + selectorRadius + (1f - it) * (sliderHeight - selectorRadius * 2)
      drawMarkerPoint(Offset(center.x, selectorY))
    }

    // Selector for current Value
    if (enabled && value != null && selectedColor != null) {
      val selectorRadius = SELECTOR_RADIUS.toPx()
      val selectorY = outerMargin + selectorRadius + (1f - value) * (sliderHeight - selectorRadius * 2)
      drawSelectorPoint(
        position = Offset(center.x, selectorY),
        color = selectedColor
      )
    }
  }
}

@SuplaComponentPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.small)
    ) {
      LinearColorSelector(
        value = 0.5f,
        selectedColor = Color.Gray,
        modifier = Modifier.height(350.dp).width(40.dp)
      )
      LinearColorSelector(
        value = null,
        selectedColor = Color.Gray,
        modifier = Modifier.height(350.dp).width(40.dp)
      )
      LinearColorSelector(
        value = null,
        selectedColor = Color.Gray,
        modifier = Modifier.height(350.dp).width(40.dp),
        valueMarkers = listOf(0f, 0.5f, 1f)
      )
    }
  }
}
