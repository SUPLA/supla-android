package org.supla.android.features.details.windowdetail.base.ui.facadeblinds
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
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Shadow
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.drawText
import org.supla.android.ui.views.texts.BodySmall

object SlatTiltSliderDimens {
  val thumbSize = 40.dp
  val trackHeight = 16.dp
  val trackPointSize = 8.dp

  private const val SLAT_MAX_ANGLE = 60f

  fun trimAngle(angle: Float) =
    SLAT_MAX_ANGLE
      .times(2)
      .times(angle)
      .div(180)
      .minus(SLAT_MAX_ANGLE)
}

data class SlatMarker(
  val value: Float, // value as percentage
  val tilt: Float? // tilt as degrees, may be not available
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlatTiltSlider(
  value: Float?,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
  steps: Int = 0,
  slatsTiltDegrees: Float? = 0f,
  enabled: Boolean = true,
  markers: List<SlatMarker>? = null,
  onValueChange: (Float) -> Unit = {},
  onValueChangeFinished: (Float) -> Unit = {}
) {
  val interactionSource = remember { MutableInteractionSource() }
  var lastValue: Float? = value
  Slider(
    value = value ?: 0f,
    valueRange = valueRange,
    steps = steps,
    enabled = enabled,
    onValueChange = {
      lastValue = it
      onValueChange(it)
    },
    onValueChangeFinished = { onValueChangeFinished(lastValue ?: 0f) },
    interactionSource = interactionSource,
    thumb = { Thumb(interactionSource, value, slatsTiltDegrees, enabled, visible = lastValue != null) },
    track = { Track(steps, enabled = enabled, markers) },
    modifier = modifier,
  )
}

@Composable
private fun Thumb(
  interactionSource: MutableInteractionSource,
  value: Float?,
  degrees: Float?,
  enabled: Boolean,
  visible: Boolean
) {
  val interactions = remember { mutableStateListOf<Interaction>() }
  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect { interaction ->
      when (interaction) {
        is PressInteraction.Press -> interactions.add(interaction)
        is PressInteraction.Release -> interactions.remove(interaction.press)
        is PressInteraction.Cancel -> interactions.remove(interaction.press)
        is DragInteraction.Start -> interactions.add(interaction)
        is DragInteraction.Stop -> interactions.remove(interaction.start)
        is DragInteraction.Cancel -> interactions.remove(interaction.start)
      }
    }
  }

  if (visible) {
    Box(
      Modifier
        .size(SlatTiltSliderDimens.thumbSize)
        .indication(
          interactionSource = interactionSource,
          indication = ripple(
            bounded = false,
            radius = SlatTiltSliderDimens.thumbSize.div(2)
          )
        )
        .hoverable(interactionSource = interactionSource)
        .let {
          if (enabled) {
            it.shadow(elevation = Shadow.elevation, shape = CircleShape)
          } else {
            it
          }
        }
        .background(
          color = MaterialTheme.colorScheme.surface,
          shape = CircleShape
        )
        .border(
          width = 1.dp,
          color = colorResource(id = R.color.disabled),
          shape = RoundedCornerShape(corner = CornerSize(SlatTiltSliderDimens.thumbSize.div(2)))
        )
    ) {
      if (enabled) {
        if (degrees != null) {
          val slatDegrees = SlatTiltSliderDimens.trimAngle(degrees)

          Column(modifier = Modifier.align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) {
              Box(
                modifier = Modifier
                  .rotate(slatDegrees)
                  .width(12.dp)
                  .height(1.dp)
                  .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(0.5.dp))
              )
            }
          }
        } else if (value != null) {
          BodySmall(
            text = ValuesFormatter.getPercentageString(value = value.div(100f)),
            modifier = Modifier.align(Alignment.Center)
          )
        }
      }
    }
  } else {
    Box(
      Modifier
        .size(SlatTiltSliderDimens.thumbSize)
        .indication(
          interactionSource = interactionSource,
          indication = ripple(
            bounded = false,
            radius = SlatTiltSliderDimens.thumbSize.div(2)
          )
        )
        .hoverable(interactionSource = interactionSource)
    )
  }
}

@Composable
private fun Track(steps: Int, enabled: Boolean, markers: List<SlatMarker>?) {
  val color = if (enabled) {
    colorResource(id = R.color.gray_light)
  } else {
    colorResource(id = R.color.gray_lighter)
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(SlatTiltSliderDimens.trackHeight)
      .background(color, shape = RoundedCornerShape(SlatTiltSliderDimens.trackHeight.div(2))),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val stepBackground = MaterialTheme.colorScheme.onPrimary
    val markerBackground = MaterialTheme.colorScheme.surface
    val markerBorder = if (enabled) colorResource(R.color.disabled) else MaterialTheme.colorScheme.outline
    val markerContent = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
    val textMeasurer = rememberTextMeasurer()

    Canvas(
      modifier = Modifier.fillMaxSize()
    ) {
      val distance = size.width.div(steps - 1)
      for (i in 1 until steps - 1) {
        drawCircle(
          color = stepBackground,
          radius = SlatTiltSliderDimens.trackPointSize.toPx().div(2),
          center = Offset(distance.times(i), center.y)
        )
      }

      markers?.forEach {
        drawMarker(
          backgroundColor = markerBackground,
          borderColor = markerBorder,
          contentColor = markerContent,
          position = it.value,
          degrees = it.tilt,
          textMeasurer = textMeasurer
        )
      }
    }
  }
}

private val markerLinesOffsets = listOf(4.dp, 0.dp, (-4).dp)
private fun DrawScope.drawMarker(
  backgroundColor: Color,
  borderColor: Color,
  contentColor: Color,
  position: Float,
  degrees: Float?,
  textMeasurer: TextMeasurer
) {
  val xOffset = size.width / 100 * position
  drawCircle(
    color = backgroundColor,
    radius = 12.dp.toPx(),
    center = Offset(xOffset, center.y)
  )
  drawCircle(
    color = borderColor,
    radius = 12.dp.toPx(),
    style = Stroke(width = 1.dp.toPx()),
    center = Offset(xOffset, center.y)
  )

  if (degrees != null) {
    val rotation = SlatTiltSliderDimens.trimAngle(degrees)
    markerLinesOffsets.forEach { lineOffset ->
      rotate(
        degrees = rotation,
        pivot = Offset(xOffset, center.y - lineOffset.toPx())
      ) {
        drawLine(
          color = contentColor,
          start = Offset(xOffset - 5.dp.toPx(), center.y - lineOffset.toPx()),
          end = Offset(xOffset + 5.dp.toPx(), center.y - lineOffset.toPx()),
          strokeWidth = 1.dp.toPx()
        )
      }
    }
  } else {
    drawText(
      text = "?",
      textMeasurer = textMeasurer,
      color = contentColor,
      center = Offset(xOffset, center.y)
    )
  }
}

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
      SlatTiltSlider(value = 0.2f)
      SlatTiltSlider(value = 2f, valueRange = 1f..10f, steps = 10)
      SlatTiltSlider(value = 55f)
      SlatTiltSlider(value = 55f, enabled = false)
      SlatTiltSlider(value = null, enabled = false, markers = previewMarkers)
      SlatTiltSlider(value = null, enabled = true, markers = previewMarkers)
      SlatTiltSlider(value = 55f, slatsTiltDegrees = null)
    }
  }
}

private val previewMarkers = listOf(
  SlatMarker(0f, 180f),
  SlatMarker(25f, 45f),
  SlatMarker(50f, 90f),
  SlatMarker(75f, 0f),
  SlatMarker(100f, 65f),
  SlatMarker(85f, null),
)
