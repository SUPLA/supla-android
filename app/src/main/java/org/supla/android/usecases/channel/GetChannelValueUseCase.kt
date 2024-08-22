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

import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.usecases.channel.valueprovider.DepthSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.DistanceSensorValueProvider
import org.supla.android.usecases.channel.valueprovider.ElectricityMeterValueProvider
import org.supla.android.usecases.channel.valueprovider.GpmValueProvider
import org.supla.android.usecases.channel.valueprovider.HumidityAndTemperatureValueProvider
import org.supla.android.usecases.channel.valueprovider.ThermometerValueProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelValueUseCase @Inject constructor(
  depthSensorValueProvider: DepthSensorValueProvider,
  gpmValueProvider: GpmValueProvider,
  humidityAndTemperatureValueProvider: HumidityAndTemperatureValueProvider,
  thermometerValueProvider: ThermometerValueProvider,
  distanceSensorValueProvider: DistanceSensorValueProvider,
  electricityMeterValueProvider: ElectricityMeterValueProvider
) {

  private val providers = listOf(
    depthSensorValueProvider,
    gpmValueProvider,
    humidityAndTemperatureValueProvider,
    thermometerValueProvider,
    distanceSensorValueProvider,
    electricityMeterValueProvider
  )

  @Suppress("UNCHECKED_CAST")
  operator fun <T> invoke(channel: ChannelDataEntity, valueType: ValueType = ValueType.FIRST): T {
    providers.forEach {
      if (it.handle(channel.function)) {
        return it.value(channel, valueType) as T
      }
    }

    throw IllegalStateException("No value provider for channel function `${channel.function}`")
  }
}

interface ChannelValueProvider {
  fun handle(function: Int): Boolean

  fun value(channelData: ChannelDataEntity, valueType: ValueType): Any
}
