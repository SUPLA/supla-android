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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.features.details.thermostatdetail.schedule.data.ThermostatScheduleDetailEntryBoxValue
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.ResourceCache
import org.supla.android.ui.extensions.isPhoneLandscape

const val ROWS_COUNT = 25
val columnsCount = DayOfWeek.entries.size
val boxPadding = 2.dp

data class ScheduleTableState<Value : ScheduleDetailEntryBoxValue>(
  val schedule: Map<ScheduleDetailEntryBoxKey, Value> = emptyMap(),
  val currentDayOfWeek: DayOfWeek? = null,
  val currentHour: Int? = null
)

interface ScheduleTableScope {
  fun onScheduleTableLongPress(key: ScheduleDetailEntryBoxKey?)
  fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey)
  fun onScheduleTableReload()
  fun onScheduleTableInvalidate()
}

@Composable
fun <Value : ScheduleDetailEntryBoxValue> ScheduleTableScope.ScheduleTable(
  state: ScheduleTableState<Value>,
  modifier: Modifier = Modifier,
  onBoxSizeChanged: ((Size) -> Unit)? = null
) {
  val configuration = LocalConfiguration.current
  val useLandscape = configuration.isPhoneLandscape
  val resources = LocalResources.current
  val resourceCache = remember { ResourceCache(resources) }

  val (viewSize, updateSize) = remember { mutableStateOf<IntSize?>(null) }
  val internalState by rememberMutableScheduleSizeState(viewSize, state, useLandscape, onBoxSizeChanged)

  ScheduleTableCanvas(
    internalState = internalState,
    modifier = modifier,
    updateSize = updateSize
  ) {
    if (internalState.viewSize == null) {
      return@ScheduleTableCanvas // Skip drawing when view size is not set yet
    }

    if (useLandscape) {
      scheduleTableDaysLandscape(internalState, resourceCache)
      scheduleTableBoxesLandscape(internalState, state, resourceCache)
    } else {
      scheduleTableDaysPortrait(internalState, resourceCache)
      scheduleTableBoxesPortrait(internalState, state, resourceCache)
    }
  }
}

@Composable
private fun ScheduleTableScope.ScheduleTableCanvas(
  internalState: ScheduleInternalState,
  modifier: Modifier = Modifier,
  updateSize: (IntSize?) -> Unit,
  onDraw: DrawScope.() -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val eventStateHolder by remember(internalState.viewSize) {
    mutableStateOf(MotionEventStateHolder(null, internalState.boxPositions, internalState.boxSize))
  }

  Canvas(
    modifier = modifier
      .onSizeChanged(updateSize)
      .pointerInteropFilter { event ->
        eventStateHolder.handleEvent(
          event = event,
          context = context,
          coroutineScope = coroutineScope,
          onLongPress = { onScheduleTableLongPress(it) },
          onTouched = { onScheduleTableTouched(it) },
          onInvalidate = { onScheduleTableInvalidate() },
          onFinished = { onScheduleTableReload() }
        )
      },
    onDraw = onDraw
  )
}

private fun DrawScope.scheduleTableDaysLandscape(
  internalState: ScheduleInternalState,
  resourceCache: ResourceCache
) {
  val halfHeight = internalState.gridSize.height.div(2)
  val halfWidth = internalState.gridSize.width.times(0.5f)
  var y = internalState.firstRowHeight + halfHeight
  for (day in internalState.days) {
    if (day.isCurrent) {
      drawCircle(
        color = resourceCache.color(R.color.highlight_color),
        radius = halfWidth,
        center = Offset(halfWidth, y)
      )
    }

    drawText(
      textLayoutResult = day.textLayoutResult,
      color = resourceCache.color(R.color.on_background),
      topLeft = Offset(halfWidth.minus(day.textLayoutResult.size.width.div(2)), y.minus(day.textLayoutResult.size.height.div(2)))
    )
    y += internalState.gridSize.height
  }
}

private fun <Value : ScheduleDetailEntryBoxValue> DrawScope.scheduleTableBoxesLandscape(
  internalState: ScheduleInternalState,
  state: ScheduleTableState<Value>,
  resourceCache: ResourceCache
) {
  var x = internalState.gridSize.width.times(1.5f)
  for (hour in internalState.hours) {
    if (hour.isCurrent) {
      internalState.boxPositions[ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, hour.value.toShort())]?.let {
        val halfWidth = internalState.gridSize.width.div(2f)
        val boxPaddingPx = boxPadding.toPx()
        drawCircle(
          color = resourceCache.color(R.color.highlight_color),
          radius = halfWidth,
          center = Offset(it.x.plus(halfWidth).minus(boxPaddingPx), it.y.minus(halfWidth).minus(boxPaddingPx))
        )
      }
    }

    drawText(
      textLayoutResult = hour.textLayoutResult,
      color = resourceCache.color(R.color.on_background),
      topLeft = Offset(x.minus(hour.textLayoutResult.size.height.div(2)), 0f)
    )
    x += internalState.gridSize.width

    drawBoxes(
      hour = hour,
      width = internalState.gridSize.width.div(2f),
      internalState = internalState,
      state = state,
      resourceCache = resourceCache
    )
  }
}

private fun DrawScope.scheduleTableDaysPortrait(
  internalState: ScheduleInternalState,
  resourceCache: ResourceCache
) {
  val halfHeight = internalState.gridSize.height.div(2)
  val halfWidth = internalState.gridSize.width.times(0.5f)

  var x = internalState.firstColumnWidth + halfWidth
  for (day in internalState.days) {
    if (day.isCurrent) {
      drawRoundRect(
        color = resourceCache.color(R.color.highlight_color),
        topLeft = Offset(x.minus(halfWidth), 0f),
        size = internalState.gridSize,
        cornerRadius = CornerRadius(halfHeight, halfHeight)
      )
    }

    drawText(
      textLayoutResult = day.textLayoutResult,
      color = resourceCache.color(R.color.on_background),
      topLeft = Offset(
        x = x.minus(day.textLayoutResult.size.width.div(2)),
        y = halfHeight.minus(day.textLayoutResult.size.height.div(2))
      )
    )
    x += internalState.gridSize.width
  }
}

private fun <Value : ScheduleDetailEntryBoxValue> DrawScope.scheduleTableBoxesPortrait(
  internalState: ScheduleInternalState,
  state: ScheduleTableState<Value>,
  resourceCache: ResourceCache,
) {
  var y = internalState.gridSize.height.times(1.5f)
  for (hour in internalState.hours) {
    if (hour.isCurrent) {
      internalState.boxPositions[ScheduleDetailEntryBoxKey(DayOfWeek.MONDAY, hour.value.toShort())]?.let {
        val halfHeight = internalState.gridSize.height.div(2f)
        drawCircle(
          color = resourceCache.color(R.color.highlight_color),
          radius = halfHeight,
          center = Offset(hour.textLayoutResult.size.width.div(2f), it.y.plus(halfHeight).minus(boxPadding.toPx()))
        )
      }
    }

    drawText(
      textLayoutResult = hour.textLayoutResult,
      color = resourceCache.color(R.color.on_background),
      topLeft = Offset(0f, y.minus(hour.textLayoutResult.size.height.div(2)))
    )
    y += internalState.gridSize.height

    drawBoxes(
      hour = hour,
      width = internalState.gridSize.height.div(2f),
      internalState = internalState,
      state = state,
      resourceCache = resourceCache
    )
  }
}

private fun DrawScope.drawBoxes(
  hour: DrawableText<Int>,
  width: Float,
  internalState: ScheduleInternalState,
  state: ScheduleTableState<*>,
  resourceCache: ResourceCache
) {
  for (day in internalState.days) {
    val key = ScheduleDetailEntryBoxKey(day.value, hour.value.toShort())
    val entryValue = state.schedule[key]
    val entryPosition = internalState.boxPositions[key]

    if (entryValue != null) {
      entryValue.drawBox(
        drawScope = this@drawBoxes,
        topLeft = entryPosition!!,
        size = internalState.boxSize,
        cornerRadius = internalState.cornerRadius,
        resourceCache = resourceCache
      )
    } else {
      drawDefaultBox(entryPosition!!, internalState.boxSize, internalState.cornerRadius, resourceCache.color(R.color.disabled))
    }

    if (hour.value == (state.currentHour ?: false) && day.value == (state.currentDayOfWeek ?: false)) {
      internalState.path.reset()
      internalState.path.moveTo(entryPosition.x + internalState.cornerRadius.x, entryPosition.y)
      internalState.path.relativeLineTo(width - internalState.cornerRadius.x, 0f)
      internalState.path.relativeLineTo(-width, width)
      internalState.path.relativeLineTo(0f, -width + internalState.cornerRadius.x)
      internalState.path.close()

      drawPath(color = Color.Black, path = internalState.path)
    }
  }
}

private fun DrawScope.drawDefaultBox(
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

private val previewScope = object : ScheduleTableScope {
  override fun onScheduleTableLongPress(key: ScheduleDetailEntryBoxKey?) {}
  override fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey) {}
  override fun onScheduleTableReload() {}
  override fun onScheduleTableInvalidate() {}
}

@SuplaPreview
@Composable
private fun Preview() {
  val schedule = mapOf(
    ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ThermostatScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_4),
    ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ThermostatScheduleDetailEntryBoxValue(
      SuplaScheduleProgram.PROGRAM_1,
      SuplaScheduleProgram.PROGRAM_2,
      SuplaScheduleProgram.OFF,
      SuplaScheduleProgram.PROGRAM_3
    )
  )

  SuplaTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(1.dp),
      modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
      previewScope.ScheduleTable(
        ScheduleTableState(schedule = schedule, currentDayOfWeek = DayOfWeek.MONDAY, currentHour = 12),
        Modifier
          .systemBarsPadding()
          .fillMaxSize()
      )
    }
  }
}

@SuplaPreviewLandscape
@Composable
private fun PreviewLandscape() {
  val schedule = mapOf(
    ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ThermostatScheduleDetailEntryBoxValue(SuplaScheduleProgram.PROGRAM_4),
    ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ThermostatScheduleDetailEntryBoxValue(
      SuplaScheduleProgram.PROGRAM_1,
      SuplaScheduleProgram.PROGRAM_2,
      SuplaScheduleProgram.OFF,
      SuplaScheduleProgram.PROGRAM_3
    )
  )

  SuplaTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(1.dp),
      modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
      previewScope.ScheduleTable(
        ScheduleTableState(schedule = schedule, currentDayOfWeek = DayOfWeek.MONDAY, currentHour = 12),
        Modifier
          .systemBarsPadding()
          .fillMaxSize()
      )
    }
  }
}
