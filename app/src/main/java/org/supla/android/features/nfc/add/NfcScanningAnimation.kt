package org.supla.android.features.nfc.add
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

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R

@Composable
fun NfcScanningAnimation(
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 4.dp,
) {
  val infinite = rememberInfiniteTransition(label = "nfc")

  val ringScale by infinite.animateFloat(
    initialValue = 0.75f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ringScale"
  )
  val ringAlpha by infinite.animateFloat(
    initialValue = 0.55f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ringAlpha"
  )

  val bob by infinite.animateFloat(
    initialValue = 0f,
    targetValue = -6f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bob"
  )

  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
  ) {
    PulsingRing(
      modifier = Modifier
        .fillMaxSize()
        .scale(ringScale)
        .alpha(ringAlpha),
      strokeWidth = strokeWidth
    )

    Icon(
      painter = painterResource(id = R.drawable.ic_menu_nfc),
      contentDescription = "NFC",
      tint = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .size(72.dp)
        .offset(y = bob.dp)
    )
  }
}

@Composable
private fun PulsingRing(
  modifier: Modifier = Modifier,
  strokeWidth: Dp = 4.dp
) {
  val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }
  val color = MaterialTheme.colorScheme.onPrimary

  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val diameter = minOf(w, h)
    val topLeft = Offset((w - diameter) / 2f, (h - diameter) / 2f)
    val ringSize = Size(diameter, diameter)

    drawArc(
      color = color.copy(alpha = 0.55f),
      startAngle = 0f,
      sweepAngle = 360f,
      useCenter = false,
      topLeft = topLeft,
      size = ringSize,
      style = Stroke(width = strokePx, cap = StrokeCap.Round)
    )

    drawArc(
      color = color.copy(alpha = 0.85f),
      startAngle = 300f,
      sweepAngle = 55f,
      useCenter = false,
      topLeft = topLeft,
      size = ringSize,
      style = Stroke(width = strokePx, cap = StrokeCap.Round)
    )
  }
}
