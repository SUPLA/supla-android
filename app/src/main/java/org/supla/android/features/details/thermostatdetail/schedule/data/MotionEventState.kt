package org.supla.android.features.details.thermostatdetail.schedule.data
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
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.supla.android.extensions.ifLet
import org.supla.android.extensions.toPx
import org.supla.android.features.details.thermostatdetail.schedule.ui.ScheduleDetailViewProxy
import kotlin.math.pow
import kotlin.math.sqrt

data class MotionEventStateHolder(
  private var state: MotionEventState?,
  private val boxes: Map<ScheduleDetailEntryBoxKey, Offset>,
  private val boxSize: Size
) {

  fun handleEvent(
    event: MotionEvent,
    viewProxy: ScheduleDetailViewProxy,
    context: Context,
    coroutineScope: CoroutineScope
  ): Boolean {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> handleDownEvent(event, boxes, boxSize, coroutineScope, context, viewProxy)
      MotionEvent.ACTION_MOVE -> handleMoveEvent(event, boxes, boxSize, viewProxy, context)
      MotionEvent.ACTION_UP -> handleUpEvent(boxes, event, boxSize, viewProxy, context)
      else -> viewProxy.invalidateSchedule()
    }

    return true
  }

  private fun handleDownEvent(
    event: MotionEvent,
    boxes: Map<ScheduleDetailEntryBoxKey, Offset>,
    boxSize: Size,
    coroutineScope: CoroutineScope,
    context: Context,
    viewProxy: ScheduleDetailViewProxy
  ) {
    val eventOffset = Offset(event.rawX, event.rawY)

    state = MotionEventState(
      downPosition = eventOffset,
      downPositionBox = boxes.entries.firstOrNull { event.inside(it.value, boxSize) }?.key,
      maxMovePosition = eventOffset
    )

    // Start long press watcher
    coroutineScope.launch {
      val initialState = state
      delay(MotionEventState.LONG_PRESS_TIME_MS.toLong())
      if (initialState === state && state?.longPressed(context) == true) {
        state?.consume()
        viewProxy.startQuartersDialog(state?.downPositionBox)
      }
    }
  }

  private fun handleMoveEvent(
    event: MotionEvent,
    boxes: Map<ScheduleDetailEntryBoxKey, Offset>,
    boxSize: Size,
    viewProxy: ScheduleDetailViewProxy,
    context: Context
  ) {
    if (state?.isConsumed() == true) {
      return // Long press watcher used this event
    }

    // Mark touched boxes
    if (state?.moved(context) == true) {
      boxes.entries.firstOrNull { event.inside(it.value, boxSize) }?.key?.let { viewProxy.changeScheduleEntry(it) }
    }

    // Update distance for long press
    val moveOffset = Offset(event.rawX, event.rawY)
    ifLet(state?.downPosition, state?.maxMovePosition) { (downPosition, maxMovePosition) ->
      val newDistance = event.distance(downPosition)
      val oldDistance = downPosition.distance(maxMovePosition)
      if (newDistance > oldDistance) {
        state?.maxMovePosition = moveOffset
      }
    }
    state?.moved = true
  }

  private fun handleUpEvent(
    boxes: Map<ScheduleDetailEntryBoxKey, Offset>,
    event: MotionEvent,
    boxSize: Size,
    viewProxy: ScheduleDetailViewProxy,
    context: Context
  ) {
    if (state?.isConsumed() == false) {
      if (state?.moved(context) == false) {
        boxes.entries.firstOrNull { event.inside(it.value, boxSize) }?.key?.let { viewProxy.changeScheduleEntry(it) }
      }
      viewProxy.updateSchedule()
    }
    state = null
  }
}

data class MotionEventState(
  val downPosition: Offset,
  var downPositionBox: ScheduleDetailEntryBoxKey?,
  var maxMovePosition: Offset,
  var moved: Boolean = false,
  private var consumed: Boolean = false
) {

  fun longPressed(context: Context): Boolean =
    longPressDistanceNotReached(context)

  fun consume() {
    consumed = true
  }

  fun isConsumed(): Boolean = consumed

  fun moved(context: Context): Boolean =
    maxMovePosition.distance(downPosition) > LONG_PRESS_TOLERANCE_DP.toPx(context)

  private fun longPressDistanceNotReached(context: Context): Boolean =
    maxMovePosition.distance(downPosition) < LONG_PRESS_TOLERANCE_DP.toPx(context)

  companion object {
    const val LONG_PRESS_TOLERANCE_DP = 8
    const val LONG_PRESS_TIME_MS = 500
  }
}

private fun Offset.distance(fromPoint: Offset): Float =
  sqrt((x - fromPoint.x).pow(2) + (y - fromPoint.y).pow(2))

private fun MotionEvent.inside(topLeft: Offset, size: Size) =
  x > topLeft.x &&
    y > topLeft.y &&
    x < topLeft.x + size.width &&
    y < topLeft.y + size.height

private fun MotionEvent.distance(fromPoint: Offset): Float =
  sqrt((x - fromPoint.x).pow(2) + (y - fromPoint.y).pow(2))
