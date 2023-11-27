package org.supla.android.features.thermostatdetail.scheduledetail.ui.components
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

import android.content.Context
import android.content.res.Resources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
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
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.extensions.toPx
import org.supla.android.features.thermostatdetail.scheduledetail.ScheduleDetailViewState
import org.supla.android.features.thermostatdetail.scheduledetail.data.MotionEventStateHolder
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxKey
import org.supla.android.features.thermostatdetail.scheduledetail.data.ScheduleDetailEntryBoxValue
import org.supla.android.features.thermostatdetail.scheduledetail.extensions.color
import org.supla.android.features.thermostatdetail.scheduledetail.ui.PreviewProxy
import org.supla.android.features.thermostatdetail.scheduledetail.ui.ScheduleDetailViewProxy

const val rowsCount = 25
val columnsCount = DayOfWeek.values().size
val boxPadding = 2.dp
val boxSpacing = boxPadding.times(2)
val textPadding = 8.dp

private val textSize = 12.sp
private val textFont = FontFamily(Font(R.font.open_sans_regular))
private val textFontBold = FontFamily(Font(R.font.open_sans_bold))

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTextApi::class)
@Composable
fun ScheduleTable(
  modifier: Modifier = Modifier,
  viewState: ScheduleDetailViewState,
  viewProxy: ScheduleDetailViewProxy
) {
  val context = LocalContext.current
  // colors
  val textColor = MaterialTheme.colors.onBackground
  val colorDisabled = colorResource(id = R.color.disabled)
  val colorHighlight = remember { Color(0x1F767880) }
  // texts with sizes
  val textMeasurer = rememberTextMeasurer()
  val days = remember { mutableStateListOfDrawableDayOfWeek(context, textMeasurer) }
  days.forEach { it.isCurrent = it.value == viewState.currentDayOfWeek }
  val hours = remember { mutableStateListOfDrawableHour(textMeasurer) }
  hours.forEach { it.isCurrent = it.value == viewState.currentHour }
  // positions of elements
  val (viewSize, updateSize) = remember { mutableStateOf<IntSize?>(null) }
  val textWidth = hours.first().textLayoutResult.size.width.plus(boxPadding.toPx()).plus(textPadding.toPx())
  val gridWidth = remember(viewSize) { viewSize?.width?.toFloat()?.minus(textWidth)?.div(columnsCount) ?: 0f }
  val gridHeight = remember(viewSize) { viewSize?.height?.toFloat()?.div(rowsCount) ?: 0f }
  val boxSize = remember(viewSize) { createBoxSize(viewSize, gridWidth, gridHeight) }
  val positions: Map<ScheduleDetailEntryBoxKey, Offset> = remember(viewSize) {
    createBoxesPositions(viewSize, hours, days, textWidth, gridHeight, gridWidth)
  }

  val radiusSize = dimensionResource(id = R.dimen.radius_small).toPx()
  val cornerRadius = remember { CornerRadius(radiusSize, radiusSize) }
  val path = remember { Path() }

  val coroutineScope = rememberCoroutineScope()
  val eventStateHolder by remember(viewSize) { mutableStateOf(MotionEventStateHolder(null, positions, boxSize)) }

  Canvas(
    modifier = modifier
      .onSizeChanged(updateSize)
      .pointerInteropFilter { eventStateHolder.handleEvent(it, viewProxy, context, coroutineScope) }
  ) {
    if (viewSize == null) {
      return@Canvas // Skip drawing when view size is not set yet
    }

    scheduleTableDays(days, textColor, colorHighlight, gridWidth, gridHeight, textWidth)
    scheduleTableBoxes(
      gridHeight,
      hours,
      days,
      textColor,
      colorDisabled,
      colorHighlight,
      viewState,
      positions,
      boxSize,
      path,
      cornerRadius
    )
  }
}

context(DrawScope)
@OptIn(ExperimentalTextApi::class)
private fun scheduleTableDays(
  days: List<DrawableText<DayOfWeek>>,
  textColor: Color,
  currentHighlightColor: Color,
  gridWidth: Float,
  gridHeight: Float,
  firstColumnWidth: Float
) {
  val halfHeight = gridHeight.div(2)
  val halfWidth = gridWidth.times(0.5f)
  var x = firstColumnWidth + halfWidth
  for (day in days) {
    if (day.isCurrent) {
      drawRoundRect(
        color = currentHighlightColor,
        topLeft = Offset(x.minus(halfWidth), 0f),
        size = Size(gridWidth, gridHeight),
        cornerRadius = CornerRadius(halfHeight, halfHeight)
      )
    }

    drawText(
      textLayoutResult = day.textLayoutResult,
      color = textColor,
      topLeft = Offset(x.minus(day.textLayoutResult.size.width.div(2)), halfHeight.minus(day.textLayoutResult.size.height.div(2)))
    )
    x += gridWidth
  }
}

context(DrawScope)
@OptIn(ExperimentalTextApi::class)
private fun scheduleTableBoxes(
  gridHeight: Float,
  hours: List<DrawableText<Int>>,
  days: List<DrawableText<DayOfWeek>>,
  textColor: Color,
  colorDisabled: Color,
  currentHighlightColor: Color,
  viewState: ScheduleDetailViewState,
  positions: Map<ScheduleDetailEntryBoxKey, Offset>,
  boxSize: Size,
  path: Path,
  cornerRadius: CornerRadius
) {
  var y = gridHeight.times(1.5f)
  for (hour in hours) {
    if (hour.isCurrent) {
      positions[ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, hour.value.toShort())]?.let {
        val halfHeight = gridHeight.div(2f)
        drawCircle(
          color = currentHighlightColor,
          radius = halfHeight,
          center = Offset(hour.textLayoutResult.size.width.div(2f), it.y.plus(halfHeight).minus(boxPadding.toPx()))
        )
      }
    }

    drawText(
      textLayoutResult = hour.textLayoutResult,
      color = textColor,
      topLeft = Offset(0f, y.minus(hour.textLayoutResult.size.height.div(2)))
    )
    y += gridHeight

    for (day in days) {
      val key = ScheduleDetailEntryBoxKey(day.value, hour.value.toShort())
      val entryValue = viewState.schedule[key]
      val entryPosition = positions[key]

      val singleProgram = entryValue?.singleProgram()
      if (singleProgram != null || entryValue == null) {
        val color = singleProgram?.color() ?: colorDisabled
        drawScheduleBoxSingleColor(entryPosition!!, boxSize, cornerRadius, color)
      } else {
        drawScheduleBoxMultiColor(path, entryPosition!!, boxSize, cornerRadius, entryValue)
      }

      if (hour.value == (viewState.currentHour ?: false) && day.value == (viewState.currentDayOfWeek ?: false)) {
        val halfHeight = gridHeight.div(2f)
        path.reset()
        path.moveTo(entryPosition.x + cornerRadius.x, entryPosition.y)
        path.relativeLineTo(halfHeight - cornerRadius.x, 0f)
        path.relativeLineTo(-halfHeight, halfHeight)
        path.relativeLineTo(0f, -halfHeight + cornerRadius.x)
        path.close()

        drawPath(color = Color.Black, path = path)
      }
    }
  }
}

context(DrawScope)
private fun drawScheduleBoxSingleColor(
  topLeft: Offset,
  size: Size,
  cornerRadius: CornerRadius,
  color: Color
) {
  drawRoundRect(
    color = color,
    topLeft = topLeft,
    size = size,
    cornerRadius = cornerRadius
  )
}

context(DrawScope)
private fun drawScheduleBoxMultiColor(
  path: Path,
  topLeft: Offset,
  size: Size,
  cornerRadius: CornerRadius,
  value: ScheduleDetailEntryBoxValue
) {
  val itemWidth = size.width.div(4)
  val itemHeight = size.height
  val quarterSize = Size(itemWidth, itemHeight)

  for (i in 0..3) {
    path.reset()
    val offset = Offset(topLeft.x.plus(itemWidth.times(i)), topLeft.y)
    when (i) {
      0 -> path.addRoundRect(RoundRect(rect = Rect(offset = offset, size = quarterSize), topLeft = cornerRadius, bottomLeft = cornerRadius))
      3 -> path.addRoundRect(
        RoundRect(
          rect = Rect(offset = offset, size = quarterSize),
          topRight = cornerRadius,
          bottomRight = cornerRadius
        )
      )

      else -> path.addRect(rect = Rect(offset = offset, size = quarterSize))
    }
    val color = when (i) {
      0 -> value.firstQuarterProgram.color()
      1 -> value.secondQuarterProgram.color()
      2 -> value.thirdQuarterProgram.color()
      3 -> value.fourthQuarterProgram.color()
      else -> throw IllegalStateException("Wanted to draw to many boxes")
    }
    drawPath(path, color = color)
  }
}

@OptIn(ExperimentalTextApi::class)
private fun labelText(text: String, textMeasurer: TextMeasurer, useBold: Boolean = false): TextLayoutResult {
  val annotatedString = buildAnnotatedString {
    withStyle(
      style = SpanStyle(
        fontSize = textSize,
        fontFamily = if (useBold) textFontBold else textFont
      )
    ) {
      append(text)
    }
  }
  return textMeasurer.measure(annotatedString)
}

private data class DrawableText<T>(
  val value: T,
  val textLayoutResult: TextLayoutResult,
  var isCurrent: Boolean
) {

  companion object {
    @OptIn(ExperimentalTextApi::class)
    fun get(dayOfWeek: DayOfWeek, resources: Resources, textMeasurer: TextMeasurer, isCurrentDay: Boolean): DrawableText<DayOfWeek> =
      DrawableText(
        value = dayOfWeek,
        textLayoutResult = labelText(resources.getString(dayOfWeek.shortText), textMeasurer, useBold = isCurrentDay),
        isCurrent = isCurrentDay
      )

    @OptIn(ExperimentalTextApi::class)
    fun get(hour: Int, textMeasurer: TextMeasurer, isCurrentHour: Boolean): DrawableText<Int> =
      DrawableText(
        value = hour,
        textLayoutResult = labelText(hour.toHour(), textMeasurer, useBold = isCurrentHour),
        isCurrent = isCurrentHour
      )
  }
}

@OptIn(ExperimentalTextApi::class)
private fun mutableStateListOfDrawableDayOfWeek(context: Context, textMeasurer: TextMeasurer) =
  mutableStateListOf<DrawableText<DayOfWeek>>().also { list ->
    DayOfWeek.values().forEach { list.add(DrawableText.get(it, context.resources, textMeasurer, false)) }
  }

@OptIn(ExperimentalTextApi::class)
private fun mutableStateListOfDrawableHour(textMeasurer: TextMeasurer) =
  mutableStateListOf<DrawableText<Int>>().also { list ->
    for (i in 0..23) {
      list.add(DrawableText.get(i, textMeasurer, false))
    }
  }

private fun createBoxSize(viewSize: IntSize?, gridWidth: Float, gridHeight: Float) =
  if (viewSize == null) {
    Size(0f, 0f)
  } else {
    Size(gridWidth.minus(boxSpacing.toPx()), gridHeight.minus(boxSpacing.toPx()))
  }

private fun createBoxesPositions(
  viewSize: IntSize?,
  hours: List<DrawableText<Int>>,
  days: List<DrawableText<DayOfWeek>>,
  textWidth: Float,
  gridHeight: Float,
  gridWidth: Float
) =
  if (viewSize == null) {
    emptyMap()
  } else {
    mutableMapOf<ScheduleDetailEntryBoxKey, Offset>().apply {
      var y = gridHeight
      for (hour in hours) {
        var x = textWidth + boxPadding.toPx()
        for (day in days) {
          put(ScheduleDetailEntryBoxKey(day.value, hour.value.toShort()), Offset(x, y.plus(boxPadding.toPx())))
          x += gridWidth
        }
        y += gridHeight
      }
    }
  }

@Preview
@Composable
private fun Preview() {
  val schedule = mapOf(
    ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_1),
    ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ScheduleDetailEntryBoxValue(
      SuplaScheduleProgram.PROGRAM_1,
      SuplaScheduleProgram.PROGRAM_2,
      SuplaScheduleProgram.OFF,
      SuplaScheduleProgram.PROGRAM_3
    )
  )

  SuplaTheme {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
      ScheduleTable(
        Modifier
          .width(400.dp)
          .height(800.dp)
          .background(Color.White),
        ScheduleDetailViewState(schedule = schedule),
        PreviewProxy(programs = schedule)
      )
    }
  }
}
