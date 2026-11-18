package org.supla.android.ui.views.channelstate
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.formatting.LocalTimeFormatter
import org.supla.android.extensions.ucFirst
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.layouts.AdaptiveTwoContentLayout
import org.supla.android.ui.views.Image
import org.supla.android.ui.views.texts.BodySmall
import org.supla.core.shared.infrastructure.LocalizedString
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

data class ChannelCurrentData(
  val label: LocalizedString,
  val icon: ImageId?,
  val value: LocalizedString,
  val timerEndTime: LocalDateTime? = null,
  val iconTint: Int? = null,
  val iconSize: Dp = 24.dp
)

@Composable
fun ChannelCurrentState(
  data: ChannelCurrentData,
  modifier: Modifier = Modifier
) {
  ChannelCurrentState(
    stateLabel = data.label(LocalContext.current),
    icon = data.icon,
    stateValue = data.value(LocalContext.current),
    modifier = modifier,
    timerEndTime = data.timerEndTime,
    iconTint = data.iconTint
  )
}

@Composable
fun ChannelCurrentState(
  stateLabel: String,
  icon: ImageId?,
  stateValue: String,
  modifier: Modifier = Modifier,
  timerEndTime: LocalDateTime? = null,
  iconTint: Int? = null,
  iconSize: Dp = 24.dp
) =
  Row(
    verticalAlignment = if (timerEndTime == null) Alignment.CenterVertically else Alignment.Top,
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny),
    modifier = modifier
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = stateLabel.uppercase(),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    icon?.let { icon ->
      Image(
        imageId = icon,
        contentDescription = null,
        tint = iconTint?.let { colorResource(it) },
        size = iconSize
      )
    }
    if (timerEndTime != null) {
      AdaptiveTwoContentLayout(
        first = { ValueText(stateValue) },
        second = { TimerRow(timerEndTime) }
      )
    } else {
      ValueText(stateValue)
    }
    Spacer(modifier = Modifier.weight(1f))
  }

@Composable
private fun ValueText(text: String) =
  Text(
    text = text.ucFirst(),
    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
  )

@Composable
private fun TimerRow(timerEndTime: LocalDateTime) {
  val formatter = LocalTimeFormatter.current
  var time: String? by remember { mutableStateOf(null) }
  var seconds: Int? by remember { mutableStateOf(null) }

  LaunchedEffect(timerEndTime) {
    while (true) {
      val now = LocalDateTime.now()
      val left = Duration.between(now, timerEndTime)
      val leftSeconds = left.seconds.toInt()
      Timber.d("left seconds: $leftSeconds")
      Timber.d("now: $now, end: $timerEndTime")
      if (left.isNegative) {
        time = null
        seconds = null
        break
      }
      time = formatter.format(leftSeconds, true)
      seconds = leftSeconds
      delay(1.seconds)
    }
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    val displayTime = time
    val displaySeconds = seconds
    if (displayTime != null && displaySeconds != null) {
      Image(
        drawableId = R.drawable.ic_timer,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp)
      )
      BodySmall(text = stringResource(R.string.channel_current_state_left, displayTime, displaySeconds))
    }
  }
}

@Composable
@SuplaPreview
private fun Preview() {
  SuplaTheme {
    Column(
      modifier = Modifier.fillMaxWidth().padding(Distance.default),
      verticalArrangement = Arrangement.spacedBy(Distance.small)
    ) {
      ChannelCurrentState(
        stateLabel = "State",
        icon = null,
        stateValue = "On"
      )
      ChannelCurrentState(
        stateLabel = stringResource(R.string.action_trigger_schedule_button_mode),
        icon = null,
        stateValue = stringResource(R.string.action_trigger_temporary_unlocked),
        timerEndTime = LocalDateTime.now().plusMinutes(5)
      )
    }
  }
}
