package org.supla.android.features.thermostatdetail.ui
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.core.ui.theme.grey
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.valuesFormatter
import java.util.Date

interface TimerHeaderState {
  val endDateText: StringProvider
  val currentStateIcon: Int?
  val currentStateIconColor: Int
  val currentStateValue: StringProvider

  companion object {
    fun endDateText(timerEndDate: Date?): StringProvider {
      val (endDate) = guardLet(timerEndDate) { return { "" } }

      return { context ->
        val date = context.valuesFormatter.getFullDateString(endDate)
        context.getString(R.string.details_timer_state_label_for_timer_days, date)
      }
    }

    fun currentStateIcon(mode: SuplaHvacMode?): Int? =
      when (mode) {
        SuplaHvacMode.HEAT -> R.drawable.ic_heat
        SuplaHvacMode.COOL -> R.drawable.ic_cool
        else -> null
      }

    fun currentStateIconColor(mode: SuplaHvacMode?): Int =
      when (mode) {
        SuplaHvacMode.HEAT -> R.color.red
        SuplaHvacMode.COOL -> R.color.blue
        else -> R.color.disabled
      }

    fun currentStateValue(mode: SuplaHvacMode?, heatSetpoint: Float?, coolSetpoint: Float?): StringProvider =
      when (mode) {
        SuplaHvacMode.OFF -> { _ -> "OFF" }
        SuplaHvacMode.HEAT -> { context ->
          context.valuesFormatter.getTemperatureString(heatSetpoint)
        }

        SuplaHvacMode.COOL -> { context ->
          context.valuesFormatter.getTemperatureString(coolSetpoint)
        }

        else -> { _ -> "" }
      }
  }
}

@Composable
fun TimerHeader(state: TimerHeaderState, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = state.endDateText(LocalContext.current).uppercase(),
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
      text = state.currentStateValue(LocalContext.current),
      style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
    )
    Spacer(modifier = Modifier.weight(1f))
  }
}
