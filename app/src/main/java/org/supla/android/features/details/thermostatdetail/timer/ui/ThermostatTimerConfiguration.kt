@file:OptIn(ExperimentalMaterial3Api::class)

package org.supla.android.features.details.thermostatdetail.timer.ui
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.gray
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.extensions.valuesFormatter
import org.supla.android.features.details.thermostatdetail.timer.DeviceMode
import org.supla.android.features.details.thermostatdetail.timer.TimerDetailViewState
import org.supla.android.ui.views.DatePicker
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.NumberPicker
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.slider.ThermostatThumb
import org.supla.android.ui.views.thermostat.TemperatureControlButton
import java.util.Date

@Composable
fun ThermostatTimerConfiguration(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(MaterialTheme.colorScheme.surface)
  ) {
    val bottomPadding = if (state.editTime) 144.dp else 80.dp
    Column(
      modifier = Modifier
        .padding(start = Distance.default, end = Distance.default, bottom = bottomPadding)
        .verticalScroll(rememberScrollState())
    ) {
      HeaderText(text = stringResource(id = R.string.details_timer_select_mode), modifier = Modifier.padding(top = Distance.default))
      SegmentedComponent(
        items = DeviceMode.entries.map { stringResource(id = it.stringRes) },
        activeItem = state.selectedMode.position,
        modifier = Modifier.padding(top = Distance.tiny),
        onClick = { viewProxy.toggleDeviceMode(DeviceMode.from(it)) }
      )

      if (state.selectedMode != DeviceMode.OFF) {
        TemperatureSelector(state, viewProxy)
      }

      TimeSelectionHeader(state) { viewProxy.toggleSelectorMode() }

      TimerSelector(state, viewProxy)

      InfoText(state)
    }

    if (state.isTimerOn) {
      BottomSummaryEdit(viewProxy = viewProxy, modifier = Modifier.align(Alignment.BottomCenter))
    } else {
      BottomSummaryNotRunning(
        state = state,
        viewProxy = viewProxy,
        modifier = Modifier.align(Alignment.BottomCenter)
      )
    }

    if (state.loadingState.loading) {
      LoadingScrim()
    }
  }
}

@Composable
private fun HeaderText(text: String, modifier: Modifier = Modifier) =
  Text(
    text = text.uppercase(),
    color = MaterialTheme.colorScheme.gray,
    style = MaterialTheme.typography.bodyMedium,
    modifier = modifier
  )

@Composable
private fun TemperatureSelector(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  Row(
    modifier = Modifier.padding(top = Distance.default),
    verticalAlignment = Alignment.CenterVertically
  ) {
    CaptionText(text = stringResource(id = R.string.details_timer_min_temp))
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = LocalContext.current.valuesFormatter.getTemperatureString(state.currentTemperature),
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.weight(1f))
    CaptionText(text = stringResource(id = R.string.details_timer_max_temp))
  }

  Row(
    modifier = Modifier.padding(top = Distance.tiny),
    horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
  ) {
    TemperatureControlButton(
      icon = R.drawable.ic_minus,
      color = colorResource(id = state.thumbColor),
      size = dimensionResource(id = R.dimen.button_default_size),
      onClick = { viewProxy.onTemperatureChange(TemperatureCorrection.DOWN) }
    )
    TemperatureSlider(state, viewProxy)
    TemperatureControlButton(
      icon = R.drawable.ic_plus,
      color = colorResource(id = state.thumbColor),
      size = dimensionResource(id = R.dimen.button_default_size),
      onClick = { viewProxy.onTemperatureChange(TemperatureCorrection.UP) }
    )
  }
}

context(RowScope)
@Composable
private fun TemperatureSlider(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  val lightGrayColor = colorResource(id = R.color.gray_light)
  val colors = SliderDefaults.colors(
    activeTrackColor = lightGrayColor,
    disabledActiveTrackColor = lightGrayColor,
    disabledInactiveTrackColor = lightGrayColor,
    inactiveTrackColor = lightGrayColor,
    activeTickColor = lightGrayColor,
    disabledActiveTickColor = lightGrayColor,
    disabledInactiveTickColor = lightGrayColor,
    inactiveTickColor = lightGrayColor
  )
  val interactionSource = remember { MutableInteractionSource() }
  Slider(
    value = state.currentTemperature ?: 0f,
    valueRange = state.temperaturesRange,
    steps = state.temperatureSteps,
    onValueChange = { viewProxy.onTemperatureChange(it) },
    interactionSource = interactionSource,
    thumb = {
      ThermostatThumb(
        interactionSource = interactionSource,
        iconRes = state.thumbIcon,
        color = colorResource(id = state.thumbColor)
      )
    },
    modifier = Modifier.weight(1f),
    colors = colors
  )
}

@Composable
private fun TimeSelectionHeader(state: TimerDetailViewState, onModeChanged: () -> Unit) =
  Row(
    modifier = Modifier.padding(top = Distance.default),
    verticalAlignment = Alignment.CenterVertically
  ) {
    HeaderText(text = stringResource(id = R.string.details_timer_select_time))
    Spacer(modifier = Modifier.weight(1f))
    EditModeButton(state, onClick = onModeChanged)
  }

@Composable
private fun EditModeButton(state: TimerDetailViewState, onClick: () -> Unit) =
  TextButton(onClick = onClick) {
    Icon(
      painter = if (state.showCalendar) painterResource(id = R.drawable.ic_timer) else painterResource(id = R.drawable.ic_schedule),
      contentDescription = null,
      modifier = Modifier.padding(end = Distance.tiny),
      tint = colorResource(id = R.color.primary)
    )
    Text(
      text = if (state.showCalendar) stringResource(R.string.details_timer_counter) else stringResource(R.string.details_timer_calendar),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onBackground
    )
  }

@Composable
private fun TimerSelector(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  if (state.showCalendar) {
    TimerSelectorCalendar(state, viewProxy)
  } else {
    TimerSelectorCounter(state, viewProxy)
  }
}

@Composable
private fun InfoText(state: TimerDetailViewState) =
  Text(
    text = state.timerInfoText(LocalContext.current),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.gray,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, top = Distance.small, end = Distance.default, bottom = Distance.default)
  )

@Composable
fun TimerSelectorCalendar(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  @Suppress("DEPRECATION")
  val pickerState = rememberDatePickerState(
    yearRange = state.yearsRange,
    initialSelectedDateMillis = state.calendarValue?.time?.minus(state.calendarValue.timezoneOffset.times(60000)),
    selectableDates = object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return state.dateValidator(Date(utcTimeMillis))
      }
    }
  )

  DatePicker(
    state = pickerState,
    headline = null,
    showModeToggle = false,
    // As we do not have any on click events, it's workaround to get it
    modifier = Modifier.pointerInput(Unit) {
      awaitEachGesture {
        do {
          val event: PointerEvent = awaitPointerEvent()
        } while (event.changes.any { it.pressed })
        viewProxy.onDateChanged(pickerState.selectedDateMillis)
      }
    }
  )

  CaptionText(
    text = stringResource(id = R.string.calendar_picker_end_hour),
    modifier = Modifier.padding(start = 12.dp)
  )

  TextField(
    value = state.calendarTimeValue?.let { LocalContext.current.valuesFormatter.getHourString(it) } ?: "12:00",
    trailingIcon = {
      Icon(painter = painterResource(id = R.drawable.ic_timer), contentDescription = null)
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    readOnly = true,
    onClicked = { viewProxy.onTimeClicked() }
  )
}

@Composable
private fun CaptionText(text: String, modifier: Modifier = Modifier) =
  Text(
    text = text.uppercase(),
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.gray,
    modifier = modifier
  )

@Composable
fun TimerSelectorCounter(viewState: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  Box(
    modifier = Modifier.padding(top = Distance.small)
  ) {
    Box(
      modifier = Modifier
        .height(40.dp)
        .fillMaxWidth()
        .background(
          color = colorResource(id = R.color.gray_lighter),
          shape = RoundedCornerShape(dimensionResource(id = R.dimen.radius_default))
        )
        .align(Alignment.Center)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(Distance.default)) {
      Spacer(modifier = Modifier.weight(1f))

      NumberPicker(
        range = IntRange(0, 365),
        selectedValue = viewState.timerDays,
        formatter = { context, i ->
          context.resources.getQuantityString(R.plurals.day_pattern, i, i)
        },
        onValueChanged = { viewProxy.onTimerDaysChange(it) }
      )
      NumberPicker(
        range = IntRange(0, 23),
        selectedValue = viewState.timerHours,
        formatter = { context, i ->
          context.resources.getQuantityString(R.plurals.hour_pattern, i, i)
        },
        onValueChanged = { viewProxy.onTimerHoursChange(it) }
      )
      NumberPicker(
        range = IntRange(0, 59),
        selectedValue = viewState.timerMinutes,
        formatter = { context, i ->
          context.resources.getQuantityString(R.plurals.minute_pattern, i, i)
        },
        onValueChanged = { viewProxy.onTimerMinutesChange(it) }
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun BottomSummaryNotRunning(
  state: TimerDetailViewState,
  viewProxy: TimerDetailViewProxy,
  modifier: Modifier = Modifier
) =
  Column(
    modifier = modifier
  ) {
    Separator()
    Button(
      text = stringResource(id = R.string.details_timer_start),
      onClick = { viewProxy.onStartTimer() },
      enabled = state.startEnabled,
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = Distance.default, top = Distance.small, end = Distance.default, bottom = Distance.small)
    )
  }

@Composable
private fun BottomSummaryEdit(
  viewProxy: TimerDetailViewProxy,
  modifier: Modifier = Modifier
) =
  Column(
    modifier = modifier
  ) {
    Separator()
    OutlinedButton(
      text = stringResource(id = R.string.cancel),
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = Distance.default, top = Distance.small, end = Distance.default)
    ) { viewProxy.editTimerCancel() }
    Button(
      text = stringResource(id = R.string.save),
      onClick = { viewProxy.onStartTimer() },
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = Distance.default, top = Distance.small, end = Distance.default, bottom = Distance.small)
    )
  }

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    ThermostatTimerConfiguration(
      state = TimerDetailViewState(),
      viewProxy = PreviewProxy2(TimerDetailViewState())
    )
  }
}

@Preview
@Composable
private fun Preview_Manual() {
  SuplaTheme {
    ThermostatTimerConfiguration(
      state = TimerDetailViewState(
        selectedMode = DeviceMode.MANUAL
      ),
      viewProxy = PreviewProxy2(TimerDetailViewState())
    )
  }
}

private class PreviewProxy2(val state: TimerDetailViewState) : TimerDetailViewProxy {
  override val timerLeftTime: Int = 0
  override fun getViewState(): StateFlow<TimerDetailViewState> =
    MutableStateFlow(state)
}
