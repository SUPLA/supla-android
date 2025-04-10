package org.supla.android.extensions
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

import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.ui.views.buttons.supla.SuplaButtonShape

private val shadowPath = Path()

fun Modifier.innerShadow(
  color: Color = Color.Black,
  spread: Dp = 0.dp,
  blur: Dp = 0.dp,
  offsetY: Dp = 0.dp,
  offsetX: Dp = 0.dp,
  topLeftRadius: Dp = 0.dp,
  topRightRadius: Dp = 0.dp,
  bottomLeftRadius: Dp = 0.dp,
  bottomRightRadius: Dp = 0.dp,
  active: () -> Boolean = { true }
) = drawWithContent {
  drawContent()

  if (!active()) {
    return@drawWithContent
  }

  val rect = Rect(Offset.Zero, size)
  val paint = Paint()

  val topLeftCornerRadius = topLeftRadius.toPx()
  val topRightCornerRadius = topRightRadius.toPx()
  val bottomLeftCornerRadius = bottomLeftRadius.toPx()
  val bottomRightCornerRadius = bottomRightRadius.toPx()

  drawIntoCanvas {
    paint.color = color
    paint.isAntiAlias = true
    it.saveLayer(rect, paint)
    shadowPath.reset()
    shadowPath.addRoundRect(
      RoundRect(
        Rect(
          left = rect.left,
          top = rect.top,
          right = rect.right,
          bottom = rect.bottom,
        ),
        topLeft = CornerRadius(topLeftCornerRadius, topLeftCornerRadius),
        topRight = CornerRadius(topRightCornerRadius, topRightCornerRadius),
        bottomLeft = CornerRadius(bottomLeftCornerRadius, bottomLeftCornerRadius),
        bottomRight = CornerRadius(bottomRightCornerRadius, bottomRightCornerRadius)
      )
    )
    it.drawPath(shadowPath, paint)
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    if (blur.toPx() > 0) {
      frameworkPaint.maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
    }
    val left = if (offsetX > 0.dp) {
      rect.left + offsetX.toPx()
    } else {
      rect.left
    }
    val top = if (offsetY > 0.dp) {
      rect.top + offsetY.toPx()
    } else {
      rect.top
    }
    val right = if (offsetX < 0.dp) {
      rect.right + offsetX.toPx()
    } else {
      rect.right
    }
    val bottom = if (offsetY < 0.dp) {
      rect.bottom + offsetY.toPx()
    } else {
      rect.bottom
    }
    paint.color = Color.Black
    shadowPath.reset()
    shadowPath.addRoundRect(
      RoundRect(
        Rect(
          left = left + spread.toPx() / 2,
          top = top + spread.toPx() / 2,
          right = right - spread.toPx() / 2,
          bottom = bottom - spread.toPx() / 2
        ),
        topLeft = CornerRadius(topLeftCornerRadius, topLeftCornerRadius),
        topRight = CornerRadius(topRightCornerRadius, topRightCornerRadius),
        bottomLeft = CornerRadius(bottomLeftCornerRadius, bottomLeftCornerRadius),
        bottomRight = CornerRadius(bottomRightCornerRadius, bottomRightCornerRadius)
      )
    )
    it.drawPath(shadowPath, paint)
    frameworkPaint.xfermode = null
    frameworkPaint.maskFilter = null
  }
}

@Composable
fun Modifier.disabledOverlay(disabled: Boolean, radius: Dp? = null, color: Color = colorResource(id = R.color.disabledOverlay)) =
  composed {
    val paint = remember {
      Paint().also { it.color = color.copy(alpha = 0.7f) }
    }

    drawWithContent {
      drawContent()

      if (disabled) {
        drawIntoCanvas {
          if (radius != null) {
            it.drawRoundRect(0f, 0f, size.width, size.height, radius.toPx(), radius.toPx(), paint)
          } else {
            it.drawRect(0f, 0f, size.width, size.height, paint)
          }
        }
      }
    }
  }

@Composable
fun Modifier.buttonBackground(shape: SuplaButtonShape) =
  this.then(
    Modifier
      .background(
        color = colorResource(id = R.color.surface),
        shape = shape.shape
      )
      .innerShadowForButtonBackground(
        color = colorResource(id = R.color.supla_button_background_outside),
        blur = 10.dp,
        spread = 30.dp,
        shape = shape,
        offsetY = 0.dp
      )
  )

private fun Modifier.innerShadowForButtonBackground(
  color: Color = Color.Black,
  shape: SuplaButtonShape,
  spread: Dp = 0.dp,
  blur: Dp = 0.dp,
  offsetY: Dp = 0.dp,
  offsetX: Dp = 0.dp,
  active: () -> Boolean = { true }
) = drawWithContent {
  if (active()) {
    val rect = Rect(Offset.Zero, size)
    val paint = Paint()

    val topLeftCornerRadius = shape.topStartRadius.toPx()
    val topRightCornerRadius = shape.topEndRadius.toPx()
    val bottomLeftCornerRadius = shape.bottomStartRadius.toPx()
    val bottomRightCornerRadius = shape.bottomEndRadius.toPx()

    drawIntoCanvas {
      paint.color = color
      paint.isAntiAlias = true
      it.saveLayer(rect, paint)
      shadowPath.reset()
      shadowPath.addRoundRect(
        RoundRect(
          Rect(
            left = rect.left,
            top = rect.top,
            right = rect.right,
            bottom = rect.bottom,
          ),
          topLeft = CornerRadius(topLeftCornerRadius, topLeftCornerRadius),
          topRight = CornerRadius(topRightCornerRadius, topRightCornerRadius),
          bottomLeft = CornerRadius(bottomLeftCornerRadius, bottomLeftCornerRadius),
          bottomRight = CornerRadius(bottomRightCornerRadius, bottomRightCornerRadius)
        )
      )
      it.drawPath(shadowPath, paint)
      val frameworkPaint = paint.asFrameworkPaint()
      frameworkPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
      if (blur.toPx() > 0) {
        frameworkPaint.maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
      }
      val left = if (offsetX > 0.dp) {
        rect.left + offsetX.toPx()
      } else {
        rect.left
      }
      val top = if (offsetY > 0.dp) {
        rect.top + offsetY.toPx()
      } else {
        rect.top
      }
      val right = if (offsetX < 0.dp) {
        rect.right + offsetX.toPx()
      } else {
        rect.right
      }
      val bottom = if (offsetY < 0.dp) {
        rect.bottom + offsetY.toPx()
      } else {
        rect.bottom
      }
      paint.color = Color.Black
      shadowPath.reset()
      shadowPath.addRoundRect(
        RoundRect(
          Rect(
            left = left + spread.toPx() / 2,
            top = top + spread.toPx() / 2,
            right = right - spread.toPx() / 2,
            bottom = bottom - spread.toPx() / 2
          ),
          topLeft = CornerRadius(topLeftCornerRadius, topLeftCornerRadius),
          topRight = CornerRadius(topRightCornerRadius, topRightCornerRadius),
          bottomLeft = CornerRadius(bottomLeftCornerRadius, bottomLeftCornerRadius),
          bottomRight = CornerRadius(bottomRightCornerRadius, bottomRightCornerRadius)
        )
      )
      it.drawPath(shadowPath, paint)
      frameworkPaint.xfermode = null
      frameworkPaint.maskFilter = null
    }
  }

  drawContent()
}
