package org.supla.android.data.formatting
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

import android.annotation.SuppressLint
import androidx.compose.runtime.compositionLocalOf
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
class DateFormatter {
  private val hourFormatter = SimpleDateFormat("HH:mm")
  private val monthFormatter = SimpleDateFormat("dd MMM")
  private val dateFormatter = SimpleDateFormat("dd.MM.yyyy")
  private val shortDateFormatter = SimpleDateFormat("dd.MM.yy")
  private val fullDateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
  private val dayHourFormatter = SimpleDateFormat("EEEE HH:mm")
  private val dayAndHourFormatter = SimpleDateFormat("dd MMM HH:mm")
  private val dayAndHourShortFormatter = SimpleDateFormat("dd.MM HH:mm")
  private val monthAndYearFormatter = SimpleDateFormat("LLLL yyyy")
  private val yearFormatter = SimpleDateFormat("yyyy")

  fun getHourString(date: Date?): String? =
    date?.let { hourFormatter.format(it) }

  fun getMonthString(date: Date?): String? =
    date?.let { monthFormatter.format(it) }

  fun getDateString(date: Date?): String? =
    date?.let { dateFormatter.format(it) }

  fun getShortDateString(date: Date?): String? =
    date?.let { shortDateFormatter.format(it) }

  fun getFullDateString(date: Date?): String? =
    date?.let { fullDateFormatter.format(it) }

  fun getDayHourDateString(date: Date?): String? =
    date?.let { dayHourFormatter.format(it) }

  fun getDayAndHourDateString(date: Date?): String? =
    date?.let { dayAndHourFormatter.format(it) }

  fun getDayAndHourShortDateString(date: Date?): String? =
    date?.let { dayAndHourShortFormatter.format(it) }

  fun getMonthAndYearString(date: Date?): String? =
    date?.let { monthAndYearFormatter.format(it) }

  fun getYearString(date: Date?): String? =
    date?.let { yearFormatter.format(it) }
}

val LocalDateFormatter = compositionLocalOf { DateFormatter() }
