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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalConfiguration
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
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.extensions.ifFalse
import org.supla.android.ui.extensions.isPhoneLandscape
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.TimerProgressView
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.channelstate.ChannelCurrentData
import org.supla.android.ui.views.channelstate.ChannelProgramData
import org.supla.android.ui.views.channelstate.ChannelState
import org.supla.android.ui.views.forms.NumberPicker
import org.supla.android.ui.views.forms.NumberPickerHighlight
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
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
  if (LocalConfiguration.current.isPhoneLandscape) {
    LandscapeView(state)
  } else {
    PortraitView(state)
  }
}

@Composable
private fun TimerDetailViewScope.PortraitView(state: TimersDetailViewState) {
  val timerData = state.timerData
  if (timerData != null) {
    PortraitRunning(state, timerData)
  } else {
    PortraitDefault(state)
  }
}

@Composable
private fun TimerDetailViewScope.PortraitDefault(state: TimersDetailViewState) {
  Column(
    modifier = Modifier.fillMaxHeight(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    ChannelState(state.deviceStateData, state.programInfo)

    Column(
      modifier = Modifier
        .background(MaterialTheme.colorScheme.surface)
        .fillMaxWidth()
        .padding(all = Distance.default),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      TimerHeader(state)
      SwitchSelector(state)
      TimePicker(
        timeInSeconds = state.timerTimeInSeconds,
        onTimeChanged = { updateTimerTime(it) },
        modifier = Modifier
          .fillMaxWidth()
      )
      state.forceActive.ifFalse { DurationInfoTime(state) }
      StartButton(state)
    }
  }
}

@Composable
private fun TimerDetailViewScope.PortraitRunning(state: TimersDetailViewState, timerData: TimerProgressData) {
  var leftTimeInSecs by remember(timerData.endTime) { mutableIntStateOf(0) }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    ChannelState(
      deviceStateData = state.deviceStateData,
      programInfo = state.programInfo,
      modifier = Modifier.align(Alignment.TopCenter)
    )

    TimerInProgress(
      timerData = timerData,
      leftTimeInSecs = leftTimeInSecs,
      onLeftTimeChanged = { leftTimeInSecs = it },
      modifier = Modifier.align(Alignment.Center)
    )

    EditButton(
      leftTimeInSecs = leftTimeInSecs,
      modifier = Modifier
        .align(Alignment.Center)
        .padding(top = 292.dp)
    )

    TimerActionButtons(
      timerValue = timerData.timerValue,
      modifier = Modifier.align(Alignment.BottomCenter)
    )

    if (state.editMode) {
      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .background(MaterialTheme.colorScheme.surface)
          .fillMaxWidth()
          .padding(all = Distance.default),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Distance.small)
      ) {
        TimerHeader(state)
        TimePicker(
          timeInSeconds = state.timerTimeInSeconds,
          onTimeChanged = { updateTimerTime(it) },
          modifier = Modifier.fillMaxWidth()
        )
        CancelEditButton()
        StartButton(state)
      }
    }
  }
}

@Composable
private fun TimerDetailViewScope.LandscapeView(state: TimersDetailViewState) {
  val timerData = state.timerData
  if (timerData != null) {
    LandscapeRunning(state, timerData)
  } else {
    LandscapeDefault(state)
  }
}

@Composable
private fun TimerDetailViewScope.LandscapeDefault(state: TimersDetailViewState) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    modifier = Modifier
      .fillMaxHeight()
      .background(MaterialTheme.colorScheme.surface)
      .padding(start = Distance.default, end = Distance.default, bottom = Distance.default)
  ) {
    Column(modifier = Modifier.weight(1f)) {
      ChannelState(state.deviceStateData, state.programInfo, padding = PaddingValues(top = Distance.default))
      Spacer(modifier = Modifier.weight(1f))
      state.forceActive.ifFalse { DurationInfoTime(state) }
      StartButton(state, padding = PaddingValues.Zero)
    }
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
      TimerHeader(state, PaddingValues(top = Distance.default))
      SwitchSelector(state)
      TimePicker(
        timeInSeconds = state.timerTimeInSeconds,
        onTimeChanged = { updateTimerTime(it) },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

@Composable
private fun TimerDetailViewScope.LandscapeRunning(state: TimersDetailViewState, timerData: TimerProgressData) {
  var leftTimeInSecs by remember(timerData.endTime) { mutableIntStateOf(0) }

  Row(
    horizontalArrangement = Arrangement.spacedBy(Distance.default),
    modifier = Modifier
      .fillMaxHeight()
      .background(MaterialTheme.colorScheme.surface)
      .padding(start = Distance.default, end = Distance.default, bottom = Distance.default)
  ) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
      ChannelState(state.deviceStateData, state.programInfo, padding = PaddingValues(top = Distance.default))
      Spacer(modifier = Modifier.weight(1f))
      TimerInProgress(
        timerData = timerData,
        leftTimeInSecs = leftTimeInSecs,
        onLeftTimeChanged = { leftTimeInSecs = it },
        modifier = Modifier.heightIn(max = 200.dp)
      )
      Spacer(modifier = Modifier.weight(1f))
      if (state.editMode) {
        CancelEditButton()
      }
    }
    Column(
      modifier = Modifier.weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(Distance.small)
    ) {
      if (state.editMode) {
        TimerHeader(state, PaddingValues(top = Distance.default))
        TimePicker(
          timeInSeconds = state.timerTimeInSeconds,
          onTimeChanged = { updateTimerTime(it) },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.weight(1f))
        StartButton(state)
      } else {
        Spacer(modifier = Modifier.weight(1f))
        EditButton(leftTimeInSecs)
        TimerLeaveButton(timerData.timerValue)
        TimerCancelButton(timerData.timerValue)
      }
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
    TimerLeaveButton(timerValue)
    TimerCancelButton(timerValue)
  }
}

@Composable
private fun TimerDetailViewScope.TimerLeaveButton(timerValue: TimerValue) =
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

@Composable
private fun TimerDetailViewScope.TimerCancelButton(timerValue: TimerValue) =
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

@Composable
private fun TimerDetailViewScope.StartButton(
  state: TimersDetailViewState,
  padding: PaddingValues = PaddingValues.Zero
) {
  Button(
    text = stringResource(id = if (state.editMode) R.string.save else R.string.details_timer_start),
    modifier = Modifier
      .fillMaxWidth()
      .padding(padding),
    enabled = state.online && !state.forceActive,
    onClick = { onStartTimer() }
  )
}

@Composable
private fun TimerDetailViewScope.CancelEditButton() =
  OutlinedButton(
    text = stringResource(id = R.string.details_timer_edit_cancel),
    modifier = Modifier.fillMaxWidth(),
    onClick = { cancelEditMode() }
  )

@Composable
private fun TimerHeader(
  state: TimersDetailViewState,
  padding: PaddingValues = PaddingValues(horizontal = Distance.default)
) =
  Text(
    text = configurationHeader(state),
    style = MaterialTheme.typography.titleSmall,
    textAlign = TextAlign.Center,
    modifier = Modifier.padding(padding)
  )

@Composable
private fun TimerDetailViewScope.SwitchSelector(
  state: TimersDetailViewState,
  padding: PaddingValues = PaddingValues(vertical = Distance.default)
) =
  SegmentedComponent(
    items = TimerTargetAction.entries,
    activeItem = state.targetAction,
    enabled = state.online && !state.forceActive,
    onClick = { updateAction(it) },
    modifier = Modifier
      .fillMaxWidth()
      .padding(padding)
  )

@Composable
private fun DurationInfoTime(
  state: TimersDetailViewState,
  padding: PaddingValues = PaddingValues(horizontal = Distance.default, vertical = Distance.small)
) =
  Text(
    text = timerInfoText(state),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
    modifier = Modifier.padding(padding)
  )

@Composable
private fun TimerDetailViewScope.EditButton(leftTimeInSecs: Int, modifier: Modifier = Modifier) =
  TextButton(
    onClick = { startEditMode(leftTimeInSecs) },
    modifier = modifier
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

@Composable
private fun configurationHeader(state: TimersDetailViewState): String =
  when {
    state.forceActive -> stringResource(id = R.string.details_timer_disabled_forced_mode)
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
@SuplaPreviewLandscape
@Composable
private fun PreviewConfiguration() {
  SuplaTheme {
    previewScope.TimerDetailView(
      TimersDetailViewState(
        targetAction = TimerTargetAction.TURN_ON,
        timerTimeInSeconds = 3600,
        deviceStateData = ChannelCurrentData(
          label = localizedString(R.string.details_timer_state_label),
          icon = ImageId(R.drawable.fnc_switch_on),
          value = LocalizedString.Empty
        ),
      )
    )
  }
}

@SuplaPreview
@SuplaPreviewLandscape
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

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun PreviewTimerInEditMode() {
  SuplaTheme {
    previewScope.TimerDetailView(
      TimersDetailViewState(
        timerData = TimerProgressData(
          startTime = Date(),
          endTime = Date(Date().time + 3600_000),
          indeterminate = false,
          timerValue = TimerValue.ON
        ),
        editMode = true,
        programInfo = listOf(
          ChannelProgramData(
            type = ChannelProgramData.Type.CURRENT,
            time = localizedString(R.string.program_info_time, localizedString(R.string.time_just_minutes, 12)),
            description = localizedString(R.string.schedule_program_force_on)
          ),
          ChannelProgramData(
            type = ChannelProgramData.Type.NEXT,
            description = localizedString(R.string.schedule_program_force_off)
          )
        ),
      )
    )
  }
}
