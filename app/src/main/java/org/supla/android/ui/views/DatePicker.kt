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
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DatePicker(
  state: DatePickerState,
  modifier: Modifier = Modifier,
  dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
  headline: (@Composable () -> Unit)? = {
    DatePickerDefaults.DatePickerHeadline(
      selectedDateMillis = state.selectedDateMillis,
      displayMode = state.displayMode,
      dateFormatter = dateFormatter,
      modifier = Modifier.padding(PaddingValues(start = 24.dp, end = 12.dp, bottom = 12.dp))
    )
  },
  showModeToggle: Boolean = true
) {
  val colors = DatePickerDefaults.colors(
    containerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    headlineContentColor = MaterialTheme.colorScheme.onSurface,
    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
    yearContentColor = MaterialTheme.colorScheme.onSurface,
    currentYearContentColor = MaterialTheme.colorScheme.onSurface,
    selectedYearContentColor = MaterialTheme.colorScheme.onSurface,
    selectedYearContainerColor = MaterialTheme.colorScheme.surface,
    dayContentColor = MaterialTheme.colorScheme.onSurface,
    disabledDayContentColor = MaterialTheme.colorScheme.outline,
    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
    disabledSelectedDayContentColor = MaterialTheme.colorScheme.onSurface,
    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
    disabledSelectedDayContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    todayContentColor = MaterialTheme.colorScheme.onSurface,
    todayDateBorderColor = MaterialTheme.colorScheme.primary
  )

  androidx.compose.material3.DatePicker(
    state = state,
    colors = colors,
    title = null,
    modifier = modifier,
    dateFormatter = dateFormatter,
    headline = headline,
    showModeToggle = showModeToggle
  )
}
