@file:OptIn(ExperimentalMaterial3Api::class)

package org.supla.android.ui.views
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.supla.android.core.ui.theme.disabled
import org.supla.android.core.ui.theme.primaryLight
import java.util.Date

@Composable
fun DatePicker(
  state: DatePickerState,
  modifier: Modifier = Modifier,
  dateValidator: (Date) -> Boolean = { true },
  dateFormatter: DatePickerFormatter = remember { DatePickerFormatter() },
  headline: (@Composable () -> Unit)? = {
    DatePickerDefaults.DatePickerHeadline(
      state,
      dateFormatter,
      modifier = Modifier.padding(PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp))
    )
  },
  showModeToggle: Boolean = true
) {
  val colors = DatePickerDefaults.colors(
    containerColor = MaterialTheme.colors.surface,
    titleContentColor = MaterialTheme.colors.onSurface,
    headlineContentColor = MaterialTheme.colors.onSurface,
    weekdayContentColor = MaterialTheme.colors.onSurface,
    yearContentColor = MaterialTheme.colors.onSurface,
    currentYearContentColor = MaterialTheme.colors.onSurface,
    selectedYearContentColor = MaterialTheme.colors.onSurface,
    selectedYearContainerColor = MaterialTheme.colors.surface,
    dayContentColor = MaterialTheme.colors.onSurface,
    disabledDayContentColor = Color.disabled,
    selectedDayContentColor = MaterialTheme.colors.onPrimary,
    disabledSelectedDayContentColor = MaterialTheme.colors.onSurface,
    selectedDayContainerColor = MaterialTheme.colors.primary,
    disabledSelectedDayContainerColor = Color.primaryLight,
    todayContentColor = MaterialTheme.colors.onSurface,
    todayDateBorderColor = MaterialTheme.colors.primary
  )

  androidx.compose.material3.DatePicker(
    state = state,
    colors = colors,
    title = null,
    modifier = modifier,
    dateValidator = { dateValidator(Date(it)) },
    dateFormatter = dateFormatter,
    headline = headline,
    showModeToggle = showModeToggle
  )
}
