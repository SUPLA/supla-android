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

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.Shadow
import org.supla.android.core.ui.theme.SuplaTheme

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlatTiltSlider(
  value: Float,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
  steps: Int = 0,
  slatsTiltDegrees: Float = 0f,
  enabled: Boolean = true,
  onValueChange: (Float) -> Unit = {},
  onValueChangeFinished: (Float) -> Unit = {}
) {
  val interactionSource = remember { MutableInteractionSource() }
  Slider(
    value = value,
    valueRange = valueRange,
    steps = steps,
    enabled = enabled,
    onValueChange = onValueChange,
    onValueChangeFinished = { onValueChangeFinished(value) },
    interactionSource = interactionSource,
    thumb = { Thumb(interactionSource = interactionSource, degrees = slatsTiltDegrees, enabled = enabled) },
    track = { Track(steps, enabled = enabled) },
    modifier = modifier,
  )
}

@Composable
private fun Thumb(
  interactionSource: MutableInteractionSource,
  degrees: Float,
  enabled: Boolean
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

  val slatDegrees = SlatTiltSliderDimens.trimAngle(degrees)

  Box(
    Modifier
      .size(SlatTiltSliderDimens.thumbSize)
      .indication(
        interactionSource = interactionSource,
        indication = rememberRipple(
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
        color = MaterialTheme.colors.surface,
        shape = CircleShape
      )
  ) {
    if (enabled) {
      Column(modifier = Modifier.align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(
          modifier = Modifier
            .rotate(slatDegrees)
            .width(12.dp)
            .height(1.dp)
            .background(colorResource(id = R.color.gray), RoundedCornerShape(0.5.dp))
        )
        Box(
          modifier = Modifier
            .rotate(slatDegrees)
            .width(12.dp)
            .height(1.dp)
            .background(colorResource(id = R.color.gray), RoundedCornerShape(0.5.dp))
        )
        Box(
          modifier = Modifier
            .rotate(slatDegrees)
            .width(12.dp)
            .height(1.dp)
            .background(colorResource(id = R.color.gray), RoundedCornerShape(0.5.dp))
        )
      }
    }
  }
}

@Composable
private fun Track(steps: Int, enabled: Boolean) {
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
    for (i in 0 until steps - 2) {
      Spacer(modifier = Modifier.weight(1f))
      Box(
        modifier = Modifier
          .size(SlatTiltSliderDimens.trackPointSize)
          .background(colorResource(id = R.color.on_primary), shape = CircleShape)
      )
    }
    Spacer(modifier = Modifier.weight(1f))
  }
}

@Composable
@Preview
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
      SlatTiltSlider(value = 0.2f)
      SlatTiltSlider(value = 2f, valueRange = 1f..10f, steps = 10)
      SlatTiltSlider(value = 55f)
      SlatTiltSlider(value = 55f, enabled = false)
    }
  }
}
