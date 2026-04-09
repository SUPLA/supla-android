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

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.model.temperature.TemperatureCorrection
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.features.details.thermostatdetail.timer.DeviceMode
import org.supla.android.features.details.thermostatdetail.timer.TimerDetailViewState
import org.supla.android.features.details.thermostatdetail.timer.WorkingMode
import org.supla.android.ui.dialogs.TimePickerDialog
import org.supla.core.shared.infrastructure.LocalizedString

interface ThermostatTimerViewScope : ThermostatTimerConfigurationScope, ThermostatTimerInProgressScope {
  fun onTimeChanged(hour: Hour)
  fun onTimePickerDismiss()
}

@Composable
fun ThermostatTimerViewScope.View(viewState: TimerDetailViewState) {
  if (viewState.showTimePicker) {
    TimePickerDialog(
      selectedHour = viewState.calendarTimeValue,
      onConfirmTap = { onTimeChanged(it) },
      onDismissTap = { onTimePickerDismiss() }
    )
  }

  if (viewState.isTimerOn && viewState.editTime.not()) {
    InProgressView(viewState)
  } else {
    ConfigurationView(viewState)
  }
}

private val previewScope = object : ThermostatTimerViewScope {
  override fun onTimeChanged(hour: Hour) {}
  override fun onTimePickerDismiss() {}
  override fun toggleDeviceMode(deviceMode: DeviceMode) {}
  override fun toggleWorkingMode(workingMode: WorkingMode) {}
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
  override fun editTimer() {}
  override fun formatLeftTime(leftTime: Int?) = LocalizedString.Empty
  override fun cancelTimerStartManual() {}
  override fun cancelTimerStartProgram() {}
}

@Preview
@Composable
private fun PreviewInProgress() {
  SuplaTheme {
    previewScope.View(TimerDetailViewState(isTimerOn = false))
  }
}

@Preview
@Composable
private fun PreviewConfiguration() {
  SuplaTheme {
    previewScope.View(TimerDetailViewState(isTimerOn = true))
  }
}
