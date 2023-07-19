package org.supla.android.ui.views

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.progressPointShadow
import org.supla.android.ui.views.tools.drawControlPoint
import kotlin.math.cos
import kotlin.math.sin

class TimerProgressView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : AbstractComposeView(context, attrs, defStyleAttr) {

  var progress by mutableStateOf(0f)
  var indeterminate by mutableStateOf(false)

  @Composable
  override fun Content() {
    SuplaTheme {
      TimerProgressView(progress, indeterminate)
    }
  }
}

val shadowedPaint = Paint().asFrameworkPaint().apply {
  style = android.graphics.Paint.Style.STROKE
  strokeCap = android.graphics.Paint.Cap.ROUND
}
val indeterminatePaint = Paint().asFrameworkPaint().apply {
  style = android.graphics.Paint.Style.STROKE
  strokeCap = android.graphics.Paint.Cap.ROUND
}

private val viewSize = 232.dp
private val progressRadius = 100.dp

@Composable
fun TimerProgressView(progress: Float, indeterminate: Boolean) {
  val progressBackgroundColor = MaterialTheme.colors.surface
  val progressColor = MaterialTheme.colors.primaryVariant
  val pointShadowColor = MaterialTheme.colors.progressPointShadow
  val progressAlpha = convertProgressToAngle(progress)
  val position = remember {
    Animatable(0f)
  }

  if (indeterminate) {
    LaunchedEffect(position) {
      launch {
        position.animateTo(
          targetValue = 1f,
          animationSpec = repeatable(
            iterations = 999999,
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
          )
        )
      }
    }
  }

  Canvas(modifier = Modifier.size(viewSize)) {
    if (indeterminate.not()) {
      timerProgressLine(
        progressAlpha = progressAlpha,
        backgroundColor = progressBackgroundColor,
        progressColor = progressColor
      )
      timerProgressPoint(
        progressAlpha = progressAlpha,
        progressColor = progressColor,
        pointShadowColor = pointShadowColor
      )
    } else {
      timerProgressLine(
        progressAlpha = 360f,
        backgroundColor = progressBackgroundColor,
        progressColor = progressColor
      )
      indeterminateWave(
        position = position.value,
        progressColor = progressColor
      )
    }
  }
}

context(DrawScope)
private fun timerProgressLine(
  progressAlpha: Float,
  backgroundColor: Color,
  progressColor: Color
) {
  drawCircle(
    color = backgroundColor,
    radius = progressRadius.toPx(),
    style = Stroke(width = 12.dp.toPx())
  )

  val paint = shadowedPaint.apply {
    strokeWidth = 6.dp.toPx()
    color = progressColor.toArgb()
    setShadowLayer(
      10.dp.toPx(),
      0f,
      0f,
      progressColor.copy(alpha = 0.7f).toArgb()
    )
  }
  drawContext.canvas.nativeCanvas.drawArc(
    RectF(
      center.x.minus(progressRadius.toPx()),
      center.y.minus(progressRadius.toPx()),
      center.x.plus(progressRadius.toPx()),
      center.y.plus(progressRadius.toPx())
    ),
    270f,
    progressAlpha,
    false,
    paint
  )
}

context(DrawScope)
private fun timerProgressPoint(
  progressAlpha: Float,
  progressColor: Color,
  pointShadowColor: Color
) {
  val alpha = progressAlpha.minus(90).times(Math.PI).div(180)
  val x = 100.dp.toPx() * cos(alpha).toFloat() + center.x
  val y = 100.dp.toPx() * sin(alpha).toFloat() + center.y

  drawControlPoint(Offset(x, y), progressColor, pointShadowColor)
  drawCircle(
    color = pointShadowColor,
    radius = 12.dp.toPx(),
    style = Fill,
    center = Offset(x, y)
  )
  drawCircle(
    color = progressColor,
    radius = 8.dp.toPx(),
    style = Fill,
    center = Offset(x, y)
  )
}

context(DrawScope)
private fun indeterminateWave(
  position: Float,
  progressColor: Color
) {
  val paint = indeterminatePaint.apply {
    strokeWidth = 6.dp.toPx()
    color = progressColor.toArgb()
    shader = RadialGradientShader(
      Offset(position * viewSize.toPx(), center.y),
      40f,
      listOf(Color.White.copy(alpha = 0.7f), progressColor),
      listOf(0.01f, 1f),
      TileMode.Mirror
    )
    isDither = true
  }
  drawContext.canvas.nativeCanvas.drawArc(
    RectF(
      center.x.minus(progressRadius.toPx()),
      center.y.minus(progressRadius.toPx()),
      center.x.plus(progressRadius.toPx()),
      center.y.plus(progressRadius.toPx())
    ),
    270f,
    360f,
    false,
    paint
  )
}

private fun convertProgressToAngle(progress: Float): Float =
  if (progress > 1) {
    360f
  } else if (progress < 0) {
    0f
  } else {
    360f * progress
  }

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column {
      Box(modifier = Modifier.background(color = Color(0xFFF5F6F7))) {
        TimerProgressView(0.55f, false)
      }
      Box(modifier = Modifier.background(color = Color(0xFFF5F6F7))) {
        TimerProgressView(0.55f, true)
      }
    }
  }
}
