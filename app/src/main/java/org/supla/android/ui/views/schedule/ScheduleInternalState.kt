package org.supla.android.ui.views.schedule
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.extensions.toPx

val boxSpacing = boxPadding.times(2)
private val textPadding = 8.dp

data class ScheduleInternalState(
  val viewSize: IntSize?,
  val gridSize: Size,
  val boxSize: Size,
  val boxPositions: Map<ScheduleDetailEntryBoxKey, Offset>,
  val cornerRadius: CornerRadius,
  val firstRowHeight: Float,
  val firstColumnWidth: Float,
  val days: List<DrawableText<DayOfWeek>>,
  val hours: List<DrawableText<Int>>,
  val path: Path = Path()
)

@Composable
fun rememberMutableScheduleSizeState(
  viewSize: IntSize?,
  state: ScheduleTableState<*>,
  useLandscape: Boolean,
  onBoxSizeChanged: ((Size) -> Unit)?
): MutableState<ScheduleInternalState> {
  val context = LocalContext.current
  val textMeasurer = rememberTextMeasurer()
  val radiusSize = dimensionResource(id = R.dimen.radius_small).toPx()

  return remember(viewSize, state.currentDayOfWeek, state.currentHour) {
    val boxPaddingPx = boxPadding.toPx()
    val textPaddingPx = textPadding.toPx()

    val days = listOfDrawableDayOfWeek(context, textMeasurer, state.currentDayOfWeek)
    val hours = listOfDrawableHour(textMeasurer, state.currentHour)

    val firstRowHeight = days.first().textLayoutResult.size.height.plus(boxPaddingPx).plus(textPaddingPx)
    val firstColumnWidth = hours.first().textLayoutResult.size.width.plus(boxPaddingPx).plus(textPaddingPx)
    val gridWidth =
      if (useLandscape) {
        viewSize?.width?.toFloat()?.div(ROWS_COUNT) ?: 0f
      } else {
        viewSize?.width?.toFloat()?.minus(firstColumnWidth)?.div(columnsCount) ?: 0f
      }
    val gridHeight =
      if (useLandscape) {
        viewSize?.height?.toFloat()?.minus(firstRowHeight)?.div(columnsCount) ?: 0f
      } else {
        viewSize?.height?.toFloat()?.div(ROWS_COUNT) ?: 0f
      }

    mutableStateOf(
      ScheduleInternalState(
        viewSize = viewSize,
        gridSize = Size(
          width = gridWidth,
          height = gridHeight
        ),
        boxSize = createBoxSize(viewSize, gridWidth, gridHeight).also { onBoxSizeChanged?.invoke(it) },
        boxPositions =
        if (useLandscape) {
          createBoxesPositionsLandscape(viewSize, hours, days, firstRowHeight, gridHeight, gridWidth, boxPaddingPx)
        } else {
          createBoxesPositionsPortrait(viewSize, hours, days, firstColumnWidth, gridHeight, gridWidth, boxPaddingPx)
        },
        cornerRadius = CornerRadius(radiusSize, radiusSize),
        firstRowHeight = firstRowHeight,
        firstColumnWidth = firstColumnWidth,
        days = days,
        hours = hours
      )
    )
  }
}

private fun listOfDrawableDayOfWeek(context: Context, textMeasurer: TextMeasurer, currentDay: DayOfWeek?) =
  mutableListOf<DrawableText<DayOfWeek>>().also { list ->
    DayOfWeek.entries.forEach { list.add(DrawableText.get(it, context.resources, textMeasurer, it == currentDay)) }
  }

private fun listOfDrawableHour(textMeasurer: TextMeasurer, currentHour: Int?) =
  mutableListOf<DrawableText<Int>>().also { list ->
    for (i in 0..23) {
      list.add(DrawableText.get(i, textMeasurer, i == currentHour))
    }
  }

private fun createBoxSize(viewSize: IntSize?, gridWidth: Float, gridHeight: Float) =
  if (viewSize == null) {
    Size(0f, 0f)
  } else {
    val boxSpacingPx = boxSpacing.toPx()
    Size(gridWidth.minus(boxSpacingPx), gridHeight.minus(boxSpacingPx))
  }

private fun createBoxesPositionsLandscape(
  viewSize: IntSize?,
  hours: List<DrawableText<Int>>,
  days: List<DrawableText<DayOfWeek>>,
  textHeight: Float,
  gridHeight: Float,
  gridWidth: Float,
  boxPadding: Float
) =
  if (viewSize == null) {
    emptyMap()
  } else {
    mutableMapOf<ScheduleDetailEntryBoxKey, Offset>().apply {
      var x = gridWidth
      for (hour in hours) {
        var y = textHeight + boxPadding
        for (day in days) {
          put(ScheduleDetailEntryBoxKey(day.value, hour.value.toShort()), Offset(x.plus(boxPadding), y))
          y += gridHeight
        }
        x += gridWidth
      }
    }
  }

private fun createBoxesPositionsPortrait(
  viewSize: IntSize?,
  hours: List<DrawableText<Int>>,
  days: List<DrawableText<DayOfWeek>>,
  textWidth: Float,
  gridHeight: Float,
  gridWidth: Float,
  boxPadding: Float
) =
  if (viewSize == null) {
    emptyMap()
  } else {
    mutableMapOf<ScheduleDetailEntryBoxKey, Offset>().apply {
      var y = gridHeight
      for (hour in hours) {
        var x = textWidth + boxPadding
        for (day in days) {
          put(ScheduleDetailEntryBoxKey(day.value, hour.value.toShort()), Offset(x, y.plus(boxPadding)))
          x += gridWidth
        }
        y += gridHeight
      }
    }
  }
