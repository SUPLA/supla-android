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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.core.ui.theme.grey
import org.supla.android.features.thermostatdetail.timerdetail.TimerDetailViewState
import org.supla.android.ui.views.TimerProgressView
import org.supla.android.ui.views.buttons.AnimationMode
import org.supla.android.ui.views.buttons.RoundedControlButton
import org.supla.android.ui.views.buttons.TextButton
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ThermostatTimerInProgress(state: TimerDetailViewState, viewProxy: TimerDetailViewProxy) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .background(MaterialTheme.colors.background)
  ) {
    TimerState(state, modifier = Modifier.align(Alignment.TopCenter))
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
private fun TimerState(state: TimerDetailViewState, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.padding(top = Distance.default),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = state.estimatedEndDateText(LocalContext.current).uppercase(),
      style = MaterialTheme.typography.body2,
      color = MaterialTheme.colors.grey
    )
    state.currentStateIcon?.let {
      Icon(
        painter = painterResource(id = it),
        contentDescription = null,
        tint = colorResource(id = state.currentStateIconColor),
        modifier = Modifier.size(16.dp)
      )
    }
    Text(
      text = state.currentStateTemperature(LocalContext.current),
      style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
    )
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
      style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold),
      textAlign = TextAlign.Center,
      modifier = Modifier.align(Alignment.Center)
    )
  }
}

@Composable
private fun EditTimeButton(onClick: () -> Unit) =
  TextButton(modifier = Modifier.padding(top = Distance.default), onClick = onClick) {
    Text(
      text = stringResource(id = R.string.details_timer_edit_time),
      color = MaterialTheme.colors.onBackground
    )
    Icon(
      painter = painterResource(id = R.drawable.pencil),
      contentDescription = null,
      modifier = Modifier.padding(start = Distance.tiny)
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
      style = MaterialTheme.typography.body2
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(Distance.tiny)
    ) {
      RoundedControlButton(
        text = stringResource(id = R.string.thermostat_detail_mode_manual),
        modifier = Modifier.weight(0.5f),
        animationMode = AnimationMode.Stated(AnimationMode.State.CLEAR)
      ) { viewProxy.cancelTimerStartManual() }
      RoundedControlButton(
        text = stringResource(id = R.string.thermostat_detail_mode_weekly_schedule),
        modifier = Modifier.weight(0.5f),
        animationMode = AnimationMode.Stated(AnimationMode.State.CLEAR)
      ) { viewProxy.cancelTimerStartProgram() }
    }
  }
}
