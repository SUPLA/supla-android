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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.gray
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.features.details.thermostatdetail.timer.DeviceMode
import org.supla.android.features.details.thermostatdetail.timer.SetpointTemperature
import org.supla.android.features.details.thermostatdetail.timer.TimerDetailViewState
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.BoxSize
import org.supla.android.ui.views.LoadingScrim
import org.supla.android.ui.views.SegmentedComponent
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.OutlinedButton
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.forms.DatePicker
import org.supla.android.ui.views.forms.NumberPicker
import org.supla.android.ui.views.forms.NumberPickerHighlight
import org.supla.android.ui.views.forms.TextField
import org.supla.android.ui.views.slider.ThermostatThumb
import org.supla.android.ui.views.thermostat.TemperatureControlButton
import org.supla.core.shared.usecase.channel.valueformatter.DefaultValueFormatter
import java.util.Date

interface ThermostatTimerConfigurationScope {
  fun toggleDeviceMode(deviceMode: DeviceMode)
  fun onTemperatureChange(step: TemperatureCorrection)
  fun onTemperatureChange(temperature: Float)
  fun onTemperatureChange(range: ClosedFloatingPointRange<Float>)
  fun toggleSelectorMode()
  fun onDateChanged(selectedDateMillis: Long?)
  fun onTimerDaysChange(days: Int)
  fun onTimerHoursChange(hours: Int)
  fun onTimerMinutesChange(minutes: Int)
  fun onTimeClicked()
  fun editTimerCancel()
  fun onStartTimer()
}

@Composable
fun ThermostatTimerConfigurationScope.ConfigurationView(state: TimerDetailViewState) {
  if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    LandscapeView(state)
  } else {
    PortraitView(state)
  }
}

@Composable
private fun ThermostatTimerConfigurationScope.PortraitView(state: TimerDetailViewState) {
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
      ModeSelector(state, Distance.default)

      if (state.selectedMode != DeviceMode.OFF) {
        TemperatureSelector(state)
      }

      TimeSelectionHeader(state, Modifier.padding(top = Distance.default)) { toggleSelectorMode() }
      TimerSelector(state, modifier = Modifier.padding(top = Distance.small))
      InfoText(state)
    }

    ButtonsPortrait(state, Modifier.align(Alignment.BottomCenter))
    state.loadingState.loading.ifTrue { LoadingScrim() }
  }
}

@Composable
private fun ThermostatTimerConfigurationScope.LandscapeView(state: TimerDetailViewState) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(MaterialTheme.colorScheme.surface)
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(Distance.default)) {
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(start = Distance.default)
      ) {
        ModeSelector(state, Distance.small)
        if (state.selectedMode != DeviceMode.OFF) {
          TemperatureSelector(state, topPadding = Distance.tiny)
        }

        Spacer(modifier = Modifier.weight(1f))

        ButtonsLandscape(state)
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(end = Distance.default)
          .verticalScroll(rememberScrollState())
      ) {
        TimeSelectionHeader(state) { toggleSelectorMode() }
        TimerSelector(state)
        InfoText(state, topPadding = if (state.showCalendar) Distance.tiny else 0.dp)
      }
    }
    state.loadingState.loading.ifTrue { LoadingScrim() }
  }
}

@Composable
private fun ThermostatTimerConfigurationScope.ModeSelector(state: TimerDetailViewState, topPadding: Dp) {
  state.temperature?.availableModes?.let { modes ->
    HeaderText(
      text = stringResource(id = R.string.details_timer_select_mode),
      modifier = Modifier.padding(top = topPadding)
    )
    SegmentedComponent(
      items = modes,
      activeItem = state.selectedMode,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = Distance.tiny),
      onClick = { toggleDeviceMode(it) },
      boxSize = if (modes.size > 2) BoxSize.Custom else BoxSize.Identical
    )
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
private fun ThermostatTimerConfigurationScope.TemperatureSelector(state: TimerDetailViewState, topPadding: Dp = Distance.default) {
  Row(
    modifier = Modifier.padding(top = topPadding),
    verticalAlignment = Alignment.CenterVertically
  ) {
    CaptionText(text = stringResource(id = R.string.details_timer_min_temp))
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = state.currentTemperatureString ?: "",
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
      color = colorResource(id = state.temperature?.thumbColorRes ?: R.color.primary),
      size = dimensionResource(id = R.dimen.button_default_size),
      onClick = { onTemperatureChange(TemperatureCorrection.DOWN) }
    )
    TemperatureSlider(state, Modifier.weight(1f))
    TemperatureControlButton(
      icon = R.drawable.ic_plus,
      color = colorResource(id = state.temperature?.thumbColorRes ?: R.color.primary),
      size = dimensionResource(id = R.dimen.button_default_size),
      onClick = { onTemperatureChange(TemperatureCorrection.UP) }
    )
  }
}

@Composable
private fun ThermostatTimerConfigurationScope.TemperatureSlider(state: TimerDetailViewState, modifier: Modifier = Modifier) {
  when (val temperature = state.temperature) {
    is SetpointTemperature.Heat -> TemperatureSlider(temperature.value, temperature, state, modifier)
    is SetpointTemperature.Cool -> TemperatureSlider(temperature.value, temperature, state, modifier)
    is SetpointTemperature.HeatAndCool -> {
      when (state.selectedMode) {
        DeviceMode.AUTO -> RangeSlider(temperature, state, modifier)
        DeviceMode.HEATING -> TemperatureSlider(temperature.setpointHeat, temperature, state, modifier)
        DeviceMode.COOLING -> TemperatureSlider(temperature.setpointCool, temperature, state, modifier)
        else -> {}
      }
    }
    else -> {}
  }
}

@Composable
private fun ThermostatTimerConfigurationScope.TemperatureSlider(
  value: Float,
  temperature: SetpointTemperature,
  state: TimerDetailViewState,
  modifier: Modifier = Modifier
) {
  val lightGrayColor = colorResource(id = R.color.gray_light)

  val colors = SliderDefaults.colors(
    activeTrackColor = lightGrayColor,
    activeTickColor = lightGrayColor,
    disabledActiveTrackColor = lightGrayColor,
    disabledInactiveTrackColor = lightGrayColor,
    inactiveTrackColor = lightGrayColor,
    disabledActiveTickColor = lightGrayColor,
    disabledInactiveTickColor = lightGrayColor,
    inactiveTickColor = lightGrayColor
  )

  val interactionSource = remember { MutableInteractionSource() }
  Slider(
    value = value,
    valueRange = state.temperaturesRange,
    steps = state.temperatureSteps,
    onValueChange = { onTemperatureChange(it) },
    interactionSource = interactionSource,
    thumb = { ThermostatThumb(interactionSource, temperature) },
    modifier = modifier,
    colors = colors
  )
}

@Composable
private fun ThermostatTimerConfigurationScope.RangeSlider(
  temperature: SetpointTemperature.HeatAndCool,
  state: TimerDetailViewState,
  modifier: Modifier = Modifier
) {
  val lightGrayColor = colorResource(id = R.color.gray_light)
  val colors = SliderDefaults.colors(
    activeTrackColor = MaterialTheme.colorScheme.primary,
    activeTickColor = MaterialTheme.colorScheme.primary,
    disabledActiveTrackColor = lightGrayColor,
    disabledInactiveTrackColor = lightGrayColor,
    inactiveTrackColor = lightGrayColor,
    disabledActiveTickColor = lightGrayColor,
    disabledInactiveTickColor = lightGrayColor,
    inactiveTickColor = lightGrayColor
  )

  val startInteractionSource = remember { MutableInteractionSource() }
  val endInteractionSource = remember { MutableInteractionSource() }
  RangeSlider(
    value = temperature.heat..temperature.cool,
    onValueChange = { onTemperatureChange(it) },
    valueRange = state.temperaturesRange,
    steps = state.temperatureSteps,
    startInteractionSource = startInteractionSource,
    startThumb = { ThermostatThumb(startInteractionSource, temperature) },
    endInteractionSource = endInteractionSource,
    endThumb = {
      ThermostatThumb(
        interactionSource = startInteractionSource,
        iconRes = temperature.secondThumbIconRes,
        color = colorResource(temperature.secondThumbColorRes)
      )
    },
    modifier = modifier,
    colors = colors
  )
}

@Composable
private fun TimeSelectionHeader(state: TimerDetailViewState, modifier: Modifier = Modifier, onModeChanged: () -> Unit) =
  Row(
    modifier = modifier,
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
private fun ThermostatTimerConfigurationScope.TimerSelector(state: TimerDetailViewState, modifier: Modifier = Modifier) {
  if (state.showCalendar) {
    TimerSelectorCalendar(state)
  } else {
    TimerSelectorCounter(state, modifier)
  }
}

@Composable
private fun InfoText(state: TimerDetailViewState, topPadding: Dp = Distance.small) =
  Text(
    text = state.timerInfoText(LocalContext.current),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.gray,
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = Distance.default, top = topPadding, end = Distance.default, bottom = Distance.default)
  )

@Composable
fun ThermostatTimerConfigurationScope.TimerSelectorCalendar(state: TimerDetailViewState) {
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
        onDateChanged(pickerState.selectedDateMillis)
      }
    }
  )

  CaptionText(
    text = stringResource(id = R.string.calendar_picker_end_hour),
    modifier = Modifier.padding(start = 12.dp)
  )

  TextField(
    value = state.calendarTimeValue?.let { ValuesFormatter.getHourString(it) } ?: "12:00",
    trailingIcon = {
      Icon(painter = painterResource(id = R.drawable.ic_timer), contentDescription = null)
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp),
    readOnly = true,
    onClicked = { onTimeClicked() }
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
fun ThermostatTimerConfigurationScope.TimerSelectorCounter(state: TimerDetailViewState, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    NumberPickerHighlight()
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.default),
      modifier = Modifier.clip(RectangleShape)
    ) {
      Spacer(modifier = Modifier.weight(1f))

      NumberPicker(
        range = IntRange(0, 365),
        selectedValue = state.timerDays,
        formatter = { context, i ->
          context.resources.getQuantityString(R.plurals.day_pattern, i, i)
        },
        onValueChanged = { onTimerDaysChange(it) }
      )
      NumberPicker(
        range = IntRange(0, 23),
        selectedValue = state.timerHours,
        formatter = { context, i ->
          context.resources.getQuantityString(R.plurals.hour_pattern, i, i)
        },
        onValueChanged = { onTimerHoursChange(it) }
      )
      NumberPicker(
        range = IntRange(0, 59),
        selectedValue = state.timerMinutes,
        formatter = { context, i ->
          context.resources.getQuantityString(R.plurals.minute_pattern, i, i)
        },
        onValueChanged = { onTimerMinutesChange(it) }
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun ThermostatTimerConfigurationScope.ButtonsPortrait(
  state: TimerDetailViewState,
  modifier: Modifier = Modifier
) =
  Column(
    modifier = modifier
      .padding(Distance.default),
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    if (state.isTimerOn) {
      OutlinedButton(
        text = stringResource(id = R.string.cancel),
        modifier = Modifier.fillMaxWidth()
      ) { editTimerCancel() }
    }
    Button(
      text = stringResource(id = if (state.isTimerOn) R.string.save else R.string.details_timer_start),
      onClick = { onStartTimer() },
      modifier = Modifier.fillMaxWidth(),
      enabled = state.isTimerOn || state.startEnabled
    )
  }

@Composable
private fun ThermostatTimerConfigurationScope.ButtonsLandscape(state: TimerDetailViewState) =
  Row(
    modifier = Modifier.padding(vertical = Distance.small),
    horizontalArrangement = Arrangement.spacedBy(Distance.default)
  ) {
    if (state.isTimerOn) {
      OutlinedButton(
        text = stringResource(id = R.string.cancel),
        modifier = Modifier.weight(1f)
      ) { editTimerCancel() }
    }
    Button(
      text = stringResource(id = if (state.isTimerOn) R.string.save else R.string.details_timer_start),
      onClick = { onStartTimer() },
      modifier = Modifier.weight(1f),
      enabled = state.isTimerOn || state.startEnabled
    )
  }

private val previewScope = object : ThermostatTimerConfigurationScope {
  override fun toggleDeviceMode(deviceMode: DeviceMode) {}
  override fun onTemperatureChange(step: TemperatureCorrection) {}
  override fun onTemperatureChange(temperature: Float) {}
  override fun onTemperatureChange(range: ClosedFloatingPointRange<Float>) {}
  override fun toggleSelectorMode() {}
  override fun onDateChanged(selectedDateMillis: Long?) {}
  override fun onTimerDaysChange(days: Int) {}
  override fun onTimerHoursChange(hours: Int) {}
  override fun onTimerMinutesChange(minutes: Int) {}
  override fun onTimeClicked() {}
  override fun editTimerCancel() {}
  override fun onStartTimer() {}
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview() {
  SuplaTheme {
    previewScope.ConfigurationView(
      state = TimerDetailViewState(
        thermometerValueFormatter = DefaultValueFormatter,
        temperature = SetpointTemperature.Heat(22f),
        loadingState = LoadingTimeoutManager.LoadingState(loading = false)
      ),
    )
  }
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview_Manual() {
  SuplaTheme {
    previewScope.ConfigurationView(
      state = TimerDetailViewState(
        thermometerValueFormatter = DefaultValueFormatter,
        selectedMode = DeviceMode.MANUAL,
        temperature = SetpointTemperature.Cool(21f),
        minTemperature = 10f,
        maxTemperature = 40f,
        loadingState = LoadingTimeoutManager.LoadingState(loading = false),
        isTimerOn = true
      )
    )
  }
}

@SuplaPreview
@SuplaPreviewLandscape
@Composable
private fun Preview_Auto() {
  SuplaTheme {
    previewScope.ConfigurationView(
      state = TimerDetailViewState(
        thermometerValueFormatter = DefaultValueFormatter,
        selectedMode = DeviceMode.MANUAL,
        temperature = SetpointTemperature.HeatAndCool(18f, 35f, false),
        minTemperature = 10f,
        maxTemperature = 40f,
        loadingState = LoadingTimeoutManager.LoadingState(loading = false)
      )
    )
  }
}
