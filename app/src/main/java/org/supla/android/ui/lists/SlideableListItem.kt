package org.supla.android.ui.lists
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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.storage.LocalApplicationPreferences
import org.supla.android.extensions.isNotNull
import org.supla.android.ui.views.texts.LabelLarge
import org.supla.core.shared.infrastructure.LocalizedString
import sh.calvin.reorderable.ReorderableCollectionItemScope
import kotlin.math.roundToInt

@Composable
fun ReorderableCollectionItemScope.SlideableListItem(
  objectId: Int,
  initialOffset: Float,
  onOffsetChanged: (Float) -> Unit,
  isDragging: Boolean,
  dragEnabled: Boolean,
  modifier: Modifier = Modifier,
  onLeftButtonClick: () -> Unit = {},
  onRightButtonClick: () -> Unit = {},
  onDragStarted: (Offset) -> Unit = {},
  onDragStopped: () -> Unit = {},
  leftButtonString: LocalizedString? = null,
  rightButtonString: LocalizedString? = null,
  content: @Composable BoxScope.() -> Unit
) {
  val scope = rememberCoroutineScope()
  val density = LocalDensity.current
  val offset = remember { Animatable(initialOffset) }
  val actionWidth = dimensionResource(R.dimen.channel_layout_button_width)
  val actionWidthPx = with(density) { actionWidth.toPx() }
  val thresholdPx = actionWidthPx * 0.35f

  val dragState = rememberDraggableState { delta ->
    val minOffset = if (rightButtonString.isNotNull) -actionWidthPx else 0f
    val maxOffset = if (leftButtonString.isNotNull) actionWidthPx else 0f
    scope.launch {
      offset.snapTo((offset.value + delta).coerceIn(minOffset, maxOffset))
    }
  }

  val controller = LocalSlideableController.current
  val preferences = LocalApplicationPreferences.current
  LaunchedEffect(objectId) {
    controller.events.collect {
      when (it) {
        SlideableListEvent.ScrollStarted -> offset.animateTo(0f)
        SlideableListEvent.ListButtonClick ->
          if (preferences.isButtonAutohide) {
            offset.animateTo(0f)
            onOffsetChanged(0f) // Needed because button click is handled only internally inside SlideableListItem
          }
        is SlideableListEvent.DragStarted ->
          if (it.objectId != objectId) {
            offset.animateTo(0f)
          }
      }
    }
  }

  val defaultItemHeight = dimensionResource(R.dimen.channel_layout_height)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(defaultItemHeight.times(LocalApplicationPreferences.current.channelHeight.div(100f)))
      .background(MaterialTheme.colorScheme.surface.copy(if (isDragging) 0.8f else 1f))
      .shadow(if (isDragging) 2.dp else 0.dp)
      .longPressDraggableHandle(enabled = dragEnabled, onDragStarted = onDragStarted, onDragStopped = onDragStopped)
      .clipToBounds()
  ) {
    leftButtonString?.let {
      ActionPane(
        onClick = {
          onLeftButtonClick()
          scope.launch { controller.emit(SlideableListEvent.ListButtonClick) }
        },
        modifier = Modifier.align(Alignment.CenterStart),
        transformation = { it.leftButtonTransformation(if (offset.value > 0) offset.value else 0f, actionWidthPx) },
        actionString = leftButtonString
      )
    }

    rightButtonString?.let {
      ActionPane(
        onClick = {
          onRightButtonClick()
          scope.launch { controller.emit(SlideableListEvent.ListButtonClick) }
        },
        modifier = Modifier.align(Alignment.CenterEnd),
        transformation = { it.rightButtonTransformation(if (offset.value < 0) offset.value else 0f, actionWidthPx) },
        actionString = rightButtonString
      )
    }

    Box(
      modifier = Modifier
        .offset(x = with(density) { offset.value.toDp() })
        .draggable(
          orientation = Orientation.Horizontal,
          state = dragState,
          onDragStarted = { controller.emit(SlideableListEvent.DragStarted(objectId)) },
          onDragStopped = {
            scope.launch {
              val target = when {
                offset.value > thresholdPx && leftButtonString.isNotNull -> actionWidthPx
                offset.value < -thresholdPx && rightButtonString.isNotNull -> -actionWidthPx
                else -> 0f
              }
              offset.animateTo(target, animationSpec = tween(durationMillis = 180))
              onOffsetChanged(target)
            }
          }
        ),
      contentAlignment = Alignment.Center,
      content = content
    )
  }
}

@Composable
private fun ActionPane(
  actionString: LocalizedString,
  modifier: Modifier = Modifier,
  transformation: (Modifier) -> Modifier,
  onClick: () -> Unit
) =
  Box(
    modifier = modifier
      .fillMaxHeight()
      .width(dimensionResource(R.dimen.channel_layout_button_width))
      .clickable(
        onClick = onClick,
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      )
      .let { transformation(it) }
      .background(MaterialTheme.colorScheme.primaryContainer),
    contentAlignment = Alignment.Center
  ) {
    LabelLarge(
      text = actionString(),
      color = MaterialTheme.colorScheme.onPrimaryContainer,
      textAlign = TextAlign.Center
    )
  }

fun Modifier.leftButtonTransformation(
  offset: Float,
  itemWidthPx: Float
): Modifier {
  val percentage = ((offset * 100f) / itemWidthPx)
    .coerceIn(0f, 100f)

  val rotation = 90f - 90f * percentage / 100f

  val offsetX = (offset / 2f - itemWidthPx / 2f)
    .coerceAtMost(0f)

  return this
    .offset { IntOffset(x = offsetX.roundToInt(), y = 0) }
    .graphicsLayer {
      rotationY = rotation
    }
}

fun Modifier.rightButtonTransformation(
  offset: Float,
  itemWidthPx: Float
): Modifier {
  val percentage = ((-offset * 100f) / itemWidthPx)
    .coerceIn(0f, 100f)

  val rotation = 90f * percentage / 100f - 90f

  val offsetX = (itemWidthPx / 2f + offset / 2f)
    .coerceAtLeast(0f)

  return this
    .offset { IntOffset(x = offsetX.roundToInt(), y = 0) }
    .graphicsLayer { rotationY = rotation }
}

class SlideableController {
  private val _events = MutableSharedFlow<SlideableListEvent>()
  val events = _events.asSharedFlow()

  suspend fun emit(event: SlideableListEvent) {
    _events.emit(event)
  }
}

interface SlideableListEvent {
  data class DragStarted(val objectId: Int) : SlideableListEvent
  data object ScrollStarted : SlideableListEvent
  data object ListButtonClick : SlideableListEvent
}

private val DefaultSlideableController = SlideableController()

val LocalSlideableController = staticCompositionLocalOf { DefaultSlideableController }
