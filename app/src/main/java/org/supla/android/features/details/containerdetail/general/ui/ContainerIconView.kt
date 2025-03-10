package org.supla.android.features.details.containerdetail.general.ui
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.extensions.fontDpSize
import org.supla.android.extensions.toPx
import org.supla.core.shared.extensions.ifTrue
import kotlin.math.abs

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
  alertTextMargin = 4f
)

@Composable
fun ContainerIconView(
  fillLevel: Float?,
  modifier: Modifier = Modifier,
  controlLevels: List<ControlLevel> = emptyList(),
  type: ContainerType = ContainerType.DEFAULT
) {
  var scale by remember { mutableFloatStateOf(0f) }

  // Colors
  val borderColor = MaterialTheme.colorScheme.outline
  val fluidColor = type.primary()
  val contentColor = MaterialTheme.colorScheme.surface
  val waveColor = remember(type) { type.secondary() }

  // Alert text
  val textMeasurer = rememberTextMeasurer()
  val titleSmallStyle = MaterialTheme.typography.titleSmall
  val fontSize = fontDpSize(6.dp)
  val textStyle = remember(scale) { titleSmallStyle.copy(fontSize = fontSize.times(scale)) }
  val textLayoutResults = remember(controlLevels, textStyle) {
    controlLevels.associateWith { textMeasurer.measure(it.levelString, textStyle) }
  }

  val context = remember(scale, type) {
    object : ContainerViewScope() {
      override val waveColor: Color = waveColor
      override val borderColor: Color = borderColor
      override val contentColor: Color = contentColor
      override val fluidColor: Color = fluidColor
      override val specification: ContainerSpecification = CONTAINER_SPECIFICATION.scale(scale)
    }
  }

  Canvas(modifier = modifier) {
    context.drawFluidContainer(fillLevel, controlLevels, textLayoutResults) {
      targetRect.width.div(CONTAINER_WIDTH).let {
        (it != scale).ifTrue { scale = it }
      }
    }
  }
}

private abstract class ContainerViewScope {
  lateinit var targetRect: Rect

  val containerMargin: Float = 4.dp.toPx()
  val containerStrokeWidth: Float = 2.dp.toPx()
  val controlLevelStrokeWidth: Float = 1.dp.toPx()

  val containerPath: Path = Path()
  val fluidPath: Path = Path()
  val clipPath: Path = Path()
  val wavePath: Path = Path()
  val labelPath: Path = Path()

  var alarmUpper: ControlLevel? = null
  var alarmLower: ControlLevel? = null
  var warningUpper: ControlLevel? = null
  var warningLower: ControlLevel? = null

  abstract val waveColor: Color
  abstract val contentColor: Color
  abstract val fluidColor: Color
  abstract val borderColor: Color

  abstract val specification: ContainerSpecification
}

context(DrawScope)
private fun ContainerViewScope.drawFluidContainer(
  fillLevel: Float?,
  controlLevels: List<ControlLevel>,
  textLayoutResults: Map<ControlLevel, TextLayoutResult>,
  updateScaleCallback: ContainerViewScope.() -> Unit
) {
  targetRect = targetRect(containerMargin)
  updateScaleCallback()

  val containerRect = containerRect(targetRect, specification)
  clipPath.reset()
  clipPath.addRoundRect(roundRect = containerRect)
  containerPath.setupContainer(targetRect, containerRect, specification)

  drawPath(path = containerPath, color = contentColor)
  drawFluid(fillLevel)
  drawControlLevelsLines(controlLevels)

  drawPath(
    path = containerPath,
    color = borderColor,
    style = Stroke(width = containerStrokeWidth, pathEffect = PathEffect.cornerPathEffect(1f))
  )

  warningUpper?.let { drawWarningControlLevel(it, textLayoutResults[it], nextLevel = warningLower) }
  warningLower?.let { drawWarningControlLevel(it, textLayoutResults[it], previousLevel = warningUpper) }
  alarmUpper?.let { drawAlarmControlLevel(it, textLayoutResults[it], nextLevel = alarmLower) }
  alarmLower?.let { drawAlarmControlLevel(it, textLayoutResults[it], previousLevel = alarmUpper) }
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
private fun ContainerViewScope.drawControlLevelsLines(controlLevels: List<ControlLevel>) {
  alarmUpper = controlLevels.firstOrNull { it is ErrorLevel && it.type == ControlLevel.Type.UPPER }
  alarmLower = controlLevels.firstOrNull { it is ErrorLevel && it.type == ControlLevel.Type.LOWER }
  warningUpper = controlLevels.firstOrNull { it is WarningLevel && it.type == ControlLevel.Type.UPPER }
  warningLower = controlLevels.firstOrNull { it is WarningLevel && it.type == ControlLevel.Type.LOWER }

  warningLower?.let { drawControlLine(it) }
  warningUpper?.let { drawControlLine(it) }
  alarmLower?.let { drawControlLine(it) }
  alarmUpper?.let { drawControlLine(it) }
}

context(DrawScope)
private fun ContainerViewScope.targetRect(containerMargin: Float): Rect {
  val canvasRatio = size.width / size.height
  val doubleMargin = containerMargin.times(2)

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
private fun ContainerViewScope.drawControlLine(
  controlLevel: ControlLevel,
) {
  val levelPosition = specification.levelPosition(targetRect, controlLevel.level)
  drawLine(
    color = controlLevel.color,
    start = Offset(targetRect.left, levelPosition),
    end = Offset(targetRect.right, levelPosition),
    pathEffect = PathEffect.dashPathEffect(floatArrayOf(specification.dashWidth, specification.dashSpace)),
    strokeWidth = controlLevelStrokeWidth
  )
}

context(DrawScope)
private fun ContainerViewScope.drawAlarmControlLevel(
  controlLevel: ControlLevel,
  textLayoutResult: TextLayoutResult?,
  previousLevel: ControlLevel? = null,
  nextLevel: ControlLevel? = null
) {
  val textWidth = textLayoutResult?.size?.width ?: 0
  val levelPosition = specification.levelPosition(targetRect, controlLevel.level)
  val textStartPosition = targetRect.right
    .minus(containerStrokeWidth)
    .minus(textWidth)

  val topPosition = getControlLevelTextPosition(previousLevel, nextLevel, levelPosition, textLayoutResult)

  textLayoutResult?.let {
    translate(left = textStartPosition, top = topPosition) {
      labelPath.setupLabel(specification, controlLevel, it.size)
      drawPath(labelPath, contentColor)
      drawPath(labelPath, borderColor, style = Stroke(containerStrokeWidth))
      drawText(it, color = controlLevel.color)
    }
  }
}

context(DrawScope)
private fun ContainerViewScope.drawWarningControlLevel(
  controlLevel: ControlLevel,
  textLayoutResult: TextLayoutResult?,
  previousLevel: ControlLevel? = null,
  nextLevel: ControlLevel? = null
) {
  val levelPosition = specification.levelPosition(targetRect, controlLevel.level)
  val textStartPosition = targetRect.left
    .plus(containerStrokeWidth)

  val topPosition = getControlLevelTextPosition(previousLevel, nextLevel, levelPosition, textLayoutResult)

  textLayoutResult?.let {
    translate(left = textStartPosition, top = topPosition) {
      labelPath.setupLabel(specification, controlLevel, it.size)
      drawPath(labelPath, contentColor)
      drawPath(labelPath, borderColor, style = Stroke(containerStrokeWidth))
      drawText(it, color = controlLevel.color)
    }
  }
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
      val previousLevelPosition = specification.levelPosition(targetRect, previousLevel.level)
      val desiredPosition = levelPosition - halfTextHeight
      val previousDesiredPosition = previousLevelPosition - halfTextHeight
      if (desiredPosition > previousDesiredPosition + textHeight - specification.alertTextMargin) {
        desiredPosition
      } else {
        val middleLine = previousLevelPosition + abs(levelPosition - previousLevelPosition).div(2)
        middleLine - specification.alertTextMargin
      }
    }

    nextLevel != null -> {
      val desiredPosition = levelPosition - halfTextHeight
      val nextDesiredPosition = specification.levelPosition(targetRect, nextLevel.level)

      if (nextDesiredPosition > desiredPosition + textHeight + specification.alertTextMargin) {
        desiredPosition
      } else {
        val middleLine = levelPosition + abs(nextDesiredPosition - levelPosition) / 2
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

private fun Path.setupLabel(specification: ContainerSpecification, controlLevel: ControlLevel, size: IntSize) {
  val correction = specification.alertTextMargin.div(2)

  reset()
  moveTo(-correction, specification.alertTextMargin)
  lineTo(-correction, size.height.toFloat().minus(correction))
  if (controlLevel.type == ControlLevel.Type.LOWER) {
    lineTo(size.width.toFloat().div(2), size.height.toFloat().plus(correction))
  }
  lineTo(size.width.toFloat() + correction, size.height.toFloat().minus(correction))
  lineTo(size.width.toFloat() + correction, specification.alertTextMargin)
  if (controlLevel.type == ControlLevel.Type.UPPER) {
    lineTo(size.width.toFloat().div(2), -(correction))
  }
  close()
}

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
    alertTextMargin = alertTextMargin.times(scale)
  )

  fun levelPosition(targetRect: Rect, level: Float) =
    targetRect.bottom
      .minus(bottomPartHeight.minus(levelMargin.times(2)).times(level))
      .minus(levelMargin)
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun Preview() {
  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      ContainerIconView(
        fillLevel = 0f,
        controlLevels = listOf(
          ErrorLevel(0.99f, "99%", ControlLevel.Type.UPPER),
          WarningLevel(0.9f, "85%", ControlLevel.Type.UPPER),
          WarningLevel(0.15f, "15%", ControlLevel.Type.LOWER)
        ),
        modifier = Modifier.size(width = 75.dp, height = 120.dp)
      )
      ContainerIconView(
        fillLevel = 1f,
        controlLevels = listOf(
          WarningLevel(0.9f, "90%", ControlLevel.Type.UPPER),
          WarningLevel(0.86f, "86%", ControlLevel.Type.LOWER)
        ),
        modifier = Modifier.size(width = 150.dp, height = 240.dp),
        type = ContainerType.WATER
      )
      ContainerIconView(
        fillLevel = 0.5f,
        controlLevels = listOf(ErrorLevel(0.9f, "90%", ControlLevel.Type.UPPER)),
        modifier = Modifier.size(width = 200.dp, height = 240.dp),
        type = ContainerType.SEPTIC
      )
      ContainerIconView(
        fillLevel = 1f,
        modifier = Modifier.size(width = 150.dp, height = 300.dp)
      )
      ContainerIconView(
        fillLevel = null,
        modifier = Modifier
          .size(width = 150.dp, height = 300.dp)
      )
    }
  }
}
