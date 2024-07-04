package org.supla.android.data
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
import org.supla.android.Preferences
import org.supla.android.R
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit
import org.supla.android.extensions.days
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.hours
import org.supla.android.extensions.minutesInHour
import org.supla.android.extensions.secondsInMinute
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class ValuesFormatter @Inject constructor(
  private val preferences: Preferences
) {

  fun isTemperatureDefined(rawValue: Double?): Boolean {
    return rawValue != null && rawValue > TEMPERATURE_NA_VALUE
  }

  fun getTemperatureString(rawValue: Float?, withUnit: Boolean = false, withDegree: Boolean = true, precision: Int = 1) =
    getTemperatureString(rawValue?.toDouble(), withUnit, withDegree, precision)

  fun getTemperatureString(rawValue: Double?, withUnit: Boolean = false, withDegree: Boolean = true) =
    getTemperatureString(rawValue, withUnit, withDegree, 1)

  fun getTemperatureString(rawValue: Double?, withUnit: Boolean = false, withDegree: Boolean = true, precision: Int = 1): String {
    return when {
      !isTemperatureDefined(rawValue) && withUnit ->
        String.format("%s%s", NO_VALUE_TEXT, getUnitString())

      !isTemperatureDefined(rawValue) -> NO_VALUE_TEXT

      withUnit -> String.format(
        "%.${precision}f%s",
        getTemperatureInConfiguredUnit(rawValue!!),
        getUnitString()
      )

      else -> String.format(
        "%.${precision}f%s",
        getTemperatureInConfiguredUnit(rawValue!!),
        if (withDegree) getUnitString().substring(0, 1) else ""
      )
    }
  }

  fun getDistanceString(rawValue: Double?): String {
    val (distance) = guardLet(rawValue) { return "$NO_VALUE_TEXT m" }
    if (distance < 0 || distance.isNaN()) {
      return "$NO_VALUE_TEXT m"
    }

    return if (distance >= 1000) {
      String.format("%.2f km", distance / 1000f)
    } else if (distance >= 1) {
      String.format("%.2f m", distance)
    } else if (distance * 100 >= 1) {
      String.format("%.1f cm", distance.times(100))
    } else {
      String.format("%d mm", distance.times(1000).roundToInt())
    }
  }

  fun getTemperatureInConfiguredUnit(value: Double): Double {
    return if (!isTemperatureDefined(value) || isCelsius()) {
      value
    } else {
      toFahrenheit(value)
    }
  }

  fun getTemperatureAndHumidityString(
    temperatureAndHumidity: TemperatureAndHumidity?,
    withUnit: Boolean = false
  ): String {
    val temperatureString = getTemperatureString(temperatureAndHumidity?.temperature, withUnit)
    val humidityString = getHumidityString(temperatureAndHumidity?.humidity)

    return String.format("%s\n%s", temperatureString, humidityString)
  }

  fun getHourWithMinutes(minutes: Int): StringProvider {
    val hours = minutes.div(60)

    if (hours < 1) {
      return { context -> context.getString(R.string.time_just_minutes, minutes) }
    } else {
      return { context -> context.getString(R.string.time_hours_and_minutes, hours, (minutes % 60)) }
    }
  }

  fun getHourString(hour: Hour): String {
    return getTimeString(hour = hour.hour, minute = hour.minute)
  }

  fun getTimeString(hour: Int? = null, minute: Int? = null, second: Int? = null): String {
    var result = ""

    hour?.let { result = if (it < 10) "0$it" else "$it" }
    minute?.let {
      if (result.isNotEmpty()) {
        result += ":"
      }
      result += if (it < 10) "0$it" else "$it"
    }
    second?.let {
      if (result.isNotEmpty()) {
        result += ":"
      }
      result += if (it < 10) "0$it" else "$it"
    }

    return result
  }

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

  fun getPercentageString(value: Float): String =
    "${value.times(100).toInt()}%"

  fun getTimerRestTime(time: Int): StringProvider {
    val days = time.days
    return if (days > 0) {
      { it.resources.getQuantityString(R.plurals.day_pattern, days, days) + " ⏱️" }
    } else {
      { getTimeString(time.hours, time.minutesInHour, time.secondsInMinute) }
    }
  }

  private fun getHumidityString(rawValue: Double?): String {
    return when {
      !isHumidityDefined(rawValue) ->
        String.format("%s%s", NO_VALUE_TEXT, '%')

      else -> String.format("%.1f%s", rawValue, '%')
    }
  }

  private fun isHumidityDefined(rawValue: Double?): Boolean {
    return rawValue != null && rawValue > 0
  }

  private fun isCelsius(): Boolean = preferences.temperatureUnit == TemperatureUnit.CELSIUS

  private fun getUnitString(): String {
    return if (preferences.temperatureUnit == TemperatureUnit.FAHRENHEIT) {
      "\u00B0F"
    } else {
      "\u00B0C"
    }
  }

  private fun toFahrenheit(celsiusValue: Double): Double {
    return 9.0 / 5.0 * celsiusValue + 32.0
  }

  companion object {
    const val NO_VALUE_TEXT = "---"

    /**
     * Special magic constant used to represent temperature value representing
     * that temperature data is not available. This should be done differently
     * at some point in future.
     */
    private const val TEMPERATURE_NA_VALUE = ThermometerValueProvider.UNKNOWN_VALUE
  }
}
