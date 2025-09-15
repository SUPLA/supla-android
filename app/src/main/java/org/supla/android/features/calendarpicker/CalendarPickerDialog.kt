package org.supla.android.features.calendarpicker
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.supla.android.R
import org.supla.android.core.ui.theme.SuplaTheme
import org.supla.android.core.ui.theme.gray
import org.supla.android.data.formatting.LocalDateFormatter
import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.model.general.RangeValueType
import org.supla.android.data.source.local.calendar.DayOfWeek
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.extensions.date
import org.supla.android.extensions.dayOfMonth
import org.supla.android.extensions.dayStart
import org.supla.android.extensions.hour
import org.supla.android.extensions.inside
import org.supla.android.extensions.monthEnd
import org.supla.android.extensions.monthNo
import org.supla.android.extensions.monthStart
import org.supla.android.extensions.nextDay
import org.supla.android.extensions.nextMonth
import org.supla.android.extensions.previousMonth
import org.supla.android.extensions.sameDay
import org.supla.android.extensions.setHour
import org.supla.android.extensions.shift
import org.supla.android.extensions.weekEnd
import org.supla.android.extensions.weekStart
import org.supla.android.ui.dialogs.Dialog
import org.supla.android.ui.views.Separator
import org.supla.android.ui.views.SeparatorStyle
import org.supla.android.ui.views.TextField
import org.supla.android.ui.views.buttons.Button
import org.supla.android.ui.views.buttons.IconButton
import org.supla.android.ui.views.buttons.TextButton
import org.supla.core.shared.extensions.guardLet
import org.supla.core.shared.extensions.ifLet
import java.util.Date

sealed interface CalendarPickerState {
  val currentDate: Date
  val initialDate: Date
  val saveEnabled: Boolean
  val selectableRange: DateRange?
}

data class CalendarRangePickerState(
  val selectedRange: DateRange? = null,
  override val currentDate: Date = Date(),
  override val selectableRange: DateRange? = null
) : CalendarPickerState {
  override val saveEnabled: Boolean
    get() = selectedRange != null

  override val initialDate: Date
    get() {
      ifLet(selectedRange?.start) { (start) -> return start }
      return currentDate
    }
}

/**
 * Structure is prepared but CalendarPickerDialog is not ready to work with it.
 */
data class CalendarDatePickerState(
  var selectedDate: Date? = null,
  override val currentDate: Date = Date(),
  override val selectableRange: DateRange? = null
) : CalendarPickerState {
  override val saveEnabled: Boolean
    get() = selectedDate != null

  override val initialDate: Date
    get() {
      ifLet(selectedDate) { (date) -> return date }
      return currentDate
    }
}

private val calendarDaySize = 32.dp

@Composable
fun CalendarPickerDialog(
  state: CalendarRangePickerState,
  onSelected: (startDate: Date?, endDate: Date?) -> Unit,
  onSave: () -> Unit,
  onCancel: () -> Unit,
  onDismiss: () -> Unit
) {
  var visibleDate by remember { mutableStateOf(state.initialDate) }

  val startDate = remember { mutableStateOf(state.selectedRange?.start) }
  val endDate = remember { mutableStateOf(state.selectedRange?.end) }

  val startHour = remember { mutableStateOf(state.selectedRange?.start?.hour() ?: Hour(0, 0)) }
  val endHour = remember { mutableStateOf(state.selectedRange?.end?.hour() ?: Hour(23, 59)) }

  val startHourCorrect = remember { mutableStateOf(true) }
  val endHourCorrect = remember { mutableStateOf(true) }

  Dialog(
    onDismiss = onDismiss,
    usePlatformDefaultWidth = false,
    modifier = Modifier.padding(
      start = dimensionResource(id = R.dimen.distance_default),
      end = dimensionResource(id = R.dimen.distance_default)
    )
  ) {
    DialogHeader(
      visibleDate = visibleDate,
      forward = { visibleDate = visibleDate.nextMonth() },
      backward = { visibleDate = visibleDate.previousMonth() },
      state = state
    )

    CalendarHeader()
    CalendarWeeks(state = state, visibleDate) {
      handleDateSelection(it, startDate, endDate, startHour.value, endHour.value, onSelected)
    }
    CalendarHourInput(state, startHour.value, endHour.value, startHourCorrect, endHourCorrect) { hour, selectionType ->
      handleHourSelection(hour, selectionType, startDate, endDate, startHour, endHour, onSelected)
    }

    Separator(style = SeparatorStyle.LIGHT, modifier = Modifier.padding(top = dimensionResource(id = R.dimen.distance_default)))
    DialogButtons(saveAllowed = startHourCorrect.value && endHourCorrect.value, onSave, onCancel, state)
  }
}

@Composable
private fun DialogHeader(visibleDate: Date, state: CalendarPickerState, forward: () -> Unit, backward: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny)),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(
      icon = R.drawable.ic_arrow_right,
      onClick = backward,
      rotate = true,
      enabled = state.selectableRange?.start?.before(visibleDate.monthStart()) ?: true
    )
    Text(
      text = LocalDateFormatter.current.getMonthAndYearString(visibleDate)?.capitalize(Locale.current) ?: "",
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.weight(1f)
    )
    IconButton(
      icon = R.drawable.ic_arrow_right,
      onClick = forward,
      enabled = state.selectableRange?.end?.after(visibleDate.monthEnd()) ?: true
    )
  }
}

@Composable
private fun DialogButtons(saveAllowed: Boolean, onSave: () -> Unit, onCancel: () -> Unit, state: CalendarPickerState) {
  Button(
    onClick = onSave,
    text = stringResource(id = R.string.save),
    enabled = state.saveEnabled && saveAllowed,
    modifier = Modifier
      .padding(
        start = dimensionResource(id = R.dimen.distance_small),
        top = dimensionResource(id = R.dimen.distance_small),
        end = dimensionResource(id = R.dimen.distance_small)
      )
      .fillMaxWidth()
  )
  TextButton(
    onClick = onCancel,
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

@Composable
private fun CalendarHeader() {
  Row(
    horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny)),
    modifier = Modifier.padding(
      start = dimensionResource(id = R.dimen.distance_small),
      top = dimensionResource(id = R.dimen.distance_tiny),
      end = dimensionResource(id = R.dimen.distance_small)
    )
  ) {
    Spacer(modifier = Modifier.weight(1f))
    DayOfWeek.entries.forEach {
      CalendarDayBox(
        text = stringResource(id = it.shortText),
        header = true
      )
    }
    Spacer(modifier = Modifier.weight(1f))
  }
}

@Composable
private fun CalendarWeeks(state: CalendarPickerState, visibleDate: Date, onDaySelected: (Date) -> Unit) {
  monthWeeks(visibleDate).forEach { dateRange ->
    Row(
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_tiny)),
      modifier = Modifier.padding(
        start = dimensionResource(id = R.dimen.distance_small),
        top = dimensionResource(id = R.dimen.distance_tiny),
        end = dimensionResource(id = R.dimen.distance_small)
      )
    ) {
      Spacer(modifier = Modifier.weight(1f))

      var date = dateRange.start
      (0..6).forEach { i ->
        val day = date.dayStart()
        CalendarDayBox(
          text = day.dayOfMonth.toString(),
          currentDay = day.sameDay(state.currentDate),
          enabled = isDayEnabled(day, visibleDate, state),
          selected = isDaySelected(day, state),
          highlighted = isDayHighlighted(day, state)
        ) {
          onDaySelected(day)
        }
        date = date.shift(1)
      }

      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun CalendarHourInput(
  state: CalendarPickerState,
  initialStartHour: Hour,
  initialEndHour: Hour,
  startHourCorrect: MutableState<Boolean>,
  endHourCorrect: MutableState<Boolean>,
  onHourSelected: (Hour, RangeValueType?) -> Unit
) {
  if (state is CalendarRangePickerState) {
    val context = LocalContext.current
    var startHour by remember { mutableStateOf(initialStartHour.toString(context)) }
    var endHour by remember { mutableStateOf(initialEndHour.toString(context)) }

    Row(
      modifier = Modifier.padding(
        start = dimensionResource(id = R.dimen.distance_small),
        end = dimensionResource(id = R.dimen.distance_small)
      ),
      horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.distance_small))
    ) {
      Column(modifier = Modifier.weight(1f)) {
        CalendarHourTextField(
          label = stringResource(id = R.string.calendar_picker_start_hour),
          value = startHour,
          correct = startHourCorrect.value,
          horizontalPadding = 0.dp
        ) {
          val hour = Hour.from(it)
          if (hour != null) {
            onHourSelected(hour, RangeValueType.START)
          }
          startHourCorrect.value = hour != null
          startHour = it
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        CalendarHourTextField(
          label = stringResource(id = R.string.calendar_picker_end_hour),
          value = endHour,
          correct = endHourCorrect.value,
          horizontalPadding = 0.dp
        ) {
          val hour = Hour.from(it)
          var correct = false
          if (hour != null) {
            onHourSelected(hour, RangeValueType.END)

            correct = true
            if (state.selectedRange != null) {
              val endDate = state.selectedRange.end.setHour(hour.hour, hour.minute, 59)
              if (endDate.before(state.selectedRange.start)) {
                correct = false
              }
            }
          }
          endHourCorrect.value = correct
          endHour = it
        }
      }
    }
  } else {
    var hourCorrect by remember { mutableStateOf(true) }
    CalendarHourTextField(label = stringResource(id = R.string.calendar_picker_end_hour), correct = hourCorrect, value = "00:00") {
      val hour = Hour.from(it)
      if (hour != null) {
        onHourSelected(hour, null)
      }
      hourCorrect = hour != null
    }
  }
}

@Composable
private fun CalendarHourTextField(
  label: String,
  value: String,
  correct: Boolean,
  horizontalPadding: Dp = dimensionResource(id = R.dimen.distance_small),
  onChange: (String) -> Unit = { }
) {
  Text(
    text = label.uppercase(),
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.gray,
    modifier = Modifier.padding(
      start = horizontalPadding.plus(12.dp),
      top = dimensionResource(id = R.dimen.distance_default),
      end = horizontalPadding
    )
  )
  TextField(
    value = value,
    modifier = Modifier.padding(
      start = horizontalPadding,
      top = 4.dp,
      end = horizontalPadding
    ),
    isError = correct.not(),
    onValueChange = onChange
  )
}

@Composable
private fun CalendarDayBox(
  text: String,
  currentDay: Boolean = false,
  header: Boolean = false,
  enabled: Boolean = true,
  selected: Boolean = false,
  highlighted: Boolean = false,
  onClick: () -> Unit = {}
) {
  val style = if (selected || currentDay || header) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall
  val color = when {
    selected -> MaterialTheme.colorScheme.primary
    highlighted -> MaterialTheme.colorScheme.primary
    enabled -> MaterialTheme.colorScheme.onBackground
    else -> colorResource(id = R.color.disabled)
  }

  var modifier = Modifier
    .width(calendarDaySize)
    .height(calendarDaySize)
  if (currentDay) {
    modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
  }
  if (selected) {
    modifier = modifier.border(width = 1.dp, color = color, shape = CircleShape)
  }

  Box(
    modifier = modifier.clickable(
      onClick = {
        if (enabled) {
          onClick()
        }
      },
      indication = if (enabled) ripple() else null,
      interactionSource = remember { MutableInteractionSource() }
    )
  ) {
    Text(
      text = text,
      style = style,
      textAlign = TextAlign.Center,
      modifier = Modifier.align(Alignment.Center),
      color = color
    )
  }
}

private fun monthWeeks(visibleDate: Date): List<DateRange> {
  val monthEnd = visibleDate.monthEnd()
  return mutableListOf<DateRange>().also {
    val firstStart = visibleDate.monthStart().weekStart()
    val firstEnd = firstStart.weekEnd()
    it.add(DateRange(firstStart, firstEnd))

    var next = firstEnd.nextDay()
    do {
      val nextEnd = next.weekEnd()
      it.add(DateRange(next, nextEnd))
      next = nextEnd.nextDay()
    } while (next.time <= monthEnd.time)
  }
}

private fun isDaySelected(day: Date, state: CalendarPickerState): Boolean {
  return when (state) {
    is CalendarDatePickerState ->
      state.selectedDate?.sameDay(day) == true

    is CalendarRangePickerState ->
      state.selectedRange?.start?.sameDay(day) == true || state.selectedRange?.end?.sameDay(day) == true
  }
}

private fun isDayEnabled(day: Date, visibleDate: Date, state: CalendarPickerState): Boolean {
  when (state) {
    is CalendarDatePickerState ->
      return isDaySelectable(day, visibleDate, state)

    is CalendarRangePickerState -> {
      val (range) = guardLet(state.selectedRange) { return isDaySelectable(day, visibleDate, state) }
      return if (range.start != range.end) {
        isDaySelectable(day, visibleDate, state)
      } else if (day.before(range.start.dayStart())) {
        false
      } else {
        isDaySelectable(day, visibleDate, state)
      }
    }
  }
}

private fun isDaySelectable(day: Date, visibleDate: Date, state: CalendarPickerState): Boolean {
  val (selectableRange) = guardLet(state.selectableRange) { return day.monthNo == visibleDate.monthNo }

  return day.inside(selectableRange) && day.monthNo == visibleDate.monthNo
}

private fun isDayHighlighted(day: Date, state: CalendarPickerState): Boolean {
  when (state) {
    is CalendarDatePickerState ->
      return false

    is CalendarRangePickerState -> {
      val (range) = guardLet(state.selectedRange) { return false }
      return day.after(range.start) && day.before(range.end)
    }
  }
}

private fun handleDateSelection(
  date: Date,
  startDate: MutableState<Date?>,
  endDate: MutableState<Date?>,
  startHour: Hour,
  endHour: Hour,
  handler: (Date?, Date?) -> Unit
) {
  if (startDate.value == null || endDate.value != null) {
    startDate.value = date
    endDate.value = null
  } else {
    endDate.value = date
  }
  startDate.value = startDate.value?.setHour(startHour.hour, startHour.minute, 0)
  endDate.value = endDate.value?.setHour(endHour.hour, endHour.minute, 59)
  handler(startDate.value, endDate.value)
}

private fun handleHourSelection(
  hour: Hour,
  selectionType: RangeValueType?,
  startDate: MutableState<Date?>,
  endDate: MutableState<Date?>,
  startHour: MutableState<Hour>,
  endHour: MutableState<Hour>,
  handler: (Date?, Date?) -> Unit
) {
  when (selectionType) {
    RangeValueType.START -> startHour.value = hour
    RangeValueType.END -> endHour.value = hour
    else -> {}
  }
  if (startDate.value != null) {
    startDate.value = startDate.value?.setHour(startHour.value.hour, startHour.value.minute, 0)
    endDate.value = endDate.value?.setHour(endHour.value.hour, endHour.value.minute, 59)
    handler(startDate.value, endDate.value)
  }
}

@Preview
@Composable
private fun Preview() {
  SuplaTheme {
    CalendarPickerDialog(CalendarRangePickerState(), onSelected = { _, _ -> }, onSave = {}, onCancel = {}) { }
  }
}

@Preview
@Composable
private fun Preview2() {
  SuplaTheme {
    CalendarPickerDialog(
      CalendarRangePickerState(selectableRange = DateRange(date(2023, 11, 12), date(2023, 11, 15))),
      onSelected = { _, _ -> },
      onSave = {},
      onCancel = {}
    ) { }
  }
}
