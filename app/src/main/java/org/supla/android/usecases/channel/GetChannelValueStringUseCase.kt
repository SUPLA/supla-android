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
import org.supla.android.usecases.channel.stringvalueprovider.DistanceSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ElectricityMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.GpmValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityAndTemperatureValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ImpulseCounterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.NoValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.PressureSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.RainSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.SwitchWithElectricityMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ThermometerValueStringProvider
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelValueStringUseCase @Inject constructor(
  thermometerValueProvider: ThermometerValueStringProvider,
  humidityAndTemperatureValueProvider: HumidityAndTemperatureValueStringProvider,
  depthSensorValueProvider: DepthSensorValueStringProvider,
  generalPurposeMeasurementValueProvider: GpmValueStringProvider,
  distanceSensorValueStringProvider: DistanceSensorValueStringProvider,
  electricityMeterValueStringProvider: ElectricityMeterValueStringProvider,
  switchWithElectricityMeterValueStringProvider: SwitchWithElectricityMeterValueStringProvider,
  impulseCounterValueStringProvider: ImpulseCounterValueStringProvider,
  pressureSensorValueStringProvider: PressureSensorValueStringProvider,
  rainSensorValueStringProvider: RainSensorValueStringProvider,
  humidityValueStringProvider: HumidityValueStringProvider
) {

  private val providers = listOf(
    thermometerValueProvider,
    humidityAndTemperatureValueProvider,
    depthSensorValueProvider,
    generalPurposeMeasurementValueProvider,
    distanceSensorValueStringProvider,
    electricityMeterValueStringProvider,
    switchWithElectricityMeterValueStringProvider,
    impulseCounterValueStringProvider,
    pressureSensorValueStringProvider,
    rainSensorValueStringProvider,
    humidityValueStringProvider,
    NoValueStringProvider(SuplaFunction.STAIRCASE_TIMER),
    NoValueStringProvider(SuplaFunction.POWER_SWITCH),
    NoValueStringProvider(SuplaFunction.LIGHTSWITCH),
    NoValueStringProvider(SuplaFunction.MAIL_SENSOR),
    NoValueStringProvider(SuplaFunction.OPENING_SENSOR_WINDOW),
    NoValueStringProvider(SuplaFunction.OPEN_SENSOR_DOOR),
    NoValueStringProvider(SuplaFunction.OPEN_SENSOR_GATE),
    NoValueStringProvider(SuplaFunction.OPEN_SENSOR_GATEWAY),
    NoValueStringProvider(SuplaFunction.OPEN_SENSOR_GARAGE_DOOR),
    NoValueStringProvider(SuplaFunction.OPEN_SENSOR_ROOF_WINDOW),
    NoValueStringProvider(SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER)
  )

  operator fun invoke(channel: ChannelDataEntity, valueType: ValueType = ValueType.FIRST, withUnit: Boolean = true): String {
    return valueOrNull(channel, valueType, withUnit) ?: ValuesFormatter.NO_VALUE_TEXT
  }

  fun valueOrNull(channel: ChannelDataEntity, valueType: ValueType = ValueType.FIRST, withUnit: Boolean = true): String? {
    providers.firstOrNull { it.handle(channel) }?.let {
      if (channel.channelValueEntity.online.not()) {
        return ValuesFormatter.NO_VALUE_TEXT
      }

      return it.value(channel, valueType, withUnit)
    }

    Trace.e(TAG, "No value formatter for channel function `${channel.function}`")
    return null
  }
}

enum class ValueType {
  FIRST, SECOND
}

interface ChannelValueStringProvider {
  fun handle(channelData: ChannelDataEntity): Boolean
  fun value(channelData: ChannelDataEntity, valueType: ValueType, withUnit: Boolean = true): String?
}
