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

import org.supla.android.Trace
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.extensions.TAG
import org.supla.android.usecases.channel.stringvalueprovider.DepthSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.GpmValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityAndTemperatureValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ThermometerValueStringProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelValueStringUseCase @Inject constructor(
  thermometerValueProvider: ThermometerValueStringProvider,
  humidityAndTemperatureValueProvider: HumidityAndTemperatureValueStringProvider,
  depthSensorValueProvider: DepthSensorValueStringProvider,
  generalPurposeMeasurementValueProvider: GpmValueStringProvider,
) {

  private val providers = listOf(
    thermometerValueProvider,
    humidityAndTemperatureValueProvider,
    depthSensorValueProvider,
    generalPurposeMeasurementValueProvider
  )

  operator fun invoke(channel: ChannelDataEntity, valueType: ValueType = ValueType.FIRST, withUnit: Boolean = true): String {
    if (channel.channelValueEntity.online.not()) {
      return ValuesFormatter.NO_VALUE_TEXT
    }

    providers.forEach {
      if (it.handle(channel.function)) {
        return it.value(channel, valueType, withUnit)
      }
    }

    Trace.e(TAG, "No value formatter for channel function `${channel.function}`")
    return ValuesFormatter.NO_VALUE_TEXT
  }
}

enum class ValueType {
  FIRST, SECOND
}

interface ChannelValueStringProvider {
  fun handle(function: Int): Boolean

  fun value(channelData: ChannelDataEntity, valueType: ValueType, withUnit: Boolean = true): String
}
