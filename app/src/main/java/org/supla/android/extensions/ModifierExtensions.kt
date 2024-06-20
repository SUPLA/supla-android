package org.supla.android.extensions

import android.graphics.BlurMaskFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R

private val shadowPath = Path()

fun Modifier.innerShadow(
  color: Color = Color.Black,
  cornersRadius: Dp = 0.dp,
  spread: Dp = 0.dp,
  blur: Dp = 0.dp,
  offsetY: Dp = 0.dp,
  offsetX: Dp = 0.dp,
  topLeftRadius: Dp? = null,
  topRightRadius: Dp? = null,
  bottomLeftRadius: Dp? = null,
  bottomRightRadius: Dp? = null
) = drawWithContent {
  drawContent()

  val rect = Rect(Offset.Zero, size)
  val paint = Paint()

  val topLeftCornerRadius = topLeftRadius?.toPx() ?: cornersRadius.toPx()
  val topRightCornerRadius = topRightRadius?.toPx() ?: cornersRadius.toPx()
  val bottomLeftCornerRadius = bottomLeftRadius?.toPx() ?: cornersRadius.toPx()
  val bottomRightCornerRadius = bottomRightRadius?.toPx() ?: cornersRadius.toPx()

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

fun Modifier.customOuterShadow(
  color: Color = DefaultShadowColor,
  alpha: Float = 0.25f,
  borderRadius: Dp = 0.dp,
  shadowRadius: Dp = 4.dp,
  offsetY: Dp = 8.dp,
  offsetX: Dp = 0.dp,
  height: Float? = null
) = composed {
  val shadowColor = color.copy(alpha = alpha).toArgb()
  val transparent = color.copy(alpha = 0f).toArgb()
  this.drawBehind {
    this.drawIntoCanvas {
      val paint = Paint()
      val frameworkPaint = paint.asFrameworkPaint()
      frameworkPaint.color = transparent
      frameworkPaint.setShadowLayer(
        shadowRadius.toPx(),
        offsetX.toPx(),
        offsetY.toPx(),
        shadowColor
      )
      it.drawRoundRect(
        0f,
        0f,
        this.size.width,
        height ?: this.size.height,
        borderRadius.toPx(),
        borderRadius.toPx(),
        paint
      )
    }
  }
}

@Composable
fun Modifier.disabledOverlay(disabled: Boolean, color: Color = colorResource(id = R.color.disabledOverlay)) =
  composed {
    val paint = remember {
      Paint().also { it.color = color }
    }

    drawWithContent {
      drawContent()

      if (disabled) {
        drawIntoCanvas {
          it.drawRect(0f, 0f, size.width, size.height, paint)
        }
      }
    }
  }
