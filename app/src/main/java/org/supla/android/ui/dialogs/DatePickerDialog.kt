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
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import org.supla.android.R
import org.supla.android.ui.views.DatePicker
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.TextButton
import java.util.Date

@Composable
fun DatePickerDialog(
  selectedDate: Date? = null,
  yearRange: IntRange? = null,
  onConfirmTap: (Date) -> Unit = {},
  onDismissTap: () -> Unit = {},
  dateValidator: (Date) -> Boolean = { true }
) {
  @Suppress("DEPRECATION")
  val state = rememberDatePickerState(
    yearRange = yearRange ?: DatePickerDefaults.YearRange,
    initialSelectedDateMillis = selectedDate?.time?.minus(selectedDate.timezoneOffset.times(60000))
  )

  Dialog(
    onDismiss = {},
    usePlatformDefaultWidth = false,
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.distance_small),
        top = dimensionResource(id = R.dimen.distance_small),
        end = dimensionResource(id = R.dimen.distance_small)
      )
  ) {
    DatePicker(
      state = state,
      modifier = Modifier.padding(top = dimensionResource(id = R.dimen.distance_small)),
      dateValidator = { dateValidator(it) }
    )

    Separator()

    Button(
      onClick = { onConfirmTap(Date(state.selectedDateMillis!!)) },
      text = stringResource(id = R.string.save),
      enabled = state.selectedDateMillis != null,
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
