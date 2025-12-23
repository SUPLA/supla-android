package org.supla.android.features.details.rgbanddimmer.rgb.ui
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import org.supla.android.extensions.HsvColor
import org.supla.android.extensions.exactly
import org.supla.android.extensions.isNull
import org.supla.android.features.details.rgbanddimmer.common.LinearColorSelector
import org.supla.android.features.details.rgbanddimmer.common.OUTER_SURFACE_WIDTH
import org.supla.android.features.details.rgbanddimmer.common.drawMarkerPoint
import org.supla.android.features.details.rgbanddimmer.common.drawSelectorPoint
import org.supla.android.tools.SuplaSizeClassPreview
import org.supla.core.shared.extensions.ifTrue
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ColorPickerComponent(
  color: HsvColor?,
  onColorSelected: (HsvColor) -> Unit,
  modifier: Modifier = Modifier,
  markers: List<HsvColor> = emptyList(),
  enabled: Boolean = true,
  onColorSelectionStarted: () -> Unit = {},
  onColorSelecting: (HsvColor) -> Unit = {}
) {
  var hsv by remember { mutableStateOf<HsvColor?>(null) }

  LaunchedEffect(color) {
    hsv = color
  }

  SelectorLayout(
    modifier = modifier.height(IntrinsicSize.Min),
  ) {
    // Color Wheel
    CircularColorSelector(
      currentColor = hsv?.fullBrightnessColor,
      currentHue = hsv?.hue,
      currentSaturation = hsv?.saturation,
      enabled = enabled,
      markers = hsv.isNull.ifTrue { markers } ?: emptyList(),
      onDragStart = onColorSelectionStarted,
      onDrag = { newHue, newSaturation ->
        val color = hsv?.copy(hue = newHue, saturation = newSaturation) ?: HsvColor(newHue, newSaturation, 1f)
        hsv = color
        onColorSelecting(color)
      },
      onDragEnd = {
        hsv?.let { onColorSelected(it) }
      }
    )

    LinearColorSelector(
      value = hsv?.value,
      selectedColor = hsv?.color,
      enabled = enabled,
      startColor = hsv?.fullBrightnessColor ?: Color.White,
      valueMarkers = hsv.isNull.ifTrue { markers.map { it.value } } ?: emptyList(),
      onValueChangeStarted = onColorSelectionStarted,
      onValueChanging = {
        // Setting brightness to 0 is not allowed. If the user wants turn off the dimmer
        // should click on turn off button
        val brightness = max(0.01f, it)
        val color = hsv?.copy(value = brightness) ?: HsvColor(1f, 1f, brightness)
        hsv = color
        onColorSelecting(color)
      },
      onValueChanged = {
        hsv?.let { onColorSelected(it) }
      },
      modifier = Modifier
        .width(40.dp) // Fixed width for the slider
        .fillMaxHeight()
    )
  }
}

@Composable
private fun CircularColorSelector(
  currentColor: Color?,
  currentSaturation: Float?,
  currentHue: Float?,
  enabled: Boolean = true,
  markers: List<HsvColor> = emptyList(),
  onDragStart: () -> Unit = {},
  onDrag: (Float, Float) -> Unit = { _, _ -> },
  onDragEnd: () -> Unit = {}
) {
  val surfaceColor = MaterialTheme.colorScheme.surface

  Canvas(
    modifier = Modifier
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { onDragStart() },
          onDragEnd = {
            if (enabled) {
              onDragEnd()
            }
          },
          onDrag = { change, _ ->
            if (enabled) {
              calculateHueAndSaturation(change.position).let { (hue, saturation) ->
                onDrag(hue, saturation)
              }
            }
          }
        )
      }
      .pointerInput(Unit) {
        detectTapGestures {
          if (enabled) {
            calculateHueAndSaturation(it).let { (hue, saturation) ->
              onDrag(hue, saturation)
            }
            onDragEnd()
          }
        }
      }
  ) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val minSideLength = minOf(centerX, centerY)
    val radius = minSideLength - OUTER_SURFACE_WIDTH.toPx()

    drawIntoCanvas { _ ->
      // Draw surface around
      drawCircle(
        color = surfaceColor,
        radius = minSideLength,
        center = Offset(centerX, centerY)
      )

      // Draw Hue wheel (Sweep Gradient)
      val hueGradient = Brush.sweepGradient(
        colors = listOf(
          Color.Red,
          Color.Yellow,
          Color.Green,
          Color.Cyan,
          Color.Blue,
          Color.Magenta,
          Color.Red
        ),
        center = Offset(centerX, centerY)
      )
      drawCircle(
        brush = hueGradient,
        radius = radius,
        center = Offset(centerX, centerY)
      )

      // Draw Value/Saturation overlay
      val saturationOverlayBrush = Brush.radialGradient(
        colors = listOf(Color.White, Color.Transparent),
        center = Offset(centerX, centerY),
        radius = radius
      )
      drawCircle(
        brush = saturationOverlayBrush,
        radius = radius,
        center = Offset(centerX, centerY)
      )

      markers.forEach {
        val selectorRadius = radius * it.saturation
        val selectorAngle = it.hue.toRadians()
        val selectorX = centerX + selectorRadius * cos(selectorAngle)
        val selectorY = centerY + selectorRadius * sin(selectorAngle)

        drawMarkerPoint(Offset(selectorX, selectorY))
      }

      // 4. Draw Selector
      if (enabled && currentSaturation != null && currentHue != null && currentColor != null) {
        val selectorRadius = radius * currentSaturation
        val selectorAngle = currentHue.toRadians()
        val selectorX = centerX + selectorRadius * cos(selectorAngle)
        val selectorY = centerY + selectorRadius * sin(selectorAngle)

        drawSelectorPoint(
          position = Offset(selectorX, selectorY),
          color = currentColor
        )
      }
    }
  }
}

@Composable
private fun SelectorLayout(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val configuration = LocalWindowInfo.current
  val screenWidth = with(LocalDensity.current) { configuration.containerSize.width.dp.toPx().toInt() }
  val screenHeight = with(LocalDensity.current) { configuration.containerSize.height.dp.toPx().toInt() }

  Layout(modifier = modifier, content = content) { measurables, constraints ->

    val possibleWidth = min(constraints.maxWidth, screenWidth)
    val possibleHeight = min(constraints.maxHeight, screenHeight)

    val brightnessSelectorWidth = 40.dp.toPx().toInt()
    val spacing = 24.dp.toPx().toInt()

    val colorSelectorWidth =
      (possibleHeight > possibleWidth + brightnessSelectorWidth + spacing).ifTrue {
        possibleWidth - brightnessSelectorWidth - spacing
      } ?: possibleHeight

    val colorSelector = measurables[0]
    val brightnessSelector = measurables[1]
    val colorSelectorPlaceable = colorSelector.measure(constraints.exactly(colorSelectorWidth, colorSelectorWidth))
    val brightnessSelectorPlaceable = brightnessSelector.measure(constraints.exactly(brightnessSelectorWidth, colorSelectorWidth))

    val horizontalSpace = (possibleWidth - colorSelectorWidth - brightnessSelectorWidth - spacing) / 2
    val verticalSpace = (possibleHeight - colorSelectorWidth) / 2

    layout(possibleWidth, colorSelectorWidth) {
      colorSelectorPlaceable.placeRelative(horizontalSpace, verticalSpace)
      brightnessSelectorPlaceable.placeRelative(horizontalSpace + spacing + colorSelectorWidth, verticalSpace)
    }
  }
}

private fun PointerInputScope.calculateHueAndSaturation(offset: Offset): Pair<Float, Float> {
  val centerX = size.width / 2f
  val centerY = size.height / 2f
  val radius = minOf(centerX, centerY) - OUTER_SURFACE_WIDTH.toPx() // Account for stroke

  val dx = offset.x - centerX
  val dy = offset.y - centerY
  val distance = min(radius, sqrt(dx * dx + dy * dy))

  val angle = atan2(dy, dx).toDegrees()
  return Pair((angle + 360) % 360, distance / radius)
}

private fun Float.toDegrees(): Float = this * 180f / PI.toFloat()
private fun Float.toRadians(): Float = this * PI.toFloat() / 180f

@SuplaSizeClassPreview
@Composable
fun PreviewColorPickerComponent() {
  var color by remember { mutableStateOf(HsvColor()) } // Start with red

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    ColorPickerComponent(
      color = color,
      onColorSelected = { hsv ->
        color = hsv
        println("Selected color: ${color.fullBrightnessColor}, brightness: ${color.value}")
      }
    )
    Spacer(modifier = Modifier.height(16.dp))
    // You could add a Text or a Box here to display the selected color
    Box(
      modifier = Modifier
        .size(60.dp)
        .background(color.color)
    )
  }
}
