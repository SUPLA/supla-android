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

import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.usecases.channel.stringvalueprovider.ContainerValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.DepthSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.DistanceSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ElectricityMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.GpmValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HeatpolThermostatValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityAndTemperatureValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.HumidityValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ImpulseCounterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.NoValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.PressureSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.RainSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.SwitchWithMeterValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.ThermometerValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.WeightSensorValueStringProvider
import org.supla.android.usecases.channel.stringvalueprovider.WindSensorValueStringProvider
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT
import timber.log.Timber
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
  switchWithMeterValueStringProvider: SwitchWithMeterValueStringProvider,
  impulseCounterValueStringProvider: ImpulseCounterValueStringProvider,
  pressureSensorValueStringProvider: PressureSensorValueStringProvider,
  rainSensorValueStringProvider: RainSensorValueStringProvider,
  humidityValueStringProvider: HumidityValueStringProvider,
  containerValueStringProvider: ContainerValueStringProvider,
  weightSensorValueStringProvider: WeightSensorValueStringProvider,
  windSensorValueStringProvider: WindSensorValueStringProvider,
  heatpolThermostatValueStringProvider: HeatpolThermostatValueStringProvider
) {

  private val providers = listOf(
    thermometerValueProvider,
    humidityAndTemperatureValueProvider,
    depthSensorValueProvider,
    generalPurposeMeasurementValueProvider,
    distanceSensorValueStringProvider,
    electricityMeterValueStringProvider,
    switchWithMeterValueStringProvider,
    impulseCounterValueStringProvider,
    pressureSensorValueStringProvider,
    rainSensorValueStringProvider,
    humidityValueStringProvider,
    containerValueStringProvider,
    weightSensorValueStringProvider,
    windSensorValueStringProvider,
    heatpolThermostatValueStringProvider,
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
    NoValueStringProvider(SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER),
    NoValueStringProvider(SuplaFunction.DIMMER),
    NoValueStringProvider(SuplaFunction.RGB_LIGHTING),
    NoValueStringProvider(SuplaFunction.DIMMER_AND_RGB_LIGHTING),
    NoValueStringProvider(SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK),
    NoValueStringProvider(SuplaFunction.CONTROLLING_THE_DOOR_LOCK),
    NoValueStringProvider(SuplaFunction.CONTROLLING_THE_GATE),
    NoValueStringProvider(SuplaFunction.CONTROLLING_THE_GARAGE_DOOR),
    NoValueStringProvider(SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER),
    NoValueStringProvider(SuplaFunction.VALVE_OPEN_CLOSE),
    NoValueStringProvider(SuplaFunction.NO_LIQUID_SENSOR),
    NoValueStringProvider(SuplaFunction.FLOOD_SENSOR),
    NoValueStringProvider(SuplaFunction.HOTEL_CARD_SENSOR)
  )

  operator fun invoke(channel: ChannelWithChildren, valueType: ValueType = ValueType.FIRST, withUnit: Boolean = true): String {
    return valueOrNull(channel, valueType, withUnit) ?: NO_VALUE_TEXT
  }

  fun valueOrNull(channel: ChannelWithChildren, valueType: ValueType = ValueType.FIRST, withUnit: Boolean = true): String? {
    providers.firstOrNull { it.handle(channel) }?.let {
      if (channel.channel.channelValueEntity.status.offline && it !is NoValueStringProvider) {
        return NO_VALUE_TEXT
      }

      return it.value(channel, valueType, withUnit)
    }

    Timber.e("No value formatter for channel function `${channel.channel.function}`")
    return null
  }
}

enum class ValueType {
  FIRST, SECOND
}

interface ChannelValueStringProvider {
  fun handle(channelWithChildren: ChannelWithChildren): Boolean
  fun value(channelWithChildren: ChannelWithChildren, valueType: ValueType, withUnit: Boolean = true): String?
}
