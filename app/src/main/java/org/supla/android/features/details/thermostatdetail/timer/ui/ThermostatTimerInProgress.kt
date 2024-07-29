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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.features.details.thermostatdetail.timer.TimerDetailViewState
import org.supla.android.features.details.thermostatdetail.ui.TimerHeader
import org.supla.android.ui.views.TimerProgressView
import org.supla.android.ui.views.buttons.TextButton
import org.supla.android.ui.views.buttons.supla.SuplaButton
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ThermostatTimerInProgress(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
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
      TimerProgress(state, viewProxy)
      EditTimeButton { viewProxy.editTimer() }
    }
    BottomButtons(viewProxy, modifier = Modifier.align(Alignment.BottomCenter))
  }
}

@Composable
private fun TimerProgress(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  var leftTime by remember { mutableStateOf<Int?>(0) }

  LaunchedEffect(state.timerEndDate) {
    do {
      leftTime = viewProxy.timerLeftTime
      delay(100.milliseconds)
    } while (leftTime != null)
  }

  Box {
    TimerProgressView(progress = 0f, indeterminate = true)
    Text(
      text = viewProxy.formatLeftTime(leftTime)(LocalContext.current),
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
private fun BottomButtons(viewProxy: TimerDetailViewProxy, modifier: Modifier = Modifier) {
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
        onClick = { viewProxy.cancelTimerStartManual() },
        modifier = Modifier.weight(0.5f)
      )
      SuplaButton(
        text = stringResource(id = R.string.thermostat_detail_mode_weekly_schedule),
        onClick = { viewProxy.cancelTimerStartProgram() },
        modifier = Modifier.weight(0.5f)
      )
    }
  }
}
