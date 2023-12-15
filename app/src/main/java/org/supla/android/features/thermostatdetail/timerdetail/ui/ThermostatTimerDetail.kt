package org.supla.android.features.thermostatdetail.timerdetail.ui
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.supla.android.core.ui.BaseViewProxy
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.data.source.local.temperature.TemperatureCorrection
import org.supla.android.features.thermostatdetail.timerdetail.DeviceMode
import org.supla.android.features.thermostatdetail.timerdetail.TimerDetailViewState
import org.supla.android.ui.dialogs.TimePickerDialog

interface TimerDetailViewProxy : BaseViewProxy<TimerDetailViewState> {
  val timerLeftTime: Int?

  fun toggleSelectorMode() {}
  fun toggleDeviceMode(deviceMode: DeviceMode) {}
  fun onDateChanged(selectedDateMillis: Long?) {}
  fun onTimeChanged(hour: Hour) {}
  fun onTimePickerDismiss() {}
  fun onTimeClicked() {}
  fun onTimerDaysChange(days: Int) {}
  fun onTimerHoursChange(hours: Int) {}
  fun onTimerMinutesChange(minutes: Int) {}
  fun onTemperatureChange(temperature: Float) {}
  fun onTemperatureChange(step: TemperatureCorrection) {}
  fun onStartTimer() {}
  fun formatLeftTime(leftTime: Int?): StringProvider = { "" }
  fun cancelTimerStartManual() {}
  fun cancelTimerStartProgram() {}
  fun editTimer() {}
  fun editTimerCancel() {}
}

@Composable
fun ThermostatTimerDetail(viewProxy: TimerDetailViewProxy) {
  val viewState by viewProxy.getViewState().collectAsState()

  if (viewState.showTimePicker) {
    TimePickerDialog(
      selectedHour = viewState.calendarTimeValue,
      onConfirmTap = { viewProxy.onTimeChanged(it) },
      onDismissTap = { viewProxy.onTimePickerDismiss() }
    )
  }

  if (viewState.isTimerOn && viewState.editTime.not()) {
    ThermostatTimerInProgress(viewState, viewProxy)
  } else {
    ThermostatTimerConfiguration(viewState, viewProxy)
  }
}

@Preview
@Composable
private fun PreviewInProgress() {
  SuplaTheme {
    ThermostatTimerDetail(PreviewProxy(TimerDetailViewState(isTimerOn = false)))
  }
}

@Preview
@Composable
private fun PreviewConfiguration() {
  SuplaTheme {
    ThermostatTimerDetail(PreviewProxy(TimerDetailViewState(isTimerOn = true)))
  }
}

private class PreviewProxy(val state: TimerDetailViewState) : TimerDetailViewProxy {
  override val timerLeftTime: Int = 0
  override fun getViewState(): StateFlow<TimerDetailViewState> =
    MutableStateFlow(state)
}
