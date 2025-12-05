package org.supla.android.lib
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

import kotlinx.serialization.Serializable
import org.supla.android.tools.UsedFromNativeCode

@Serializable
class SuplaChannelThermostatValue @UsedFromNativeCode internal constructor() {
  private var measuredTemperature: Array<Double> = arrayOf(-273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0)
  private var presetTemperature: Array<Double> = arrayOf(-273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0, -273.0)
  private var flags: Array<Int?> = arrayOfNulls(8)
  private var values: Array<Int?> = arrayOfNulls(8)

  var schedule: Schedule? = null
  var time: Time? = null

  fun getMeasuredTemperature(idx: Int): Double? {
    return if (idx >= 0 && idx < measuredTemperature.size) {
      measuredTemperature[idx]
    } else {
      null
    }
  }

  @UsedFromNativeCode
  fun setMeasuredTemperature(idx: Int, temperature: Double): Boolean {
    if (idx >= 0 && idx < 10) {
      measuredTemperature[idx] = temperature
      return true
    }
    return false
  }

  fun getPresetTemperature(idx: Int): Double? {
    return if (idx >= 0 && idx < presetTemperature.size) {
      presetTemperature[idx]
    } else {
      null
    }
  }

  @UsedFromNativeCode
  fun setPresetTemperature(idx: Int, temperature: Double): Boolean {
    if (idx >= 0 && idx < 10) {
      presetTemperature[idx] = temperature
      return true
    }
    return false
  }

  fun getFlags(idx: Int): Int? {
    return if (idx >= 0 && idx < flags.size) flags[idx] else null
  }

  @UsedFromNativeCode
  fun setFlags(idx: Int, flags: Int): Boolean {
    if (idx >= 0 && idx < 8) {
      this.flags[idx] = flags
      return true
    }
    return false
  }

  fun getValues(idx: Int): Int? {
    return if (idx >= 0 && idx < values.size) values[idx] else null
  }

  @UsedFromNativeCode
  fun setValues(idx: Int, values: Int): Boolean {
    if (idx >= 0 && idx < 8) {
      this.values[idx] = values
      return true
    }
    return false
  }

  @UsedFromNativeCode
  fun setScheduleValue(day: Byte, hour: Byte, value: Byte) {
    if (this.schedule == null) {
      this.schedule = Schedule()
    }

    schedule!!.setValue(day, hour, value)
  }

  @UsedFromNativeCode
  fun setScheduleValueType(type: Byte) {
    if (this.schedule == null) {
      this.schedule = Schedule()
    }

    schedule!!.type = type
  }

  @UsedFromNativeCode
  fun setTime(second: Byte, minute: Byte, hour: Byte, dayOfWeek: Byte) {
    this.time = Time(second, minute, hour, dayOfWeek)
  }

  @Serializable
  class Schedule internal constructor() {
    var type: Byte = 0
    private val hourValue: Array<ByteArray?> = Array(7) { ByteArray(24) }

    fun setValue(day: Byte, hour: Byte, value: Byte): Boolean {
      if (day >= 0 && day < hourValue.size && hour >= 0 && hour < hourValue[day.toInt()]!!.size) {
        hourValue[day.toInt()]!![hour.toInt()] = value
        return true
      }
      return false
    }

    fun getValue(day: Byte, hour: Byte): Byte {
      return if (day >= 0 && day < hourValue.size && hour >= 0 && hour < hourValue[day.toInt()]!!.size) {
        hourValue[day.toInt()]!![hour.toInt()]
      } else {
        0
      }
    }

    companion object {
      const val TYPE_TEMPERATURE: Byte = 0
      const val TYPE_PROGRAM: Byte = 1
    }
  }

  @Serializable
  class Time(var second: Byte, var minute: Byte, var hour: Byte, var dayOfWeek: Byte)
}
