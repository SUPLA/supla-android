package org.supla.android.ui
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

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DOT_SIZE = 8.dp

@Composable
fun DotsLoadingIndicator(
  modifier: Modifier = Modifier,
  grayColor: Color = MaterialTheme.colorScheme.outline,
  greenColor: Color = MaterialTheme.colorScheme.primary,
  delayBetweenDots: Int = 150
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    repeat(3) { index ->
      val alpha by dotAlpha(index, delayBetweenDots)

      Box(
        modifier = Modifier
          .size(DOT_SIZE)
          .background(
            color = grayColor,
            shape = CircleShape
          )
      ) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(
              color = greenColor.copy(alpha = alpha),
              shape = CircleShape
            )
        )
      }
    }
  }
}

@Composable
private fun dotAlpha(index: Int, delayBetweenDots: Int): State<Float> {
  val infiniteTransition = rememberInfiniteTransition(label = "dots")

  return infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = delayBetweenDots * 6

        0f at 0
        0f at delayBetweenDots * index
        1f at delayBetweenDots + (delayBetweenDots * index)
        1f at (delayBetweenDots * 3) + (delayBetweenDots * index)
        0f at (delayBetweenDots * 4) + (delayBetweenDots * index)
        0f at durationMillis
      },
      repeatMode = RepeatMode.Restart
    ),
    label = "dotAlpha$index"
  )
}
