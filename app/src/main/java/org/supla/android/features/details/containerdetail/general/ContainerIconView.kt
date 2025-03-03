package org.supla.android.features.details.containerdetail.general
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.fontDpSize
import org.supla.android.extensions.toPx
import org.supla.core.shared.extensions.ifTrue
import kotlin.math.abs

sealed class ControlLevel {
  abstract val level: Float
  abstract val levelString: String
  abstract val color: Color

  var levelPosition: Float? = null
}

data class ErrorLevel(
  override val level: Float,
  override val levelString: String
) : ControlLevel() {
  override val color: Color
    get() = Color(0xFFEB3A28)
}

data class WarningLevel(
  override val level: Float,
  override val levelString: String
) : ControlLevel() {
  override val color: Color
    get() = Color(0xFFE3A400)
}

private const val CONTAINER_WIDTH = 150f
private const val CONTAINER_HEIGHT = 240f
private const val RATIO = CONTAINER_WIDTH / CONTAINER_HEIGHT

private val CONTAINER_SPECIFICATION = ContainerSpecification(
  bottomPartHeight = 190f,
  topPartWidth = 56f,
  containerRadius = 10f,
  coverHeight = 16f,
  coverWidth = 32f,
  coverRadius = 5f,
  waveX = 25f,
  waveY = 12f,
  levelMargin = 10f,
  dashWidth = 5f,
  dashSpace = 3f,
  alertSize = 20f,
  alertStartMargin = 20f,
  alertTextMargin = 4f
)

private data class ContainerSpecification(
  val bottomPartHeight: Float,
  val topPartWidth: Float,
  val containerRadius: Float,
  val coverHeight: Float,
  val coverWidth: Float,
  val coverRadius: Float,
  val waveX: Float,
  val waveY: Float,
  val levelMargin: Float,
  val dashWidth: Float,
  val dashSpace: Float,
  val alertSize: Float,
  val alertStartMargin: Float,
  val alertTextMargin: Float
) {
  val containerCornerRadius = CornerRadius(containerRadius, containerRadius)
  val coverCornerRadius = CornerRadius(coverRadius, coverRadius)

  fun scale(scale: Float) = copy(
    bottomPartHeight = bottomPartHeight.times(scale),
    topPartWidth = topPartWidth.times(scale),
    containerRadius = containerRadius.times(scale),
    coverHeight = coverHeight.times(scale),
    coverWidth = coverWidth.times(scale),
    coverRadius = coverRadius.times(scale),
    waveX = waveX.times(scale),
    waveY = waveY.times(scale),
    levelMargin = levelMargin.times(scale),
    dashWidth = dashWidth.times(scale),
    dashSpace = dashSpace.times(scale),
    alertSize = alertSize.times(scale),
    alertStartMargin = alertStartMargin.times(scale),
    alertTextMargin = alertTextMargin.times(scale)
  )

  fun levelPosition(targetRect: Rect, level: Float) =
    targetRect.bottom
      .minus(bottomPartHeight.minus(levelMargin.times(2)).times(level))
      .minus(levelMargin)
}

@Composable
fun ContainerIconView(fillLevel: Float?, modifier: Modifier = Modifier, controlLevels: List<ControlLevel> = emptyList()) {
  var scale by remember { mutableFloatStateOf(0f) }

  // Colors
  val borderColor = MaterialTheme.colorScheme.outline
  val fluidColor = MaterialTheme.colorScheme.secondary
  val contentColor = MaterialTheme.colorScheme.surface

  // Alert icon
  val alertVector = ImageVector.vectorResource(R.drawable.ic_alert_triangle)
  val alertPainter = rememberVectorPainter(alertVector)

  // Alert text
  val textMeasurer = rememberTextMeasurer()
  val titleSmallStyle = MaterialTheme.typography.titleSmall
  val fontSize = fontDpSize(5.4.dp)
  val textStyle = remember(scale) { titleSmallStyle.copy(fontSize = fontSize.times(scale)) }
  val textLayoutResults = remember(controlLevels, textStyle) {
    controlLevels.associateWith { textMeasurer.measure(it.levelString, textStyle) }
  }

  val context = remember(scale) {
    object : ContainerViewScope {
      override lateinit var targetRect: Rect

      override val containerMargin: Float = 4.dp.toPx()
      override val containerStrokeWidth: Float = 2.dp.toPx()
      override val controlLevelStrokeWidth: Float = 1.dp.toPx()

      override val clipPath: Path = Path()
      override val containerPath: Path = Path()
      override val fluidPath: Path = Path()
      override val wavePath: Path = Path()

      override val waveColor: Color = Color(0xFF0067D4)
      override val borderColor: Color = borderColor
      override val contentColor: Color = contentColor
      override val fluidColor: Color = fluidColor

      override val specification: ContainerSpecification = CONTAINER_SPECIFICATION.scale(scale)
    }
  }

  Canvas(modifier = modifier) {
    context.drawFluidContainer(fillLevel, controlLevels, alertPainter, textLayoutResults) {
      targetRect.width.div(CONTAINER_WIDTH).let {
        (it != scale).ifTrue { scale = it }
      }
    }
  }
}

private interface ContainerViewScope {
  var targetRect: Rect

  val containerMargin: Float
  val containerStrokeWidth: Float
  val controlLevelStrokeWidth: Float

  val containerPath: Path
  val fluidPath: Path
  val clipPath: Path
  val wavePath: Path

  val waveColor: Color
  val contentColor: Color
  val fluidColor: Color
  val borderColor: Color

  val specification: ContainerSpecification
}

private val ContainerViewScope.doubleMargin: Float
  get() = containerMargin.times(2)

context(DrawScope)
private fun ContainerViewScope.drawFluidContainer(
  fillLevel: Float?,
  controlLevels: List<ControlLevel>,
  alertPainter: VectorPainter,
  textLayoutResults: Map<ControlLevel, TextLayoutResult>,
  updateScaleCallback: ContainerViewScope.() -> Unit
) {
  targetRect = targetRect(doubleMargin, containerMargin)
  updateScaleCallback()

  val containerRect = containerRect(targetRect, specification)
  clipPath.reset()
  clipPath.addRoundRect(roundRect = containerRect)
  containerPath.setupContainer(targetRect, containerRect, specification)

  drawPath(path = containerPath, color = contentColor)
  drawFluid(fillLevel)
  drawControlLevels(controlLevels, alertPainter, textLayoutResults)

  drawPath(
    path = containerPath,
    color = borderColor,
    style = Stroke(width = containerStrokeWidth, pathEffect = PathEffect.cornerPathEffect(1f))
  )
}

context(DrawScope)
private fun ContainerViewScope.drawFluid(fillLevel: Float?) {
  clipPath(path = clipPath) {
    fillLevel?.let {
      fluidPath.setupFluid(fillLevel, targetRect, specification)
      drawPath(path = fluidPath, color = fluidColor)
      wavePath.setupWave(fillLevel, targetRect, specification)
      drawPath(path = wavePath, color = waveColor)
    }
  }
}

context(DrawScope)
private fun ContainerViewScope.drawControlLevels(
  controlLevels: List<ControlLevel>,
  alertPainter: VectorPainter,
  textLayoutResults: Map<ControlLevel, TextLayoutResult>
) {
  for (i in controlLevels.indices) {
    val controlLevel = controlLevels[i]
    drawControlLevel(
      controlLevel = controlLevel,
      previousLevel = controlLevels.getOrNull(i - 1),
      nextLevel = controlLevels.getOrNull(i + 1),
      alertPainter = alertPainter,
      textLayoutResult = textLayoutResults[controlLevel]
    )
  }
}

context(DrawScope)
private fun ContainerViewScope.targetRect(doubleMargin: Float, containerMargin: Float): Rect {
  val canvasRatio = size.width / size.height

  return if (canvasRatio < RATIO) {
    val height = size.width.minus(doubleMargin).div(RATIO)
    val topOffset = size.height.minus(height).div(2)
    Rect(
      topLeft = Offset(x = containerMargin, y = topOffset),
      bottomRight = Offset(x = size.width.minus(containerMargin), y = size.height - topOffset)
    )
  } else {
    val width = size.height.minus(doubleMargin).times(RATIO)
    val leftOffset = size.width.minus(width).div(2)
    Rect(
      topLeft = Offset(x = leftOffset, y = containerMargin),
      bottomRight = Offset(x = size.width - leftOffset, y = size.height.minus(containerMargin))
    )
  }
}

private fun containerRect(targetRect: Rect, specification: ContainerSpecification): RoundRect {
  val bottomPartOffset = targetRect.top.plus(targetRect.height - specification.bottomPartHeight)
  return RoundRect(
    left = targetRect.left,
    top = bottomPartOffset,
    right = targetRect.right,
    bottom = targetRect.bottom,
    bottomLeftCornerRadius = specification.containerCornerRadius,
    bottomRightCornerRadius = specification.containerCornerRadius
  )
}

context(DrawScope)
private fun ContainerViewScope.drawControlLevel(
  controlLevel: ControlLevel,
  previousLevel: ControlLevel?,
  nextLevel: ControlLevel?,
  alertPainter: VectorPainter,
  textLayoutResult: TextLayoutResult?
) {
  val textWidth = textLayoutResult?.size?.width ?: 0
  val levelPosition = specification.levelPosition(targetRect, controlLevel.level)
  val iconStartPosition = targetRect.right
    .minus(specification.alertStartMargin)
    .minus(specification.alertTextMargin.times(2))
    .minus(textWidth)

  val topPosition = getControlLevelTextPosition(previousLevel, nextLevel, levelPosition, textLayoutResult)
  controlLevel.levelPosition = levelPosition

  with(alertPainter) {
    translate(left = iconStartPosition, top = topPosition) {
      draw(Size(specification.alertSize, specification.alertSize), colorFilter = ColorFilter.tint(controlLevel.color))
    }
    textLayoutResult?.let {
      translate(
        left = targetRect.right - specification.alertTextMargin - textWidth,
        top = topPosition
      ) {
        drawText(it, color = controlLevel.color)
      }
    }
  }
  drawLine(
    color = controlLevel.color,
    start = Offset(targetRect.left, levelPosition),
    end = Offset(iconStartPosition, levelPosition),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(specification.dashWidth, specification.dashSpace)),
    strokeWidth = controlLevelStrokeWidth
  )
}

private fun ContainerViewScope.getControlLevelTextPosition(
  previousLevel: ControlLevel?,
  nextLevel: ControlLevel?,
  levelPosition: Float,
  textLayoutResult: TextLayoutResult?
): Float {
  val textHeight = textLayoutResult?.size?.height ?: 0
  val halfTextHeight = textHeight.div(2f)

  return when {
    previousLevel != null -> {
      val desiredPosition = levelPosition - halfTextHeight
      val previousDesiredPosition = (previousLevel.levelPosition ?: 0f)
      if (desiredPosition > previousDesiredPosition) {
        desiredPosition
      } else {
        val middleLine = previousDesiredPosition + abs(desiredPosition - previousDesiredPosition) / 2
        middleLine + specification.alertTextMargin
      }
    }

    nextLevel != null -> {
      val desiredPosition = levelPosition - halfTextHeight
      val nextDesiredPosition = specification.levelPosition(targetRect, nextLevel.level)

      if (nextDesiredPosition > desiredPosition + textHeight + specification.alertTextMargin) {
        desiredPosition
      } else {
        val middleLine = desiredPosition + abs(nextDesiredPosition - desiredPosition) / 2
        middleLine - textHeight + specification.alertTextMargin
      }
    }

    else -> levelPosition - halfTextHeight
  }
}

private fun Path.setupContainer(targetRect: Rect, containerRect: RoundRect, specification: ContainerSpecification) {
  val bottomPartOffset = targetRect.top.plus(targetRect.height - specification.bottomPartHeight)

  reset()
  moveTo(targetRect.left, bottomPartOffset)
  lineTo(targetRect.topCenter.x - specification.topPartWidth.div(2), targetRect.top + specification.coverHeight)
  lineTo(targetRect.topCenter.x + specification.topPartWidth.div(2), targetRect.top + specification.coverHeight)
  lineTo(targetRect.right, bottomPartOffset)
  moveTo(targetRect.center.x - specification.coverWidth.div(2), targetRect.top + specification.coverHeight)
  addRoundRect(roundRect = containerRect)
  addRoundRect(
    roundRect = RoundRect(
      left = targetRect.center.x - specification.coverWidth.div(2),
      top = targetRect.top,
      right = targetRect.center.x + specification.coverWidth.div(2),
      bottom = targetRect.top + specification.coverHeight,
      topLeftCornerRadius = specification.coverCornerRadius,
      topRightCornerRadius = specification.coverCornerRadius
    )
  )
}

private fun Path.setupFluid(fillLevel: Float, targetRect: Rect, specification: ContainerSpecification) {
  val fillHeight = specification.bottomPartHeight.minus(specification.levelMargin.times(2)).times(fillLevel)
  val fluidTop = targetRect.bottom - fillHeight - specification.levelMargin

  reset()
  addRoundRect(
    roundRect = RoundRect(
      left = targetRect.left,
      top = fluidTop,
      right = targetRect.right,
      bottom = targetRect.bottom,
      bottomLeftCornerRadius = specification.containerCornerRadius,
      bottomRightCornerRadius = specification.containerCornerRadius
    )
  )
  moveTo(targetRect.left, fluidTop)
  cubicTo(
    targetRect.left + specification.waveX,
    fluidTop - specification.waveY,
    targetRect.center.x - specification.waveX,
    fluidTop - specification.waveY,
    targetRect.center.x,
    fluidTop
  )
}

private fun Path.setupWave(fillLevel: Float, targetRect: Rect, specification: ContainerSpecification) {
  val fillHeight = specification.bottomPartHeight.minus(specification.levelMargin.times(2)).times(fillLevel)
  val fluidTop = targetRect.bottom - fillHeight - specification.levelMargin

  reset()
  moveTo(targetRect.center.x, fluidTop)
  cubicTo(
    targetRect.center.x + specification.waveX,
    fluidTop - specification.waveY,
    targetRect.right - specification.waveX,
    fluidTop - specification.waveY,
    targetRect.right,
    fluidTop
  )
  cubicTo(
    targetRect.right - specification.waveX,
    fluidTop + specification.waveY,
    targetRect.center.x + specification.waveX,
    fluidTop + specification.waveY,
    targetRect.center.x,
    fluidTop
  )
  close()
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      ContainerIconView(
        fillLevel = 0f,
        controlLevels = listOf(
          ErrorLevel(0.95f, "90%"),
          WarningLevel(0.9f, "85%")
        ),
        modifier = Modifier.size(width = 75.dp, height = 120.dp)
      )
      ContainerIconView(
        fillLevel = 1f,
        controlLevels = listOf(WarningLevel(0.9f, "90%")),
        modifier = Modifier.size(width = 150.dp, height = 240.dp)
      )
      ContainerIconView(
        fillLevel = 0.5f,
        controlLevels = listOf(ErrorLevel(0.9f, "90%")),
        modifier = Modifier.size(width = 200.dp, height = 240.dp)
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Preview_NoValue() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      ContainerIconView(
        fillLevel = 1f,
        modifier = Modifier
          .size(width = 150.dp, height = 300.dp)
      )
      ContainerIconView(
        fillLevel = null,
        modifier = Modifier
          .size(width = 150.dp, height = 300.dp)
      )
    }
  }
}
