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

import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.data.source.remote.channel.suplaFunction
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.db.ChannelValue
import org.supla.android.usecases.group.GetGroupActivePercentageUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetChannelStateUseCase @Inject constructor(
  private val getGroupActivePercentageUseCase: GetGroupActivePercentageUseCase
) {

  operator fun invoke(channelDataBase: ChannelDataBase): ChannelState {
    if (channelDataBase is ChannelDataEntity) {
      return getChannelState(channelDataBase.function, ChannelValueEntityStateWrapper(channelDataBase.channelValueEntity))
    }
    if (channelDataBase is ChannelGroupDataEntity) {
      val wrapper = ChannelGroupEntityStateWrapper(channelDataBase.channelGroupEntity, getGroupActivePercentageUseCase)
      return getChannelState(channelDataBase.function, wrapper)
    }

    throw IllegalArgumentException("Channel data base is extended by unknown class!")
  }

  operator fun invoke(channelBase: ChannelBase): ChannelState {
    if (channelBase is Channel) {
      return getChannelState(channelBase.func.suplaFunction(), ChannelValueStateWrapper(channelBase.value))
    }
    if (channelBase is ChannelGroup) {
      return getChannelState(channelBase.func.suplaFunction(), ChannelGroupStateWrapper(channelBase, getGroupActivePercentageUseCase))
    }

    throw IllegalArgumentException("Channel base is extended by unknown class!")
  }

  private fun getChannelState(function: SuplaChannelFunction, value: ValueStateWrapper): ChannelState {
    if (value.online.not()) {
      return getOfflineState(function, value)
    }

    return when (function) {
      SuplaChannelFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaChannelFunction.CONTROLLING_THE_GATE,
      SuplaChannelFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaChannelFunction.CONTROLLING_THE_DOOR_LOCK -> getOpenClose(value.subValueHi)

      SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaChannelFunction.CURTAIN,
      SuplaChannelFunction.VERTICAL_BLIND,
      SuplaChannelFunction.ROLLER_GARAGE_DOOR ->
        if (value.shadingSystemClosed) ChannelState(ChannelState.Value.CLOSED) else ChannelState(ChannelState.Value.OPEN)

      SuplaChannelFunction.TERRACE_AWNING,
      SuplaChannelFunction.PROJECTOR_SCREEN ->
        if (value.shadingSystemReversedClosed) ChannelState(ChannelState.Value.CLOSED) else ChannelState(ChannelState.Value.OPEN)

      SuplaChannelFunction.OPEN_SENSOR_GATEWAY,
      SuplaChannelFunction.OPEN_SENSOR_GATE,
      SuplaChannelFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaChannelFunction.OPEN_SENSOR_DOOR,
      SuplaChannelFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaChannelFunction.OPENING_SENSOR_WINDOW,
      SuplaChannelFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaChannelFunction.VALVE_OPEN_CLOSE,
      SuplaChannelFunction.VALVE_PERCENTAGE -> getOpenClose(value.isClosed)

      SuplaChannelFunction.POWER_SWITCH,
      SuplaChannelFunction.STAIRCASE_TIMER,
      SuplaChannelFunction.NO_LIQUID_SENSOR,
      SuplaChannelFunction.MAIL_SENSOR,
      SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      SuplaChannelFunction.HOTEL_CARD_SENSOR,
      SuplaChannelFunction.ALARM_ARMAMENT_SENSOR,
      SuplaChannelFunction.LIGHTSWITCH,
      SuplaChannelFunction.PUMP_SWITCH,
      SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH -> getOnOff(value.isClosed)

      SuplaChannelFunction.DIMMER -> getOnOff(value.brightness > 0)
      SuplaChannelFunction.RGB_LIGHTING -> getOnOff(value.colorBrightness > 0)

      SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING -> {
        val first = if (value.brightness > 0) ChannelState.Value.ON else ChannelState.Value.OFF
        val second = if (value.colorBrightness > 0) ChannelState.Value.ON else ChannelState.Value.OFF

        ChannelState(ChannelState.Value.COMPLEX, listOf(first, second))
      }

      SuplaChannelFunction.DIGIGLASS_HORIZONTAL,
      SuplaChannelFunction.DIGIGLASS_VERTICAL ->
        if (value.transparent) {
          ChannelState(ChannelState.Value.TRANSPARENT)
        } else {
          ChannelState(ChannelState.Value.OPAQUE)
        }

      SuplaChannelFunction.HVAC_THERMOSTAT -> {
        when (value.thermostatSubfunction) {
          ThermostatSubfunction.HEAT -> ChannelState(ChannelState.Value.HEAT)
          else -> ChannelState(ChannelState.Value.COOL)
        }
      }

      SuplaChannelFunction.UNKNOWN,
      SuplaChannelFunction.NONE,
      SuplaChannelFunction.THERMOMETER,
      SuplaChannelFunction.HUMIDITY,
      SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaChannelFunction.RING,
      SuplaChannelFunction.ALARM,
      SuplaChannelFunction.NOTIFICATION,
      SuplaChannelFunction.DEPTH_SENSOR,
      SuplaChannelFunction.DISTANCE_SENSOR,
      SuplaChannelFunction.WIND_SENSOR,
      SuplaChannelFunction.PRESSURE_SENSOR,
      SuplaChannelFunction.RAIN_SENSOR,
      SuplaChannelFunction.WEIGHT_SENSOR,
      SuplaChannelFunction.WEATHER_STATION,
      SuplaChannelFunction.ELECTRICITY_METER,
      SuplaChannelFunction.IC_ELECTRICITY_METER,
      SuplaChannelFunction.IC_GAS_METER,
      SuplaChannelFunction.IC_WATER_METER,
      SuplaChannelFunction.IC_HEAT_METER,
      SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaChannelFunction.GENERAL_PURPOSE_METER -> ChannelState(ChannelState.Value.NOT_USED)
    }
  }

  private fun getOfflineState(function: SuplaChannelFunction, value: ValueStateWrapper): ChannelState {
    return when (function) {
      SuplaChannelFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaChannelFunction.CONTROLLING_THE_GATE,
      SuplaChannelFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaChannelFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaChannelFunction.OPEN_SENSOR_GATEWAY,
      SuplaChannelFunction.OPEN_SENSOR_GATE,
      SuplaChannelFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaChannelFunction.OPEN_SENSOR_DOOR,
      SuplaChannelFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaChannelFunction.OPENING_SENSOR_WINDOW,
      SuplaChannelFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaChannelFunction.VALVE_OPEN_CLOSE,
      SuplaChannelFunction.VALVE_PERCENTAGE,
      SuplaChannelFunction.CURTAIN,
      SuplaChannelFunction.VERTICAL_BLIND,
      SuplaChannelFunction.ROLLER_GARAGE_DOOR -> ChannelState(ChannelState.Value.OPEN)

      SuplaChannelFunction.TERRACE_AWNING,
      SuplaChannelFunction.PROJECTOR_SCREEN -> ChannelState(ChannelState.Value.CLOSED)

      SuplaChannelFunction.POWER_SWITCH,
      SuplaChannelFunction.STAIRCASE_TIMER,
      SuplaChannelFunction.NO_LIQUID_SENSOR,
      SuplaChannelFunction.MAIL_SENSOR,
      SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      SuplaChannelFunction.HOTEL_CARD_SENSOR,
      SuplaChannelFunction.ALARM_ARMAMENT_SENSOR,
      SuplaChannelFunction.LIGHTSWITCH,
      SuplaChannelFunction.DIMMER,
      SuplaChannelFunction.RGB_LIGHTING,
      SuplaChannelFunction.PUMP_SWITCH,
      SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH -> ChannelState(ChannelState.Value.OFF)

      SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING ->
        ChannelState(ChannelState.Value.COMPLEX, listOf(ChannelState.Value.OFF, ChannelState.Value.OFF))

      SuplaChannelFunction.DIGIGLASS_HORIZONTAL,
      SuplaChannelFunction.DIGIGLASS_VERTICAL ->
        ChannelState(ChannelState.Value.OPAQUE)

      SuplaChannelFunction.HVAC_THERMOSTAT ->
        when (value.thermostatSubfunction) {
          ThermostatSubfunction.HEAT -> ChannelState(ChannelState.Value.HEAT)
          else -> ChannelState(ChannelState.Value.COOL)
        }

      SuplaChannelFunction.UNKNOWN,
      SuplaChannelFunction.NONE,
      SuplaChannelFunction.THERMOMETER,
      SuplaChannelFunction.HUMIDITY,
      SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaChannelFunction.RING,
      SuplaChannelFunction.ALARM,
      SuplaChannelFunction.NOTIFICATION,
      SuplaChannelFunction.DEPTH_SENSOR,
      SuplaChannelFunction.DISTANCE_SENSOR,
      SuplaChannelFunction.WIND_SENSOR,
      SuplaChannelFunction.PRESSURE_SENSOR,
      SuplaChannelFunction.RAIN_SENSOR,
      SuplaChannelFunction.WEIGHT_SENSOR,
      SuplaChannelFunction.WEATHER_STATION,
      SuplaChannelFunction.ELECTRICITY_METER,
      SuplaChannelFunction.IC_ELECTRICITY_METER,
      SuplaChannelFunction.IC_GAS_METER,
      SuplaChannelFunction.IC_WATER_METER,
      SuplaChannelFunction.IC_HEAT_METER,
      SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaChannelFunction.GENERAL_PURPOSE_METER -> ChannelState(ChannelState.Value.NOT_USED)
    }
  }

  private fun getOpenClose(value: Int) =
    if ((value and 0x2) == 0x2 && (value and 0x1) == 0) {
      ChannelState(ChannelState.Value.PARTIALLY_OPENED)
    } else if (value > 0) {
      ChannelState(ChannelState.Value.CLOSED)
    } else {
      ChannelState(ChannelState.Value.OPEN)
    }

  private fun getOpenClose(isClosed: Boolean) =
    if (isClosed) {
      ChannelState(ChannelState.Value.CLOSED)
    } else {
      ChannelState(ChannelState.Value.OPEN)
    }

  private fun getOnOff(isOn: Boolean) =
    if (isOn) {
      ChannelState(ChannelState.Value.ON)
    } else {
      ChannelState(ChannelState.Value.OFF)
    }
}

interface ValueStateWrapper {

  val online: Boolean
  val subValueHi: Int
  val isClosed: Boolean
  val brightness: Int
  val colorBrightness: Int
  val transparent: Boolean
  val thermostatSubfunction: ThermostatSubfunction?
  val shadingSystemClosed: Boolean
  val shadingSystemReversedClosed: Boolean
}

private class ChannelValueEntityStateWrapper(private val channelValueEntity: ChannelValueEntity) : ValueStateWrapper {
  override val online: Boolean
    get() = channelValueEntity.online
  override val subValueHi: Int
    get() = channelValueEntity.getSubValueHi()
  override val isClosed: Boolean
    get() = channelValueEntity.isClosed()
  override val brightness: Int
    get() = channelValueEntity.asBrightness()
  override val colorBrightness: Int
    get() = channelValueEntity.asBrightnessColor()
  override val transparent: Boolean
    get() = channelValueEntity.asDigiglassValue().isAnySectionTransparent
  override val thermostatSubfunction: ThermostatSubfunction
    get() = channelValueEntity.asThermostatValue().subfunction
  override val shadingSystemClosed: Boolean
    get() {
      val percentage = channelValueEntity.asRollerShutterValue().position
      val subValueHi = channelValueEntity.getSubValueHi()
      return (subValueHi > 0 && percentage < 100) || percentage >= 100
    }
  override val shadingSystemReversedClosed: Boolean
    get() {
      val percentage = channelValueEntity.asRollerShutterValue().position
      return percentage < 100
    }
}

private class ChannelGroupEntityStateWrapper(
  private val group: ChannelGroupEntity,
  private val getGroupActivePercentageUseCase: GetGroupActivePercentageUseCase
) : ValueStateWrapper {
  override val online: Boolean
    get() = group.online > 0
  override val subValueHi: Int
    get() = if (getActivePercentage() >= 100) 1 else 0
  override val isClosed: Boolean
    get() = getActivePercentage() >= 100
  override val brightness: Int
    get() = if (getActivePercentage(2) >= 100) 1 else 0
  override val colorBrightness: Int
    get() = if (getActivePercentage(1) >= 100) 1 else 0
  override val transparent: Boolean
    get() = false
  override val thermostatSubfunction: ThermostatSubfunction?
    get() = null
  override val shadingSystemClosed: Boolean
    get() = getActivePercentage() >= 100
  override val shadingSystemReversedClosed: Boolean
    get() = getActivePercentage() < 100

  private fun getActivePercentage(valueIndex: Int = 0) =
    getGroupActivePercentageUseCase(group, valueIndex)
}

private class ChannelValueStateWrapper(private val value: ChannelValue?) : ValueStateWrapper {
  override val online: Boolean
    get() = value?.onLine ?: false
  override val subValueHi: Int
    get() = value?.subValueHi?.toInt() ?: 0
  override val isClosed: Boolean
    get() = value?.isClosed ?: false
  override val brightness: Int
    get() = value?.brightness?.toInt() ?: 0
  override val colorBrightness: Int
    get() = value?.colorBrightness?.toInt() ?: 0
  override val transparent: Boolean
    get() = value?.digiglassValue?.isAnySectionTransparent ?: false
  override val thermostatSubfunction: ThermostatSubfunction?
    get() = value?.asThermostatValue()?.subfunction
  override val shadingSystemClosed: Boolean
    get() {
      val percentage = value?.rollerShutterValue?.closingPercentage ?: 0
      val subValueHi = value?.subValueHi ?: 0
      return (subValueHi > 0 && percentage < 100) || percentage >= 100
    }
  override val shadingSystemReversedClosed: Boolean
    get() {
      val percentage = value?.rollerShutterValue?.closingPercentage ?: 0
      return percentage < 100
    }
}

private class ChannelGroupStateWrapper(
  private val group: ChannelGroup,
  private val getGroupActivePercentageUseCase: GetGroupActivePercentageUseCase
) : ValueStateWrapper {
  override val online: Boolean
    get() = group.onLine
  override val subValueHi: Int
    get() = if (getActivePercentage() >= 100) 1 else 0
  override val isClosed: Boolean
    get() = getActivePercentage() >= 100
  override val brightness: Int
    get() = if (getActivePercentage(2) >= 100) 1 else 0
  override val colorBrightness: Int
    get() = if (getActivePercentage(1) >= 100) 1 else 0
  override val transparent: Boolean
    get() = false
  override val thermostatSubfunction: ThermostatSubfunction?
    get() = null
  override val shadingSystemClosed: Boolean
    get() = getActivePercentage() >= 100
  override val shadingSystemReversedClosed: Boolean
    get() = getActivePercentage() <= 0

  private fun getActivePercentage(valueIndex: Int = 0) =
    getGroupActivePercentageUseCase(group, valueIndex)
}
