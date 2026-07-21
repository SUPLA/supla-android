package org.supla.android.main.view
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalDateTimeFormatter
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.texts.BodyMedium
import org.supla.android.ui.views.texts.BodySmall
import org.supla.android.ui.views.texts.LabelMedium
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

data class EventNotificationState(
  val time: LocalDateTime?,
  val device: String?,
  val actionText: LocalizedString,
  val subjectIcon: ImageId,
  val subjectName: LocalizedString
)

@Composable
fun EventNotificationOverlay(
  state: EventNotificationState,
  modifier: Modifier = Modifier,
  onEventRemoved: () -> Unit = {}
) {
  Surface(
    color = colorResource(R.color.notification_bg),
    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_default)),
    shadowElevation = dimensionResource(R.dimen.custom_shadow_height),
    modifier = modifier
      .fillMaxWidth()
      .swipeToRemove(onRemoved = onEventRemoved)
      .padding(Distance.default)
      .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    Box {
      Column(
        modifier = Modifier.padding(Distance.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Distance.small)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
        ) {
          IconBox { Image(imageId = state.subjectIcon) }
          LabelMedium(
            text = state.subjectName(),
            modifier = Modifier.weight(1f),
            maxLines = 1
          )
          state.time?.let { BodySmall(LocalDateTimeFormatter.current.time(it)) }
        }

        BodyMedium(
          text = if (state.device != null) "${state.device} - ${state.actionText()}" else state.actionText(),
          textAlign = TextAlign.Center
        )
      }

      TimerProgressView(state)
    }
  }
}

@Composable
private fun BoxScope.TimerProgressView(state: EventNotificationState) {
  val progress = remember(state) { Animatable(0f) }

  LaunchedEffect(state) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
    )
  }

  Box(
    modifier = Modifier
      .height(2.dp)
      .fillMaxWidth(progress.value)
      .align(Alignment.BottomStart)
      .background(
        color = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(1.dp)
      )
  )
}

private fun Modifier.swipeToRemove(
  onRemoved: () -> Unit,
  animationDurationMillis: Int = 180
) = composed {
  val scope = rememberCoroutineScope()
  val offset = remember { Animatable(0f) }
  var widthPx by remember { mutableFloatStateOf(0f) }

  val dragState = rememberDraggableState { delta ->
    scope.launch { offset.snapTo(offset.value + delta) }
  }

  onSizeChanged { widthPx = it.width.toFloat() }
    .offset { IntOffset(offset.value.roundToInt(), 0) }
    .draggable(
      orientation = Orientation.Horizontal,
      state = dragState,
      onDragStarted = { scope.launch { offset.stop() } },
      onDragStopped = {
        scope.launch {
          val removalThresholdPx = widthPx / 2f
          if (widthPx > 0f && abs(offset.value) > removalThresholdPx) {
            val targetOffset = if (offset.value > 0) widthPx else -widthPx
            offset.animateTo(targetOffset, animationSpec = tween(durationMillis = animationDurationMillis))
            onRemoved()
          } else {
            offset.animateTo(0f, animationSpec = tween(durationMillis = animationDurationMillis))
          }
        }
      }
    )
}

@Composable
private fun IconBox(content: @Composable BoxScope.() -> Unit) =
  Box(
    modifier = Modifier
      .size(40.dp)
      .background(Color(0xFFFFF7D6), shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small))),
    contentAlignment = Alignment.Center,
    content = content
  )

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    Box(
      modifier = Modifier.fillMaxSize()
    ) {
      EventNotificationOverlay(
        state = EventNotificationState(
          time = LocalDateTime.now(),
          device = "HTC U11",
          actionText = localizedString(R.string.event_poweronoff),
          subjectIcon = ImageId(R.drawable.fnc_light_on),
          subjectName = LocalizedString.Constant("Living Room")
        ),
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }
  }
}
