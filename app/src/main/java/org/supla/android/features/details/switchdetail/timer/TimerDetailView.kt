package org.supla.android.features.details.switchdetail.timer
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

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.tools.SuplaPreview
import org.supla.android.ui.views.DeviceState
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.TimerProgressView
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.forms.NumberPicker
import org.supla.android.ui.views.forms.NumberPickerHighlight
import java.util.Date
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

interface TimerDetailViewScope {
  fun onStartTimer()
  fun stopTimer()
  fun cancelTimer()
  fun startEditMode(leftTimeInSecs: Int)
  fun cancelEditMode()
  fun updateAction(action: TimerTargetAction)
  fun updateTimerTime(timeInSeconds: Int)
  fun onTimerFinished()
}

@Composable
fun TimerDetailViewScope.TimerDetailView(state: TimersDetailViewState) {
  val timerData = state.timerData
  var leftTimeInSecs by remember(timerData?.endTime) { mutableIntStateOf(0) }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    state.deviceStateData?.let {
      DeviceState(
        data = it,
        modifier = Modifier
          .padding(vertical = Distance.vertical)
          .align(Alignment.TopCenter)
      )
    }

    if (timerData != null) {
      TimerInProgress(
        timerData = timerData,
        leftTimeInSecs = leftTimeInSecs,
        onLeftTimeChanged = { leftTimeInSecs = it },
        modifier = Modifier.align(Alignment.Center)
      )

      TextButton(
        onClick = { startEditMode(leftTimeInSecs) },
        modifier = Modifier
          .align(Alignment.Center)
          .padding(top = 292.dp)
      ) {
        Text(
          text = stringResource(id = R.string.details_timer_edit_time),
          color = MaterialTheme.colorScheme.onBackground,
          style = MaterialTheme.typography.bodyMedium
        )
        Icon(
          painter = painterResource(id = R.drawable.pencil),
          contentDescription = null,
          modifier = Modifier
            .padding(start = Distance.tiny)
            .size(24.dp)
        )
      }

      TimerActionButtons(
        timerValue = timerData.timerValue,
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }

    if (timerData == null || state.editMode) {
      TimerConfiguration(
        state = state,
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }
  }
}

@Composable
private fun TimerDetailViewScope.TimerInProgress(
  timerData: TimerProgressData,
  leftTimeInSecs: Int,
  onLeftTimeChanged: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  var progress by remember(timerData.startTime, timerData.endTime) { mutableIntStateOf(0) }

  LaunchedEffect(timerData.startTime, timerData.endTime) {
    while (true) {
      val leftTime = timerData.endTime.time - Date().time
      onLeftTimeChanged(max(leftTime.div(1000).toInt(), 0))
      progress = progressOf(timerData.startTime, timerData.endTime, leftTime)
      if (leftTime <= 0) {
        onTimerFinished()
        break
      }
      delay(100.milliseconds)
    }
  }

  Box(modifier = modifier) {
    TimerProgressView(progress = progress.div(PROGRESS_MULTIPLIER.toFloat()), indeterminate = timerData.indeterminate)
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      val timeValues = TimeValues.of(leftTimeInSecs.plus(1).toLong())
      Text(
        text = stringResource(id = R.string.details_timer_format, timeValues.hours, timeValues.minutes, timeValues.seconds),
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
        textAlign = TextAlign.Center
      )
      Text(
        text = stringResource(
          id = R.string.details_timer_end_hour,
          DateFormat.format(stringResource(id = R.string.hour_string_format), timerData.endTime)
        ),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun TimerDetailViewScope.TimerActionButtons(timerValue: TimerValue, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(start = Distance.default, end = Distance.default, bottom = Distance.default),
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    OutlinedButton(
      text = stringResource(
        id = R.string.details_timer_leave_it,
        pluralStringResource(
          id = if (timerValue == TimerValue.ON) R.plurals.details_timer_info_on else R.plurals.details_timer_info_off,
          count = 1
        )
      ),
      modifier = Modifier.fillMaxWidth(),
      singleLine = false,
      onClick = { stopTimer() }
    )
    Button(
      text = stringResource(
        id = R.string.details_timer_cancel_and,
        stringResource(
          id = if (timerValue == TimerValue.ON) R.string.details_timer_cancel_turn_off else R.string.details_timer_cancel_turn_on
        )
      ),
      modifier = Modifier.fillMaxWidth(),
      singleLine = false,
      onClick = { cancelTimer() }
    )
  }
}

@Composable
private fun TimerDetailViewScope.TimerConfiguration(state: TimersDetailViewState, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier.padding(vertical = Distance.default),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = configurationHeader(state),
        style = MaterialTheme.typography.titleSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = Distance.default)
      )

      if (state.editMode.not()) {
        SegmentedComponent(
          items = TimerTargetAction.entries,
          activeItem = state.targetAction,
          enabled = state.online,
          onClick = { updateAction(it) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = Distance.default)
        )
      } else {
        Spacer(modifier = Modifier.height(Distance.default))
      }

      TimePicker(
        timeInSeconds = state.timerTimeInSeconds,
        onTimeChanged = { updateTimerTime(it) },
        modifier = Modifier.fillMaxWidth()
      )

      if (state.editMode.not()) {
        HorizontalDivider(
          modifier = Modifier.padding(top = Distance.default),
          color = MaterialTheme.colorScheme.background
        )
        Text(
          text = timerInfoText(state),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = Distance.default, vertical = Distance.small)
        )
      }

      if (state.editMode) {
        OutlinedButton(
          text = stringResource(id = R.string.details_timer_edit_cancel),
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = Distance.default, bottom = Distance.small, end = Distance.default),
          onClick = { cancelEditMode() }
        )
      }

      Button(
        text = stringResource(id = if (state.editMode) R.string.save else R.string.details_timer_start),
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = Distance.default, end = Distance.default),
        enabled = state.online,
        onClick = { onStartTimer() }
      )
    }
  }
}

@Composable
private fun configurationHeader(state: TimersDetailViewState): String =
  when {
    state.editMode.not() -> stringResource(id = R.string.details_timer_text)
    state.on -> stringResource(
      id = R.string.details_timer_edit_header,
      pluralStringResource(id = R.plurals.details_timer_info_on, count = 1)
    )
    else -> stringResource(
      id = R.string.details_timer_edit_header,
      pluralStringResource(id = R.plurals.details_timer_info_off, count = 1)
    )
  }

@Composable
private fun timerInfoText(state: TimersDetailViewState): String {
  val action = state.targetAction ?: TimerTargetAction.TURN_ON
  val time = TimeValues.of(state.timerTimeInSeconds.toLong())
  val timeString = stringResource(id = R.string.details_timer_format, time.hours, time.minutes, time.seconds)

  return when (action) {
    TimerTargetAction.TURN_OFF -> stringResource(
      id = R.string.details_timer_info,
      pluralStringResource(id = R.plurals.details_timer_info_off, count = 1),
      timeString,
      pluralStringResource(id = R.plurals.details_timer_info_on, count = 2)
    )
    TimerTargetAction.TURN_ON -> stringResource(
      id = R.string.details_timer_info,
      pluralStringResource(id = R.plurals.details_timer_info_on, count = 1),
      timeString,
      pluralStringResource(id = R.plurals.details_timer_info_off, count = 2)
    )
  }
}

@Composable
private fun TimePicker(timeInSeconds: Int, onTimeChanged: (Int) -> Unit, modifier: Modifier = Modifier) {
  val values = TimeValues.of(timeInSeconds.toLong())

  Box(modifier = modifier) {
    NumberPickerHighlight()
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.default),
      modifier = Modifier
        .align(Alignment.Center)
        .clip(MaterialTheme.shapes.extraSmall)
    ) {
      NumberPicker(
        range = IntRange(0, 23),
        selectedValue = values.hours,
        formatter = { context, i -> context.resources.getQuantityString(R.plurals.hour_pattern, i, i) },
        onValueChanged = { onTimeChanged(it.times(3600) + values.minutes.times(60) + values.seconds) }
      )
      NumberPicker(
        range = IntRange(0, 59),
        selectedValue = values.minutes,
        formatter = { context, i -> context.resources.getQuantityString(R.plurals.minute_pattern, i, i) },
        onValueChanged = { onTimeChanged(values.hours.times(3600) + it.times(60) + values.seconds) }
      )
      NumberPicker(
        range = IntRange(0, 59),
        selectedValue = values.seconds,
        formatter = { context, i -> context.resources.getQuantityString(R.plurals.second_pattern, i, i) },
        onValueChanged = { onTimeChanged(values.hours.times(3600) + values.minutes.times(60) + it) }
      )
    }
  }
}

private const val PROGRESS_MULTIPLIER = 1000

private fun progressOf(startTime: Date, endTime: Date, leftTime: Long): Int {
  val wholeTime = endTime.time - startTime.time
  if (wholeTime <= 0) {
    return PROGRESS_MULTIPLIER
  }

  return ((1 - leftTime.div(wholeTime.toFloat())) * PROGRESS_MULTIPLIER).toInt().coerceIn(0, PROGRESS_MULTIPLIER)
}

private val previewScope = object : TimerDetailViewScope {
  override fun onStartTimer() {}
  override fun stopTimer() {}
  override fun cancelTimer() {}
  override fun startEditMode(leftTimeInSecs: Int) {}
  override fun cancelEditMode() {}
  override fun updateAction(action: TimerTargetAction) {}
  override fun updateTimerTime(timeInSeconds: Int) {}
  override fun onTimerFinished() {}
}

@SuplaPreview
@Composable
private fun PreviewConfiguration() {
  SuplaTheme {
    previewScope.TimerDetailView(
      TimersDetailViewState(
        targetAction = TimerTargetAction.TURN_ON,
        timerTimeInSeconds = 3600
      )
    )
  }
}

@SuplaPreview
@Composable
private fun PreviewTimerInProgress() {
  SuplaTheme {
    previewScope.TimerDetailView(
      TimersDetailViewState(
        timerData = TimerProgressData(
          startTime = Date(),
          endTime = Date(Date().time + 3600_000),
          indeterminate = false,
          timerValue = TimerValue.ON
        )
      )
    )
  }
}
