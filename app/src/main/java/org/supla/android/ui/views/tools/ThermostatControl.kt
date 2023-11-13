package org.supla.android.ui.views.tools
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

import android.graphics.Path
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.blue
import org.supla.android.core.ui.theme.progressPointShadow
import org.supla.android.extensions.distanceTo
import org.supla.android.extensions.toPx
import java.lang.Float.min
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

val THERMOSTAT_VERTICAL_POSITION_CORRECTION = (-24).dp

private const val START_ANGLE = 150f
private const val SWEEP_ANGLE = 240f

private val touchPointRadius = 24.dp

private val setpointRadius = 12.dp
private val setpointIconSize = 18.dp
private val controlCircleWidth = 16.dp
private val controlMinMaxStrokeWidth = 6.dp
private val controlShadowWidth = 20.dp

private val setpointTemperatureSizeBig = 48.sp
private val setpointTemperatureSizeSmall = 32.sp
private val configTemperatureSize = 14.sp
private val setpointTemperatureFont = FontFamily(Font(R.font.quicksand_medium))
private val configTemperatureFont = FontFamily(Font(R.font.quicksand_regular))

private val controlCirclePath = Path()
private val controlCircleHelperPath = Path()
private val controlCirclePaint = Paint().asFrameworkPaint().apply {
  style = android.graphics.Paint.Style.FILL
}
private val temperatureCirclePaint = Paint().asFrameworkPaint().apply {
  style = android.graphics.Paint.Style.FILL
}
private val shaderColors = listOf(Color(0xFFD2D2D2), Color.White, Color.White, Color(0xFFD2D2D2))
private val shaderPositions = listOf(0.85f, 0.9f, 0.95f, 1f)

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ThermostatControl(
  modifier: Modifier = Modifier,
  mainTemperatureTextProvider: (Float?, Float?) -> String,
  minTemperature: String,
  maxTemperature: String,
  currentValue: Float? = null,
  minSetpoint: Float? = null,
  maxSetpoint: Float? = null,
  isOff: Boolean = false,
  isOffline: Boolean = false,
  isHeating: Boolean = false,
  isCooling: Boolean = false,
  onPositionChangeStarted: () -> Unit,
  onPositionChangeEnded: (Float?, Float?) -> Unit
) {
  val primaryColor = colorResource(id = R.color.primary)
  val disabledColor = colorResource(id = R.color.disabled)
  val textColor = MaterialTheme.colors.onBackground
  val pointShadowColor = MaterialTheme.colors.progressPointShadow
  val (minPointColor, minPointShadowColor) = if (isOff) {
    listOf(disabledColor, disabledColor.copy(alpha = 0.4f))
  } else {
    listOf(MaterialTheme.colors.error, MaterialTheme.colors.error.copy(alpha = 0.4f))
  }
  val (maxPointColor, maxPointShadowColor) = if (isOff) {
    listOf(disabledColor, disabledColor.copy(alpha = 0.4f))
  } else {
    listOf(MaterialTheme.colors.blue, MaterialTheme.colors.blue.copy(alpha = 0.4f))
  }
  val indicatorShadowColor = when {
    isOffline -> DefaultShadowColor
    isHeating -> MaterialTheme.colors.error
    isCooling -> MaterialTheme.colors.blue
    isOff -> DefaultShadowColor
    else -> primaryColor
  }

  val paddings = dimensionResource(id = R.dimen.distance_default)
  val desiredRadius = 135.dp

  val textMeasurer = rememberTextMeasurer()
  val minTemperatureLayoutResult = boundaryTemperature(text = minTemperature, textMeasurer = textMeasurer)
  val minTemperatureTextSize = minTemperatureLayoutResult.size
  val maxTemperatureLayoutResult = boundaryTemperature(text = maxTemperature, textMeasurer = textMeasurer)
  val maxTemperatureTextSize = maxTemperatureLayoutResult.size

  val minPointIcon = painterResource(id = R.drawable.ic_heat)
  val maxPointIcon = painterResource(id = R.drawable.ic_cool)

  var initialTouchPoint by remember { mutableStateOf<Offset?>(null) }
  var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }
  var outerRadiusState by remember { mutableStateOf<Float?>(null) }
  var centerPointState by remember { mutableStateOf<Offset?>(null) }

  var lastMinSetpoint by remember { mutableStateOf(minSetpoint) }
  var lastMaxSetpoint by remember { mutableStateOf(maxSetpoint) }
  if (initialTouchPoint == null) {
    lastMinSetpoint = minSetpoint
    lastMaxSetpoint = maxSetpoint
  }
  val currentPointConfig = ControlPointConfig.build(currentValue, primaryColor, pointShadowColor)
  val minPointConfig = ControlPointConfig.build(minSetpoint, minPointColor, minPointShadowColor, minPointIcon) { lastMinSetpoint = it }
  val maxPointConfig = ControlPointConfig.build(maxSetpoint, maxPointColor, maxPointShadowColor, maxPointIcon) { lastMaxSetpoint = it }

  Canvas(
    modifier = modifier.pointerInteropFilter {
      when (it.action) {
        MotionEvent.ACTION_DOWN -> {
          initialTouchPoint = Offset(it.x, it.y)
          currentTouchPoint = Offset(it.x, it.y)

          centerPointState?.let { center ->
            outerRadiusState?.let { radius ->
              val distance = center.distanceTo(initialTouchPoint)!!
              if (distance > radius - touchPointRadius.toPx() && distance < radius) {
                onPositionChangeStarted()
              }
            }
          }
        }

        MotionEvent.ACTION_MOVE -> currentTouchPoint = Offset(it.x, it.y)
        MotionEvent.ACTION_UP -> {
          onPositionChangeEnded(lastMinSetpoint, lastMaxSetpoint)
          initialTouchPoint = null
          currentTouchPoint = null
        }
      }

      true
    }
  ) {
    val availableRadius = size.width.div(2).minus(paddings.toPx().times(2)) // half of width minus paddings
    val outerRadius = min(desiredRadius.toPx(), availableRadius)
    val center = Offset(center.x, center.y + THERMOSTAT_VERTICAL_POSITION_CORRECTION.toPx())
    outerRadiusState = outerRadius
    centerPointState = center

    drawControlTemperatureCircle(
      outerRadius = outerRadius,
      minTemperature = minTemperatureLayoutResult,
      minTemperatureSize = minTemperatureTextSize,
      maxTemperature = maxTemperatureLayoutResult,
      maxTemperatureSize = maxTemperatureTextSize,
      textColor = textColor,
      center = center,
      isOffline = isOffline
    )

    val mainTemperatureText = mainTemperatureTextProvider(lastMinSetpoint, lastMaxSetpoint)
    val temperatureControlLayoutResult = mainTemperature(text = mainTemperatureText, textMeasurer = textMeasurer)
    val temperatureControlTextSize = temperatureControlLayoutResult.size
    drawSetTemperatureCircle(
      text = temperatureControlLayoutResult,
      textColor = textColor,
      textSize = temperatureControlTextSize,
      shadowColor = indicatorShadowColor,
      outerRadius = outerRadius,
      center = center
    )

    drawControlPoints(
      outerRadius = outerRadius,
      maxSetpointConfig = maxPointConfig,
      minSetpointConfig = minPointConfig,
      currentPointConfig = currentPointConfig,
      primaryColor = primaryColor,
      center = center,
      initialPoint = initialTouchPoint,
      movingPoint = currentTouchPoint,
      isOffline = isOffline
    )
  }
}

context(DrawScope)
@OptIn(ExperimentalTextApi::class)
fun drawControlTemperatureCircle(
  outerRadius: Float,
  minTemperature: TextLayoutResult,
  minTemperatureSize: IntSize,
  maxTemperature: TextLayoutResult,
  maxTemperatureSize: IntSize,
  textColor: Color,
  center: Offset,
  isOffline: Boolean
) {
  val centerRadius = outerRadius - controlCircleWidth.toPx().div(2)
  val circleInnerRadius = outerRadius - controlCircleWidth.toPx()

  controlCirclePath.reset()
  controlCircleHelperPath.reset()

  // building path
  controlCirclePath.addArc(
    center.x - outerRadius,
    center.y - outerRadius,
    center.x + outerRadius,
    center.y + outerRadius,
    START_ANGLE,
    SWEEP_ANGLE
  )
  controlCircleHelperPath.addArc(
    center.x - circleInnerRadius,
    center.y - circleInnerRadius,
    center.x + circleInnerRadius,
    center.y + circleInnerRadius,
    START_ANGLE - 8,
    SWEEP_ANGLE + 16
  )
  controlCirclePath.op(controlCircleHelperPath, Path.Op.DIFFERENCE)

  // setting gradient shader
  controlCirclePaint.shader = RadialGradientShader(
    Offset(x = center.x, y = center.y + 1.dp.toPx()),
    outerRadius + 2.dp.toPx(),
    shaderColors,
    shaderPositions
  )

  // drawing
  drawContext.canvas.nativeCanvas.drawPath(controlCirclePath, controlCirclePaint)

  if (isOffline.not()) {
    // boundary temperatures
    drawText(
      textLayoutResult = minTemperature,
      color = textColor,
      topLeft = calculateBoundaryTemperaturePosition(144f, centerRadius, minTemperatureSize, center)
    )
    drawText(
      textLayoutResult = maxTemperature,
      color = textColor,
      topLeft = calculateBoundaryTemperaturePosition(36f, centerRadius, maxTemperatureSize, center)
    )
  }
}

context(DrawScope)
private fun calculateBoundaryTemperaturePosition(angle: Float, radius: Float, size: IntSize, center: Offset): Offset {
  val position = getPositionOnCircle(angle, radius, center)
  return Offset(position.x - size.width.div(2f), position.y - size.height.div(2f))
}

context(DrawScope)
@OptIn(ExperimentalTextApi::class)
fun drawSetTemperatureCircle(
  text: TextLayoutResult,
  textSize: IntSize,
  textColor: Color,
  shadowColor: Color,
  outerRadius: Float,
  center: Offset
) {
  val shadowWidth = controlShadowWidth.toPx()
  val shadowColorWithAlpha = shadowColor.copy(alpha = 0.2f).toArgb()
  val radius = outerRadius - 35.dp.toPx()

  // shadow paint
  temperatureCirclePaint.setShadowLayer(shadowWidth, 0f, 0f, shadowColorWithAlpha)

  // circle with shadow
  drawContext.canvas.nativeCanvas.drawCircle(center.x, center.y, radius, temperatureCirclePaint)

  // white circle over shadow
  drawCircle(color = Color.White, radius = radius, style = Fill, center = center)

  // temperature text inside of the circle
  drawText(text, textColor, topLeft = Offset(center.x - textSize.width.div(2f), center.y - textSize.height.div(2f)))
}

context(DrawScope)
private fun drawControlPoints(
  outerRadius: Float,
  maxSetpointConfig: ControlPointConfig?,
  minSetpointConfig: ControlPointConfig?,
  currentPointConfig: ControlPointConfig?,
  primaryColor: Color,
  center: Offset,
  initialPoint: Offset?,
  movingPoint: Offset?,
  isOffline: Boolean
) {
  val radiusForPoints = outerRadius - controlCircleWidth.toPx().div(2)
  if (maxSetpointConfig != null && minSetpointConfig != null) {
    val startAngle = START_ANGLE + SWEEP_ANGLE.times(minSetpointConfig.value)
    val sweepAngle = SWEEP_ANGLE.times(maxSetpointConfig.value) - SWEEP_ANGLE.times(minSetpointConfig.value)

    drawArc(
      color = primaryColor,
      startAngle = startAngle,
      sweepAngle = sweepAngle,
      useCenter = false,
      topLeft = center.minus(Offset(radiusForPoints, radiusForPoints)),
      size = Size(radiusForPoints * 2, radiusForPoints * 2),
      style = Stroke(width = controlMinMaxStrokeWidth.toPx())
    )
  }

  if (minSetpointConfig != null) {
    val maxAlpha = maxSetpointConfig?.value
    drawSetPoint(minSetpointConfig, radiusForPoints, center, initialPoint, movingPoint, null, maxAlpha)
  }

  if (maxSetpointConfig != null) {
    val minAlpha = minSetpointConfig?.value
    drawSetPoint(maxSetpointConfig, radiusForPoints, center, initialPoint, movingPoint, minAlpha, null)
  }

  if (currentPointConfig != null && isOffline.not()) {
    drawCurrentPoint(currentPointConfig, radiusForPoints, center)
  }
}

context(DrawScope)
private fun drawCurrentPoint(config: ControlPointConfig, radius: Float, center: Offset) {
  val angle = START_ANGLE + SWEEP_ANGLE.times(config.value)
  val centerPoint = getPositionOnCircle(angle, radius, center)
  drawControlPoint(centerPoint, config.pointColor, config.shadowColor)
}

context(DrawScope)
private fun drawSetPoint(
  config: ControlPointConfig,
  radius: Float,
  center: Offset,
  initialPoint: Offset?,
  movingPoint: Offset?,
  minAlpha: Float?,
  maxAlpha: Float?
) {
  val angle = START_ANGLE + SWEEP_ANGLE.times(config.value)
  val centerPoint = getPositionOnCircle(angle, radius, center).run {
    val distance = initialPoint.distanceTo(center)
    if (distance != null && abs(radius - distance) < setpointRadius.toPx()) {
      getNearestCirclePoint(movingPoint!!, center, radius, minAlpha, maxAlpha, config.positionObserver!!)
    } else {
      this
    }
  }

  drawControlPoint(centerPoint, config.pointColor, config.shadowColor, pointRadius = setpointRadius.toPx())
  config.icon?.apply {
    val iconSize = Size(setpointIconSize.toPx(), setpointIconSize.toPx())
    translate(left = centerPoint.x - iconSize.width.div(2), top = centerPoint.y - iconSize.height.div(2)) {
      draw(iconSize)
    }
  }
}

private fun getNearestCirclePoint(
  point: Offset,
  center: Offset,
  radius: Float,
  minAlpha: Float?,
  maxAlpha: Float?,
  positionObserver: (Float) -> Unit
): Offset {
  // Move circle center to 0,0
  val correctedPoint = point.minus(center)

  // Get the sin(alpha) for the touch point
  val touchPointRadius = sqrt(correctedPoint.x.pow(2) + correctedPoint.y.pow(2))
  val sinAlpha = correctedPoint.y.div(touchPointRadius)

  // Convert sin(alpha) angle and check limits
  val alpha = getAlphaPosition(correctedPoint, sinAlpha).let {
    if (minAlpha != null && it < minAlpha.times(SWEEP_ANGLE)) {
      minAlpha.times(SWEEP_ANGLE)
    } else if (maxAlpha != null && it > maxAlpha.times(SWEEP_ANGLE)) {
      maxAlpha.times(SWEEP_ANGLE)
    } else {
      it
    }
  }

  // Inform observer
  positionObserver(alpha.div(SWEEP_ANGLE))

  // Get the position
  val radial = alpha.plus(START_ANGLE).times(PI).div(180)
  val x = radius * cos(radial).toFloat()
  val y = radius * sin(radial).toFloat()

  return Offset(x, y).plus(center)
}

private fun getAlphaPosition(correctedPoint: Offset, sinAlpha: Float) =
  asin(sinAlpha).times(180).div(PI).toFloat().let {
    if (correctedPoint.y > 0 && correctedPoint.x < 0) {
      30 - it
    } else if (correctedPoint.y < 0 && correctedPoint.x > 0) {
      90 + (90 + it) + 30
    } else if (correctedPoint.y > 0 && correctedPoint.x > 0) {
      210 + it
    } else {
      -it + 30
    }
  }.let {
    if (it < 0) {
      0f
    } else if (it > SWEEP_ANGLE) {
      SWEEP_ANGLE
    } else {
      it
    }
  }

@OptIn(ExperimentalTextApi::class)
private fun mainTemperature(text: String, textMeasurer: TextMeasurer): TextLayoutResult {
  val fontSize = if (text.length > 5) {
    setpointTemperatureSizeSmall
  } else {
    setpointTemperatureSizeBig
  }

  val annotatedString = buildAnnotatedString {
    withStyle(
      style = SpanStyle(
        fontSize = fontSize,
        fontFamily = setpointTemperatureFont
      )
    ) {
      append(text)
    }
  }
  return textMeasurer.measure(annotatedString)
}

@OptIn(ExperimentalTextApi::class)
private fun boundaryTemperature(text: String, textMeasurer: TextMeasurer): TextLayoutResult {
  val annotatedString = buildAnnotatedString {
    withStyle(
      style = SpanStyle(
        fontSize = configTemperatureSize,
        fontFamily = configTemperatureFont
      )
    ) {
      append(text)
    }
  }
  return textMeasurer.measure(annotatedString)
}

private fun getPositionOnCircle(angle: Float, radius: Float, center: Offset): Offset {
  val alpha = angle.times(Math.PI).div(180)
  val x = radius * cos(alpha).toFloat() + center.x
  val y = radius * sin(alpha).toFloat() + center.y

  return Offset(x, y)
}

private data class ControlPointConfig(
  val value: Float,
  val pointColor: Color,
  val shadowColor: Color,
  val icon: Painter?,
  val positionObserver: ((Float) -> Unit)?
) {
  companion object {
    fun build(
      value: Float?,
      pointColor: Color,
      shadowColor: Color,
      icon: Painter? = null,
      positionObserver: ((Float) -> Unit)? = null
    ): ControlPointConfig? = if (value == null) {
      null
    } else {
      ControlPointConfig(value, pointColor, shadowColor, icon, positionObserver)
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
      ThermostatControl(
        modifier = Modifier
          .width(400.dp)
          .height(400.dp)
          .background(Color.White),
        mainTemperatureTextProvider = { _, _ -> "22.7°" },
        minTemperature = "10°",
        maxTemperature = "45°",
        onPositionChangeStarted = {},
        onPositionChangeEnded = { _, _ -> }
      )
      ThermostatControl(
        modifier = Modifier
          .width(400.dp)
          .height(400.dp)
          .background(Color.White),
        mainTemperatureTextProvider = { _, _ -> "22.7°" },
        minTemperature = "10°",
        maxTemperature = "45°",
        currentValue = 0.45f,
        minSetpoint = 0.55f,
        maxSetpoint = 0.65f,
        isOff = false,
        isHeating = true,
        onPositionChangeStarted = {},
        onPositionChangeEnded = { _, _ -> }
      )
    }
  }
}
