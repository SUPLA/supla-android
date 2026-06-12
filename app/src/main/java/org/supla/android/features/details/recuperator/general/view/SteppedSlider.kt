package org.supla.android.features.details.recuperator.general.view
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.core.shared.infrastructure.LocalizedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SteppedSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  labels: List<LocalizedString>? = null,
  steps: Int = 3,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val backgroundColor = MaterialTheme.colorScheme.background
  val outlineColor = MaterialTheme.colorScheme.outline
  Column(modifier = modifier) {
    labels?.let {
      Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
      ) {
        it.forEach { label ->
          Text(
            text = label().uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
      val stepWidth = maxWidth.div(steps)

      Canvas(modifier = Modifier.matchParentSize()) {
        val y = size.height.div(2f)

        drawLine(
          color = outlineColor,
          start = Offset(2.dp.toPx(), y),
          end = Offset(size.width - 2.dp.toPx(), y),
          strokeWidth = 4.dp.toPx(),
          cap = StrokeCap.Round
        )

        drawLine(
          color = primaryColor,
          start = Offset(2.dp.toPx(), y),
          end = Offset(stepWidth.toPx(), y),
          strokeWidth = 4.dp.toPx(),
          cap = StrokeCap.Round
        )
      }

      Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..steps.minus(1).toFloat(),
        steps = steps.minus(2),
        modifier = Modifier
          .width(stepWidth.times(steps - 1))
          .align(Alignment.Center),
        track = { sliderState ->
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(32.dp)
          ) {
            val distanceBetweenSteps = size.width.div(steps - 1)
            val y = size.height.div(2f)

            drawLine(
              color = outlineColor,
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = 4.dp.toPx(),
              cap = StrokeCap.Round
            )
            val selectedWidth = size.width.times(sliderState.value).div(steps - 1)
            drawLine(
              color = primaryColor,
              start = Offset(0f, y),
              end = Offset(selectedWidth, y),
              strokeWidth = 4.dp.toPx(),
              cap = StrokeCap.Round
            )

            repeat(steps) { index ->
              val x = distanceBetweenSteps * index

              drawCircle(
                color = backgroundColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
              )

              drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
              )
            }
          }
        },
        thumb = {
          Canvas(
            modifier = Modifier
              .size(32.dp)
          ) {
            drawCircle(color = primaryColor.copy(alpha = 0.4f))
            drawCircle(color = primaryColor, radius = 12.dp.toPx())
          }
        }
      )
    }
  }
}

@SuplaPreview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
      var value by remember { mutableFloatStateOf(2f) }

      SteppedSlider(
        value = value,
        onValueChange = { value = it },
        steps = 2,
        modifier = Modifier.fillMaxWidth()
      )
      SteppedSlider(
        value = value,
        onValueChange = { value = it },
        steps = 3,
        modifier = Modifier.fillMaxWidth()
      )
      SteppedSlider(
        value = value,
        onValueChange = { value = it },
        steps = 4,
        modifier = Modifier.fillMaxWidth(),
        labels = listOf(
          LocalizedString.Constant("Speed 1"),
          LocalizedString.Constant("Speed 2"),
          LocalizedString.Constant("Speed 3"),
          LocalizedString.Constant("Speed 4")
        )
      )
    }
  }
}
