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
import org.supla.android.R
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.ui.StringProvider
import org.supla.android.data.source.local.calendar.Hour
import org.supla.android.di.FORMATTER_THERMOMETER
import org.supla.android.extensions.days
import org.supla.android.extensions.hours
import org.supla.android.extensions.minutesInHour
import org.supla.android.extensions.secondsInMinute
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider
import org.supla.core.shared.data.model.thermometer.TemperatureUnit
import org.supla.core.shared.usecase.channel.valueformatter.ValueFormatter
import org.supla.core.shared.usecase.channel.valueformatter.types.withUnit
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ValuesFormatter @Inject constructor(
  private val applicationPreferences: ApplicationPreferences,
  @Named(FORMATTER_THERMOMETER) private val thermometerValueFormatter: ValueFormatter
) {

  fun isTemperatureDefined(rawValue: Double?): Boolean {
    return rawValue != null && rawValue > TEMPERATURE_NA_VALUE
  }

  fun getTemperatureString(rawValue: Float?) = thermometerValueFormatter.format(rawValue)

  fun getTemperatureString(rawValue: Double?, withUnit: Boolean = false) =
    thermometerValueFormatter.format(rawValue, withUnit(withUnit))

  fun getTemperatureInConfiguredUnit(value: Double): Double {
    return if (!isTemperatureDefined(value) || isCelsius()) {
      value
    } else {
      toFahrenheit(value)
    }
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
  fun getFullDateString(date: Date?): String? =
    date?.let {
      val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
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

  private fun isCelsius(): Boolean = applicationPreferences.temperatureUnit == TemperatureUnit.CELSIUS

  private fun toFahrenheit(celsiusValue: Double): Double {
    return 9.0 / 5.0 * celsiusValue + 32.0
  }

  companion object {
    /**
     * Special magic constant used to represent temperature value representing
     * that temperature data is not available. This should be done differently
     * at some point in future.
     */
    private const val TEMPERATURE_NA_VALUE = ThermometerValueProvider.UNKNOWN_VALUE
  }
}
