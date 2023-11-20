package org.supla.android.usecases.channel
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

import android.util.Log
import org.supla.android.data.ValuesFormatter
import org.supla.android.db.Channel
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelValueUseCase @Inject constructor(
  private val valuesFormatter: ValuesFormatter
) {

  operator fun invoke(channel: Channel, valueType: ValueType = ValueType.FIRST): String {
    if (channel.value.onLine.not()) {
      return ValuesFormatter.NO_VALUE_TEXT
    }

    return when (channel.func) {
      SUPLA_CHANNELFNC_THERMOMETER ->
        valuesFormatter.getTemperatureString(channel.value.getTemp(channel.func))

      SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
        if (valueType == ValueType.FIRST) {
          valuesFormatter.getTemperatureString(channel.value.getTemp(channel.func))
        } else {
          valuesFormatter.getHumidityString(channel.value.humidity)
        }

      else -> {
        Log.w(TAG, "Trying to get value of unsupported channel function `${channel.func}`")
        ValuesFormatter.NO_VALUE_TEXT
      }
    }
  }
}

enum class ValueType {
  FIRST, SECOND
}
