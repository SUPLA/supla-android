package org.supla.android.features.details.relayschedule.ui
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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.core.ui.theme.Distance
import org.supla.android.features.details.relayschedule.OperatingMode
import org.supla.android.ui.extensions.ifTrue
import org.supla.android.ui.views.buttons.SwitchButton
import org.supla.android.ui.views.buttons.SwitchButtonsLayout
import org.supla.android.ui.views.buttons.supla.SuplaButtonDefaults

interface OperatingButtonsScope {
  fun onManual()
  fun onWeekly()
  fun onAuto()
}

@Composable
fun OperatingButtonsScope.OperatingButtons(operatingMode: OperatingMode) {
  SwitchButtonsLayout(
    modifier = Modifier.padding(horizontal = Distance.horizontal, vertical = Distance.vertical),
    defaultWidth = 215
  ) {
    operatingMode.manualAllowed.ifTrue {
      SwitchButton(
        icon = null,
        text = stringResource(R.string.thermostat_detail_mode_manual),
        colors = SuplaButtonDefaults.primaryColors(),
        pressed = operatingMode.manualActive,
        onClick = { onManual() },
        modifier = Modifier.fillMaxWidth()
      )
    }

    operatingMode.weeklyAllowed.ifTrue {
      SwitchButton(
        icon = null,
        text = stringResource(R.string.thermostat_detail_mode_weekly_schedule),
        colors = SuplaButtonDefaults.primaryColors(),
        pressed = operatingMode.weeklyActive,
        onClick = { onWeekly() },
        modifier = Modifier.fillMaxWidth()
      )
    }

    operatingMode.autoAllowed.ifTrue {
      SwitchButton(
        icon = null,
        text = stringResource(R.string.auto),
        colors = SuplaButtonDefaults.primaryColors(),
        pressed = operatingMode.autoActive,
        onClick = { onAuto() },
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}
