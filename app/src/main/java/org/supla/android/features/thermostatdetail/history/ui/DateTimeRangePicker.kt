package org.supla.android.features.thermostatdetail.history.ui
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.calendarCaption
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.extensions.dayOfMonth
import org.supla.android.extensions.monthEnd
import org.supla.android.extensions.monthStart
import org.supla.android.extensions.nextDay
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.features.thermostatdetail.history.data.DateRange
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.views.buttons.IconButton
import java.util.Date

data class DateTimeRangeState(
  var title: String,
  var startDate: Date = Date()
) {
  val weeks: List<DateRange>
    get() {
      val monthEnd = startDate.monthEnd()
      return mutableListOf<DateRange>().also {
        val firstStart = startDate.monthStart().weekStart()
        val firstEnd = startDate.weekEnd()
        it.add(DateRange(firstStart, firstEnd))

        var next = firstEnd.nextDay()
        do {
          val nextEnd = next.weekEnd()
          it.add(DateRange(next, nextEnd))
          next = nextEnd.nextDay()
        } while (next.time <= monthEnd.time)
      }
    }
}

@Composable
fun DateTimeRangePicker(state: DateTimeRangeState, onDismiss: () -> Unit) {
  Dialog(onDismiss = onDismiss) {
    DialogHeader(title = state.title)
    Calendar(state)
  }
}

@Composable
private fun DialogHeader(title: String) =
  Row(
    modifier = Modifier
      .padding(vertical = dimensionResource(id = R.dimen.distance_small), horizontal = dimensionResource(id = R.dimen.distance_tiny))
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(icon = R.drawable.ic_arrow_right, onClick = { }, rotate = true)
    Text(
      text = title,
      style = MaterialTheme.typography.h6,
      textAlign = TextAlign.Center,
      modifier = Modifier.weight(1f)
    )
    IconButton(icon = R.drawable.ic_arrow_right, onClick = { })
  }

@Composable
private fun Calendar(state: DateTimeRangeState) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_small)),
    modifier = Modifier.padding(
      start = dimensionResource(id = R.dimen.distance_small),
      top = dimensionResource(id = R.dimen.distance_small),
      end = dimensionResource(id = R.dimen.distance_small)
    )
  ) {
    Spacer(modifier = Modifier.weight(1f))
    DayOfWeek.values().forEach {
      Text(
        text = stringResource(id = it.shortText),
        style = MaterialTheme.typography.calendarCaption(),
        modifier = Modifier.defaultMinSize(minWidth = 16.dp)
      )
    }
    Spacer(modifier = Modifier.weight(1f))
  }

  state.weeks.forEach {
    Row(
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_small)),
      modifier = Modifier.padding(
        start = dimensionResource(id = R.dimen.distance_small),
        top = dimensionResource(id = R.dimen.distance_small),
        end = dimensionResource(id = R.dimen.distance_small)
      )
    ) {
      Spacer(modifier = Modifier.weight(1f))
      var day = it.start
      for (i in 0..it.daysCount) {
        Text(
          text = stringResource(id = day.dayOfMonth),
          style = MaterialTheme.typography.calendarCaption(),
          modifier = Modifier.defaultMinSize(minWidth = 16.dp)
        )
        day = day.nextDay()
      }
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    DateTimeRangePicker(state = DateTimeRangeState("June 2021"), onDismiss = { })
  }
}
