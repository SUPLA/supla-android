package org.supla.android.ui.views.forms
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.features.details.switchdetail.timer.TimeValues
import org.supla.android.tools.SuplaPreview

@Composable
fun TimePicker(
  timeInSeconds: Int,
  onTimeChanged: (Int) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  maxTimeInSeconds: Int = 86_399
) {
  val maxTime = maxTimeInSeconds.coerceAtLeast(0)
  val selectedTime = timeInSeconds.coerceIn(0, maxTime)
  val values = TimeValues.of(selectedTime.toLong())
  val hoursRange = 0..(maxTime / 3600)
  val maxMinutes = ((maxTime - values.hours * 3600) / 60).coerceIn(0, 59)
  val minutesRange = 0..maxMinutes
  val maxSeconds = (maxTime - values.hours * 3600 - values.minutes * 60).coerceIn(0, 59)
  val secondsRange = 0..maxSeconds

  Box(modifier = modifier.width(IntrinsicSize.Max).height(IntrinsicSize.Max)) {
    NumberPickerHighlight()
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.default),
      modifier = Modifier
        .align(Alignment.Center)
        .clip(MaterialTheme.shapes.extraSmall)
    ) {
      NumberPicker(
        range = hoursRange,
        selectedValue = values.hours,
        enabled = enabled,
        formatter = { context, value -> context.resources.getQuantityString(R.plurals.hour_pattern, value, value) },
        onValueChanged = { onTimeChanged(it.times(3600) + values.minutes.times(60) + values.seconds) }
      )
      NumberPicker(
        range = minutesRange,
        selectedValue = values.minutes,
        enabled = enabled,
        formatter = { context, value -> context.resources.getQuantityString(R.plurals.minute_pattern, value, value) },
        onValueChanged = { onTimeChanged(values.hours.times(3600) + it.times(60) + values.seconds) }
      )
      NumberPicker(
        range = secondsRange,
        selectedValue = values.seconds,
        enabled = enabled,
        formatter = { context, value -> context.resources.getQuantityString(R.plurals.second_pattern, value, value) },
        onValueChanged = { onTimeChanged(values.hours.times(3600) + values.minutes.times(60) + it) }
      )
    }
    if (!enabled) {
      Box(modifier = Modifier.fillMaxSize().clickable(onClick = {}))
    }
  }
}

@SuplaPreview
@Composable
private fun TimePickerPreview() {
  SuplaTheme {
    var enabledTime by remember { mutableIntStateOf(7 * 3600 + 30 * 60) }
    var disabledTime by remember { mutableIntStateOf(12 * 3600) }

    Column(
      modifier = Modifier.padding(Distance.default),
      verticalArrangement = Arrangement.spacedBy(Distance.default)
    ) {
      TimePicker(
        timeInSeconds = enabledTime,
        onTimeChanged = { enabledTime = it },
        modifier = Modifier.fillMaxWidth()
      )
      TimePicker(
        timeInSeconds = disabledTime,
        onTimeChanged = { disabledTime = it },
        enabled = false
      )
    }
  }
}
