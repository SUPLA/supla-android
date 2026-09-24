package org.supla.android.ui.views.schedule.editor
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
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
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.ResourceCache
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.schedule.DrawableText
import org.supla.android.ui.views.schedule.MotionEventStateHolder
import org.supla.android.ui.views.schedule.ScheduleDetailEntryBoxKey
import org.supla.android.ui.views.schedule.ScheduleInternalState
import org.supla.android.ui.views.schedule.boxPadding
import org.supla.android.ui.views.schedule.colorRes
import org.supla.android.ui.views.schedule.rememberMutableScheduleSizeState

data class ScheduleTableBox(
  val firstQuarterProgram: SuplaScheduleProgram,
  val secondQuarterProgram: SuplaScheduleProgram,
  val thirdQuarterProgram: SuplaScheduleProgram,
  val fourthQuarterProgram: SuplaScheduleProgram
) {
  constructor(program: SuplaScheduleProgram) : this(program, program, program, program)

  val programs: List<SuplaScheduleProgram>
    get() = listOf(firstQuarterProgram, secondQuarterProgram, thirdQuarterProgram, fourthQuarterProgram)

  val singleProgram: SuplaScheduleProgram?
    get() = firstQuarterProgram.takeIf { program -> programs.all { it == program } }
}

data class ScheduleTableState(
  val schedule: Map<ScheduleDetailEntryBoxKey, ScheduleTableBox> = emptyMap(),
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
fun ScheduleTableScope.ScheduleTable(
  state: ScheduleTableState,
  modifier: Modifier = Modifier,
  onBoxSizeChanged: ((Size) -> Unit)? = null
) {
  val useLandscape = LocalConfiguration.current.isPhoneLandscape
  val resources = LocalResources.current
  val resourceCache = remember { ResourceCache(resources) }

  val (viewSize, updateSize) = remember { mutableStateOf<IntSize?>(null) }
  val internalState by rememberMutableScheduleSizeState(
    viewSize = viewSize,
    currentDayOfWeek = state.currentDayOfWeek,
    currentHour = state.currentHour,
    useLandscape = useLandscape,
    onBoxSizeChanged = onBoxSizeChanged
  )

  ScheduleTableCanvas(
    internalState = internalState,
    modifier = modifier,
    updateSize = updateSize
  ) {
    if (internalState.viewSize == null) {
      return@ScheduleTableCanvas
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
  modifier: Modifier,
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

private fun DrawScope.scheduleTableBoxesLandscape(
  internalState: ScheduleInternalState,
  state: ScheduleTableState,
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

    drawScheduleTableBoxes(
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

private fun DrawScope.scheduleTableBoxesPortrait(
  internalState: ScheduleInternalState,
  state: ScheduleTableState,
  resourceCache: ResourceCache
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

    drawScheduleTableBoxes(
      hour = hour,
      width = internalState.gridSize.height.div(2f),
      internalState = internalState,
      state = state,
      resourceCache = resourceCache
    )
  }
}

private fun DrawScope.drawScheduleTableBoxes(
  hour: DrawableText<Int>,
  width: Float,
  internalState: ScheduleInternalState,
  state: ScheduleTableState,
  resourceCache: ResourceCache
) {
  for (day in internalState.days) {
    val key = ScheduleDetailEntryBoxKey(day.value, hour.value.toShort())
    val box = state.schedule[key]
    val position = internalState.boxPositions.getValue(key)

    if (box == null) {
      drawRoundRect(
        color = resourceCache.color(R.color.disabled),
        topLeft = position,
        size = internalState.boxSize,
        cornerRadius = internalState.cornerRadius
      )
    } else {
      drawScheduleTableBox(box, position, internalState, resourceCache)
    }

    if (hour.value == state.currentHour && day.value == state.currentDayOfWeek) {
      internalState.path.reset()
      internalState.path.moveTo(position.x + internalState.cornerRadius.x, position.y)
      internalState.path.relativeLineTo(width - internalState.cornerRadius.x, 0f)
      internalState.path.relativeLineTo(-width, width)
      internalState.path.relativeLineTo(0f, -width + internalState.cornerRadius.x)
      internalState.path.close()
      drawPath(color = Color.Black, path = internalState.path)
    }
  }
}

private fun DrawScope.drawScheduleTableBox(
  box: ScheduleTableBox,
  position: Offset,
  internalState: ScheduleInternalState,
  resourceCache: ResourceCache
) {
  if (box.singleProgram != null) {
    drawRoundRect(
      color = resourceCache.color(box.firstQuarterProgram.colorRes()),
      topLeft = position,
      size = internalState.boxSize,
      cornerRadius = internalState.cornerRadius
    )
    return
  }

  val partWidth = internalState.boxSize.width.div(box.programs.size)
  val partSize = Size(partWidth, internalState.boxSize.height)

  box.programs.forEachIndexed { index, program ->
    val partPosition = Offset(position.x.plus(partWidth.times(index)), position.y)
    internalState.path.reset()
    when (index) {
      0 -> internalState.path.addRoundRect(
        RoundRect(
          rect = Rect(offset = partPosition, size = partSize),
          topLeft = internalState.cornerRadius,
          bottomLeft = internalState.cornerRadius
        )
      )
      box.programs.lastIndex -> internalState.path.addRoundRect(
        RoundRect(
          rect = Rect(offset = partPosition, size = partSize),
          topRight = internalState.cornerRadius,
          bottomRight = internalState.cornerRadius
        )
      )
      else -> internalState.path.addRect(Rect(offset = partPosition, size = partSize))
    }
    drawPath(path = internalState.path, color = resourceCache.color(program.colorRes()))
  }
}

private val previewScheduleTableScope = object : ScheduleTableScope {
  override fun onScheduleTableLongPress(key: ScheduleDetailEntryBoxKey?) {}
  override fun onScheduleTableTouched(key: ScheduleDetailEntryBoxKey) {}
  override fun onScheduleTableReload() {}
  override fun onScheduleTableInvalidate() {}
}

private val previewScheduleTableState = ScheduleTableState(
  schedule = mapOf(
    ScheduleDetailEntryBoxKey(DayOfWeek.TUESDAY, 3) to ScheduleTableBox(SuplaScheduleProgram.PROGRAM_4),
    ScheduleDetailEntryBoxKey(DayOfWeek.THURSDAY, 5) to ScheduleTableBox(
      firstQuarterProgram = SuplaScheduleProgram.PROGRAM_1,
      secondQuarterProgram = SuplaScheduleProgram.PROGRAM_2,
      thirdQuarterProgram = SuplaScheduleProgram.OFF,
      fourthQuarterProgram = SuplaScheduleProgram.PROGRAM_3
    )
  ),
  currentDayOfWeek = DayOfWeek.MONDAY,
  currentHour = 12
)

@SuplaPreview
@Composable
private fun ScheduleTablePreview() {
  ScheduleTablePreviewContent()
}

@SuplaPreviewLandscape
@Composable
private fun ScheduleTableLandscapePreview() {
  ScheduleTablePreviewContent()
}

@Composable
private fun ScheduleTablePreviewContent() {
  SuplaTheme {
    Column(
      verticalArrangement = Arrangement.spacedBy(1.dp),
      modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
      previewScheduleTableScope.ScheduleTable(
        state = previewScheduleTableState,
        modifier = Modifier
          .systemBarsPadding()
          .fillMaxSize()
      )
    }
  }
}
