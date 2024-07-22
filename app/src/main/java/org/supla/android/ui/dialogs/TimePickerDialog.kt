@file:OptIn(ExperimentalMaterial3Api::class)

package org.supla.android.ui.dialogs
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.TextButton

@Composable
fun TimePickerDialog(
  selectedHour: Hour? = null,
  onConfirmTap: (Hour) -> Unit = {},
  onDismissTap: () -> Unit = {}
) {
  val state = rememberTimePickerState(
    is24Hour = true,
    initialHour = selectedHour?.hour ?: 0,
    initialMinute = selectedHour?.minute ?: 0
  )

  val colors = TimePickerDefaults.colors(
    clockDialColor = MaterialTheme.colorScheme.background,
    clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onBackground,
    selectorColor = MaterialTheme.colorScheme.primary,
    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onBackground,
    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onBackground
  )

  Dialog(
    onDismiss = {},
    usePlatformDefaultWidth = false,
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.distance_small),
        top = dimensionResource(id = R.dimen.distance_small),
        end = dimensionResource(id = R.dimen.distance_small)
      ),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    TimePicker(
      state = state,
      colors = colors,
      modifier = Modifier.padding(top = dimensionResource(id = R.dimen.distance_small))
    )

    Separator()

    Button(
      onClick = { onConfirmTap(Hour(state.hour, state.minute)) },
      text = stringResource(id = R.string.save),
      modifier = Modifier
        .padding(
          start = dimensionResource(id = R.dimen.distance_small),
          top = dimensionResource(id = R.dimen.distance_small),
          end = dimensionResource(id = R.dimen.distance_small)
        )
        .fillMaxWidth()
    )
    TextButton(
      onClick = onDismissTap,
      text = stringResource(id = R.string.cancel),
      modifier = Modifier
        .padding(
          start = dimensionResource(id = R.dimen.distance_small),
          bottom = dimensionResource(id = R.dimen.distance_small),
          end = dimensionResource(id = R.dimen.distance_small)
        )
        .fillMaxWidth()
    )
  }
}
