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

class DateFormatter {
  @SuppressLint("SimpleDateFormat")
  fun getHourString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("HH:mm")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getMonthString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd MMM")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getDateString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd.MM.yyyy")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getShortDateString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd.MM.yy")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getFullDateString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getDayHourDateString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("EEEE HH:mm")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getDayAndHourDateString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd MMM HH:mm")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getMonthAndYearString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("LLLL yyyy")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getYearString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("yyyy")
      formatter.format(it)
    }
}

val LocalDateFormatter = compositionLocalOf { DateFormatter() }
