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
import org.supla.android.data.source.runtime.appsettings.TemperatureUnit
import org.supla.android.lib.singlecall.TemperatureAndHumidity
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValuesFormatter @Inject constructor(
  private val preferences: Preferences
) {

  fun isTemperatureDefined(rawValue: Double?): Boolean {
    return rawValue != null && rawValue > TEMPERATURE_NA_VALUE
  }

  fun getTemperatureString(rawValue: Float?, withUnit: Boolean = false, withDegree: Boolean = true) =
    getTemperatureString(rawValue?.toDouble(), withUnit, withDegree)

  fun getTemperatureString(rawValue: Double?, withUnit: Boolean = false, withDegree: Boolean = true): String {
    return when {
      !isTemperatureDefined(rawValue) && withUnit ->
        String.format("%s%s", NO_VALUE_TEXT, getUnitString())

      !isTemperatureDefined(rawValue) ->
        String.format("%s", NO_VALUE_TEXT)

      withUnit -> String.format(
        "%.1f%s",
        getTemperatureInConfiguredUnit(rawValue!!),
        getUnitString()
      )

      else -> String.format(
        "%.1f%s",
        getTemperatureInConfiguredUnit(rawValue!!),
        if (withDegree) getUnitString().substring(0, 1) else ""
      )
    }
  }

  fun getHumidityString(rawValue: Double?, withPercentage: Boolean = false): String {
    return if (withPercentage) {
      String.format("%.1f%%", rawValue)
    } else {
      String.format("%.1f", rawValue)
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

  @SuppressLint("SimpleDateFormat")
  fun getDateString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd.MM.yyyy")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getMonthString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd MMM")
      formatter.format(it)
    }

  @SuppressLint("SimpleDateFormat")
  fun getHourString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("HH:mm")
      formatter.format(it)
    }

  fun getPercentageString(value: Float): String =
    "${value.times(100).toInt()}%"

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
    private const val TEMPERATURE_NA_VALUE = -273
  }
}
