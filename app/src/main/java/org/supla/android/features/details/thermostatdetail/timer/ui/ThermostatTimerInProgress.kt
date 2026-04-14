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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.extensions.shift
import org.supla.android.features.details.thermostatdetail.timer.SetpointTemperature
import org.supla.android.features.details.thermostatdetail.timer.TimerDetailViewState
import org.supla.android.features.details.thermostatdetail.ui.TimerHeader
import org.supla.android.tools.SuplaPreview
import org.supla.android.tools.SuplaPreviewLandscape
import org.supla.android.ui.views.TimerProgressView
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.buttons.supla.SuplaButton
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.usecase.channel.valueformatter.DefaultValueFormatter
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

interface ThermostatTimerInProgressScope {
  fun editTimer()
  fun formatLeftTime(leftTime: Int?): LocalizedString
  fun cancelTimerStartManual()
  fun cancelTimerStartProgram()
}

@Composable
fun ThermostatTimerInProgressScope.InProgressView(state: TimerDetailViewState) {
  if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    Landscape(state)
  } else {
    Portrait(state)
  }
}

@Composable
private fun ThermostatTimerInProgressScope.Landscape(state: TimerDetailViewState) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceEvenly,
      modifier = Modifier.fillMaxHeight().weight(1f)
    ) {
      TimerHeader(
        state = state,
        modifier = Modifier
          .padding(top = Distance.default)
      )
      EditTimeButton { editTimer() }
    }

    TimerProgress(state)

    BottomButtonsLandscape(modifier = Modifier.weight(1f))
  }
}

@Composable
private fun ThermostatTimerInProgressScope.Portrait(state: TimerDetailViewState) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(MaterialTheme.colorScheme.background)
  ) {
    TimerHeader(
      state = state,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = Distance.default)
    )
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      TimerProgress(state)
      EditTimeButton { editTimer() }
    }
    BottomButtonsPortrait(Modifier.align(Alignment.BottomCenter))
  }
}

@Composable
private fun ThermostatTimerInProgressScope.TimerProgress(state: TimerDetailViewState) {
  var leftTime by remember { mutableStateOf<Int?>(0) }

  LaunchedEffect(state.timerEndDate) {
    do {
      leftTime = state.timerLeftTime
      delay(100.milliseconds)
    } while (leftTime != null)
  }

  Box {
    TimerProgressView(progress = 0f, indeterminate = true)
    Text(
      text = formatLeftTime(leftTime)(LocalContext.current),
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
      textAlign = TextAlign.Center,
      modifier = Modifier.align(Alignment.Center)
    )
  }
}

@Composable
private fun EditTimeButton(onClick: () -> Unit) =
  TextButton(onClick = onClick) {
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
private fun ThermostatTimerInProgressScope.BottomButtonsPortrait(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(start = Distance.default, end = Distance.default, bottom = Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    Text(
      text = stringResource(id = R.string.details_timer_cancel_thermostat),
      style = MaterialTheme.typography.bodyMedium
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      SuplaButton(
        text = stringResource(id = R.string.thermostat_detail_mode_manual),
        onClick = { cancelTimerStartManual() },
        modifier = Modifier.weight(0.5f)
      )
      SuplaButton(
        text = stringResource(id = R.string.thermostat_detail_mode_weekly_schedule),
        onClick = { cancelTimerStartProgram() },
        modifier = Modifier.weight(0.5f)
      )
    }
  }
}

@Composable
private fun ThermostatTimerInProgressScope.BottomButtonsLandscape(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.padding(top = Distance.default, end = Distance.default, bottom = Distance.default),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Distance.small)
  ) {
    Text(
      text = stringResource(id = R.string.details_timer_cancel_thermostat),
      style = MaterialTheme.typography.bodyMedium
    )
    SuplaButton(
      text = stringResource(id = R.string.thermostat_detail_mode_manual),
      onClick = { cancelTimerStartManual() },
    )
    SuplaButton(
      text = stringResource(id = R.string.thermostat_detail_mode_weekly_schedule),
      onClick = { cancelTimerStartProgram() },
    )
  }
}

private val previewScope = object : ThermostatTimerInProgressScope {
  override fun editTimer() {}
  override fun formatLeftTime(leftTime: Int?): LocalizedString = LocalizedString.Constant("23:55:45")
  override fun cancelTimerStartManual() {}
  override fun cancelTimerStartProgram() {}
}

@Composable
@SuplaPreview
@SuplaPreviewLandscape
private fun Preview() {
  SuplaTheme {
    previewScope.InProgressView(
      TimerDetailViewState(
        thermometerValueFormatter = DefaultValueFormatter,
        timerEndDate = Date().shift(1),
        currentMode = SuplaHvacMode.HEAT,
        temperature = SetpointTemperature.Heat(22.0f)
      )
    )
  }
}
