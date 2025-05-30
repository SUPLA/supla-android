package org.supla.android.extensions
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

import org.supla.android.data.model.chart.DateRange
import org.supla.android.data.source.local.calendar.Hour
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.abs

val Date.dayOfMonth: Int
  get() = Calendar.getInstance().let {
    it.time = this
    it.get(Calendar.DAY_OF_MONTH)
  }

val Date.monthNo: Int
  get() = Calendar.getInstance().let {
    it.time = this
    it.get(Calendar.MONTH)
  }

val Date.yearNo: Int
  get() = Calendar.getInstance().let {
    it.time = this
    it.get(Calendar.YEAR)
  }

val Date.hourOfDay: Int
  get() = Calendar.getInstance().let {
    it.time = this
    it.get(Calendar.HOUR_OF_DAY)
  }

val Date.minute: Int
  get() = Calendar.getInstance().let {
    it.time = this
    it.get(Calendar.MINUTE)
  }

fun Date.dayStart(): Date = startOfDay(this).time

fun Date.dayEnd(): Date = endOfDay(this).time

fun Date.beginOfNextHour(): Date =
  Calendar.getInstance().let {
    it.time = this
    it.set(Calendar.HOUR_OF_DAY, it.get(Calendar.HOUR_OF_DAY) + 1)
    it.set(Calendar.MINUTE, 0)
    it.time
  }

fun Date.inHalfOfHour(): Date =
  Calendar.getInstance().let {
    it.time = this
    it.set(Calendar.MINUTE, 30)
    it.time
  }

fun Date.dayNoon(): Date =
  startOfDay(this).let {
    it.set(Calendar.HOUR_OF_DAY, 12)
    it.time
  }

fun Date.nextDay(): Date =
  startOfDay(this).let {
    it.set(Calendar.DAY_OF_YEAR, it.get(Calendar.DAY_OF_YEAR) + 1)
    it.time
  }

fun Date.weekStart(): Date =
  startOfDay(this).let {
    it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    it.time
  }

fun Date.weekEnd(): Date =
  endOfDay(this).let {
    it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    it.time
  }

fun Date.monthStart(): Date =
  startOfDay(this).let {
    it.set(Calendar.DAY_OF_MONTH, 1)
    it.time
  }

fun Date.monthEnd(): Date =
  endOfDay(this).let {
    it.set(Calendar.DAY_OF_MONTH, it.getActualMaximum(Calendar.DAY_OF_MONTH))
    it.time
  }

fun Date.monthHalf(): Date =
  startOfDay(this).let {
    it.set(Calendar.DAY_OF_MONTH, 15)
    it.time
  }

fun Date.previousMonth(): Date =
  startOfDay(this).let {
    it.set(Calendar.MONTH, it.get(Calendar.MONTH) - 1)
    it.time
  }

fun Date.nextMonth(): Date =
  endOfDay(this).let {
    it.set(Calendar.MONTH, it.get(Calendar.MONTH) + 1)
    it.time
  }

fun Date.quarterStart(): Date =
  startOfDay(this).let {
    val currentQuarterFirstMonth = it.get(Calendar.MONTH).div(3).times(3)
    it.set(Calendar.MONTH, currentQuarterFirstMonth)
    it.set(Calendar.DAY_OF_MONTH, 1)
    it.time
  }

fun Date.quarterEnd(): Date =
  endOfDay(this).let {
    val currentQuarterFirstMonth = it.get(Calendar.MONTH).div(3).times(3)
    it.set(Calendar.MONTH, currentQuarterFirstMonth + 2)
    it.set(Calendar.DAY_OF_MONTH, it.getActualMaximum(Calendar.DAY_OF_MONTH))
    it.time
  }

fun Date.yearStart(): Date =
  startOfDay(this).let {
    it.set(Calendar.MONTH, Calendar.JANUARY)
    it.set(Calendar.DAY_OF_MONTH, 1)
    it.time
  }

fun Date.yearEnd(): Date =
  endOfDay(this).let {
    it.set(Calendar.MONTH, Calendar.DECEMBER)
    it.set(Calendar.DAY_OF_MONTH, 31)
    it.time
  }

fun Date.yearHalf(): Date =
  startOfDay(this).let {
    it.set(Calendar.MONTH, Calendar.JULY)
    it.set(Calendar.DAY_OF_MONTH, 1)
    it.time
  }

fun Date.daysInCurrentMonth(): Int {
  val calendar = Calendar.getInstance()
  calendar.time = this
  return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

fun Date.daysInCurrentYear(): Int {
  val calendar = Calendar.getInstance()
  calendar.time = this
  return calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
}

fun Date.daysInCurrentQuarter(): Int {
  val calendar = Calendar.getInstance()
  calendar.time = this

  val currentYear = calendar.get(Calendar.YEAR)

  return when (val currentMonth = calendar.get(Calendar.MONTH)) {
    in listOf(1, 2, 3) -> getMonthLength(currentYear, listOf(1, 2, 3))
    in listOf(4, 5, 6) -> getMonthLength(currentYear, listOf(4, 5, 6))
    in listOf(7, 8, 9) -> getMonthLength(currentYear, listOf(7, 8, 9))
    in listOf(10, 11, 12) -> getMonthLength(currentYear, listOf(10, 11, 12))
    else -> throw IllegalStateException("Invalid month `$currentMonth`!")
  }
}

fun Date.shift(days: Int): Date =
  Calendar.getInstance().let {
    it.time = this
    it.add(Calendar.DATE, days)
    it.time
  }

fun Date.shiftByHour(hour: Int): Date =
  Date(time + hour.toLong().times(60).times(60).times(1000))

fun Date.toTimestamp(): Long = time.div(1000)

fun date(year: Int, month: Int = Calendar.JANUARY, day: Int = Calendar.MONDAY, hour: Int = 0, minute: Int = 0, seconds: Int = 0): Date =
  Calendar.getInstance().let {
    it.set(Calendar.YEAR, year)
    it.set(Calendar.MONTH, month)
    it.set(Calendar.DAY_OF_MONTH, day)
    it.set(Calendar.HOUR_OF_DAY, hour)
    it.set(Calendar.MINUTE, minute)
    it.set(Calendar.SECOND, seconds)
    it.set(Calendar.MILLISECOND, 0)

    it.time
  }

fun Date.sameDay(date: Date): Boolean =
  this.dayOfMonth == date.dayOfMonth && this.monthNo == date.monthNo && this.yearNo == date.yearNo

fun Date.setHour(hour: Int, minute: Int, seconds: Int): Date {
  return Calendar.getInstance().let {
    it.time = this

    it.set(Calendar.HOUR_OF_DAY, hour)
    it.set(Calendar.MINUTE, minute)
    it.set(Calendar.SECOND, seconds)

    it.time
  }
}

fun Date.setDay(day: Date): Date {
  return Calendar.getInstance().let {
    it.time = this

    it.set(Calendar.YEAR, day.yearNo)
    it.set(Calendar.MONTH, day.monthNo)
    it.set(Calendar.DAY_OF_MONTH, day.dayOfMonth)

    it.time
  }
}

fun Date.inside(range: DateRange): Boolean =
  after(range.start) && before(range.end)

fun Date.hour() = Hour(hourOfDay, minute)

fun Date.differenceInSeconds(other: Date): Int =
  abs(this.time.minus(other.time).div(1000).toInt())

private fun getMonthLength(year: Int, months: List<Int>) =
  months.sumOf { GregorianCalendar(year, it, 1).getActualMaximum(Calendar.DAY_OF_MONTH) }

private fun startOfDay(day: Date): Calendar {
  val calendar = Calendar.getInstance()
  calendar.time = day
  calendar.firstDayOfWeek = Calendar.MONDAY
  calendar.set(Calendar.HOUR_OF_DAY, 0)
  calendar.set(Calendar.MINUTE, 0)
  calendar.set(Calendar.SECOND, 0)
  calendar.set(Calendar.MILLISECOND, 0)

  return calendar
}

private fun endOfDay(day: Date): Calendar {
  val calendar = Calendar.getInstance()
  calendar.time = day
  calendar.firstDayOfWeek = Calendar.MONDAY
  calendar.set(Calendar.HOUR_OF_DAY, 23)
  calendar.set(Calendar.MINUTE, 59)
  calendar.set(Calendar.SECOND, 59)
  calendar.set(Calendar.MILLISECOND, 999)

  return calendar
}
