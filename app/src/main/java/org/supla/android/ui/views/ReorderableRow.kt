package org.supla.android.ui.views
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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import org.supla.android.core.ui.theme.Distance
import org.supla.android.tools.ComponentPreview
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun <T> ReorderableRow(
  items: List<T>,
  modifier: Modifier = Modifier,
  onRemove: (index: Int) -> Unit,
  onMove: (from: Int, to: Int) -> Unit,
  leadingContent: @Composable ((dragging: Boolean, itemOver: Boolean) -> Unit)? = null,
  trailingContent: @Composable ((dragging: Boolean, itemOver: Boolean) -> Unit)? = null,
  itemContent: @Composable (T) -> Unit
) {
  var internList by remember { mutableStateOf(items.toMutableList()) }

  var initialDragIndex by remember { mutableStateOf<Int?>(null) }
  var draggingIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetX by remember { mutableStateOf(0f) }
  val itemRects = remember { mutableStateListOf<IntRect>() }

  val isDragging = draggingIndex != null

  var leadingRect by remember { mutableStateOf<IntRect?>(null) }
  var overLeadingContent by remember { mutableStateOf(false) }
  var trailingRect by remember { mutableStateOf<IntRect?>(null) }
  var overTrailingContent by remember { mutableStateOf(false) }

  val haptic = LocalHapticFeedback.current

  LaunchedEffect(items) {
    if (!isDragging) {
      internList = items.toMutableList()
    }
  }

  fun onDrag(change: PointerInputChange, dragOffset: Offset) {
    change.consume()

    dragOffsetX += dragOffset.x

    val dragItemIndex = draggingIndex ?: return
    val dragItemRect = itemRects.getOrNull(dragItemIndex) ?: return

    val dragItemEndPosition = dragItemRect.right + dragOffsetX
    val dragItemStartPosition = dragItemRect.left + dragOffsetX
    overLeadingContent = leadingRect?.let { dragItemStartPosition < it.right } ?: false
    overTrailingContent = trailingRect?.let { dragItemEndPosition > it.left } ?: false

    for (index in 0 ..< itemRects.size) {
      val currentItemRect = itemRects.getOrNull(index) ?: return
      val currentItemCenterPosition = currentItemRect.center.x

      if (dragItemIndex > index && dragItemStartPosition < currentItemCenterPosition) {
        internList.add(index, internList.removeAt(dragItemIndex))
        draggingIndex = index
        dragOffsetX += dragItemRect.width
      } else if (dragItemIndex < index && dragItemEndPosition > currentItemCenterPosition) {
        internList.add(index, internList.removeAt(dragItemIndex))
        draggingIndex = index
        dragOffsetX -= dragItemRect.width
      }
    }
  }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    leadingContent?.let { content ->
      Box(
        modifier = Modifier.onGloballyPositioned { leadingRect = IntRect(it.positionInParent().round(), it.size) }
      ) {
        content(isDragging, overLeadingContent)
      }
    }

    internList.forEachIndexed { index, item ->
      val isCurrentDragging = draggingIndex == index

      Box(
        modifier = Modifier
          .offset {
            if (isCurrentDragging) {
              IntOffset(dragOffsetX.roundToInt(), 0)
            } else {
              IntOffset.Zero
            }
          }
          .zIndex(if (isCurrentDragging) 1f else 0f)
          .onGloballyPositioned { coords ->
            if (itemRects.getOrNull(index) == null) {
              itemRects.add(IntRect(coords.positionInParent().round(), coords.size))
            }
          }
          .pointerInput(index) {
            detectDragGesturesAfterLongPress(
              onDragStart = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                initialDragIndex = index
                draggingIndex = index
                dragOffsetX = 0f
                overLeadingContent = false
                overTrailingContent = false
              },
              onDrag = { change, dragAmount -> onDrag(change, dragAmount) },
              onDragEnd = {
                if (overLeadingContent && draggingIndex != null) {
                  initialDragIndex?.let { onRemove(it) }
                } else {
                  val from = initialDragIndex
                  val to = draggingIndex
                  if (from != null && to != null) {
                    onMove(from, to)
                  }
                }

                draggingIndex = null
                dragOffsetX = 0f
                overLeadingContent = false
                overTrailingContent = false
              },
              onDragCancel = {
                draggingIndex = null
                dragOffsetX = 0f
                overLeadingContent = false
                overTrailingContent = false
              }
            )
          }
      ) {
        itemContent(item)
      }
    }

    trailingContent?.let { content ->
      Box(
        modifier = Modifier.onGloballyPositioned { trailingRect = IntRect(it.positionInParent().round(), it.size) }
      ) {
        content(isDragging, overTrailingContent)
      }
    }
  }
}

@ComponentPreview
@Composable
private fun Preview() {
  val items = remember { mutableListOf("A", "B", "C", "D") }

  ReorderableRow(
    items = items,
    onRemove = { index ->
      Timber.d("Remove $index")
    },
    onMove = { from, to ->
      Timber.d("Move from $from to $to")
    },
    leadingContent = { dragging, itemOver ->
      PreviewText("L", dragging, itemOver)
    },
    trailingContent = { dragging, itemOver ->
      PreviewText("T", dragging, itemOver)
    }
  ) {
    PreviewText(text = it, false, false)
  }
}

@Composable
private fun PreviewText(text: String, isCurrentDragging: Boolean, itemOver: Boolean) =
  Text(
    text = text,
    modifier = Modifier
      .padding(horizontal = Distance.small)
      .background(
        color = if (itemOver) Color.Red else if (isCurrentDragging) Color.LightGray else Color.White,
        shape = RoundedCornerShape(8.dp)
      )
      .border(
        width = 1.dp,
        color = Color.Gray,
        shape = RoundedCornerShape(8.dp)
      )
      .padding(8.dp)
  )
