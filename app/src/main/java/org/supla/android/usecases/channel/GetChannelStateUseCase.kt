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
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.hvac.ThermostatSubfunction
import org.supla.android.db.Channel
import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelGroup
import org.supla.android.db.ChannelValue
import org.supla.android.usecases.group.GetGroupActivePercentageUseCase
import org.supla.core.shared.data.model.function.container.ContainerValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.general.suplaFunction
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
    if (channelDataBase is ChannelWithChildren) {
      return getChannelState(channelDataBase.function, ChannelValueEntityStateWrapper(channelDataBase.channel.channelValueEntity))
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

  private fun getChannelState(function: SuplaFunction, value: ValueStateWrapper): ChannelState {
    if (value.online.not()) {
      return getOfflineState(function, value)
    }

    return when (function) {
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK -> getOpenClose(value.subValueHi)

      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.CURTAIN,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.ROLLER_GARAGE_DOOR ->
        if (value.shadingSystemClosed) ChannelState(ChannelState.Value.CLOSED) else ChannelState(ChannelState.Value.OPEN)

      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN ->
        if (value.shadingSystemReversedClosed) ChannelState(ChannelState.Value.CLOSED) else ChannelState(ChannelState.Value.OPEN)

      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.OPEN_SENSOR_DOOR,
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaFunction.OPENING_SENSOR_WINDOW,
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE -> getOpenClose(value.isClosed)

      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.MAIL_SENSOR,
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      SuplaFunction.HOTEL_CARD_SENSOR,
      SuplaFunction.ALARM_ARMAMENT_SENSOR,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.FLOOD_SENSOR -> getOnOff(value.isClosed)

      SuplaFunction.DIMMER -> getOnOff(value.brightness > 0)
      SuplaFunction.RGB_LIGHTING -> getOnOff(value.colorBrightness > 0)

      SuplaFunction.DIMMER_AND_RGB_LIGHTING -> {
        val first = if (value.brightness > 0) ChannelState.Value.ON else ChannelState.Value.OFF
        val second = if (value.colorBrightness > 0) ChannelState.Value.ON else ChannelState.Value.OFF

        ChannelState(ChannelState.Value.COMPLEX, listOf(first, second))
      }

      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.DIGIGLASS_VERTICAL ->
        if (value.transparent) {
          ChannelState(ChannelState.Value.TRANSPARENT)
        } else {
          ChannelState(ChannelState.Value.OPAQUE)
        }

      SuplaFunction.HVAC_THERMOSTAT -> {
        when (value.thermostatSubfunction) {
          ThermostatSubfunction.HEAT -> ChannelState(ChannelState.Value.HEAT)
          else -> ChannelState(ChannelState.Value.COOL)
        }
      }

      SuplaFunction.CONTAINER,
      SuplaFunction.SEPTIC_TANK,
      SuplaFunction.WATER_TANK ->
        when {
          value.containerValue.level > 80 -> ChannelState(ChannelState.Value.FULL)
          value.containerValue.level > 20 -> ChannelState(ChannelState.Value.HALF)
          else -> ChannelState(ChannelState.Value.EMPTY)
        }

      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
      SuplaFunction.DEPTH_SENSOR,
      SuplaFunction.DISTANCE_SENSOR,
      SuplaFunction.WIND_SENSOR,
      SuplaFunction.PRESSURE_SENSOR,
      SuplaFunction.RAIN_SENSOR,
      SuplaFunction.WEIGHT_SENSOR,
      SuplaFunction.WEATHER_STATION,
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaFunction.GENERAL_PURPOSE_METER -> ChannelState(ChannelState.Value.NOT_USED)
    }
  }

  private fun getOfflineState(function: SuplaFunction, value: ValueStateWrapper): ChannelState {
    return when (function) {
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.OPEN_SENSOR_GATEWAY,
      SuplaFunction.OPEN_SENSOR_GATE,
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
      SuplaFunction.OPEN_SENSOR_DOOR,
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
      SuplaFunction.OPENING_SENSOR_WINDOW,
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE,
      SuplaFunction.CURTAIN,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.ROLLER_GARAGE_DOOR -> ChannelState(ChannelState.Value.OPEN)

      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN -> ChannelState(ChannelState.Value.CLOSED)

      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.NO_LIQUID_SENSOR,
      SuplaFunction.MAIL_SENSOR,
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
      SuplaFunction.HOTEL_CARD_SENSOR,
      SuplaFunction.ALARM_ARMAMENT_SENSOR,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.DIMMER,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.PUMP_SWITCH,
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
      SuplaFunction.CONTAINER_LEVEL_SENSOR,
      SuplaFunction.FLOOD_SENSOR -> ChannelState(ChannelState.Value.OFF)

      SuplaFunction.DIMMER_AND_RGB_LIGHTING ->
        ChannelState(ChannelState.Value.COMPLEX, listOf(ChannelState.Value.OFF, ChannelState.Value.OFF))

      SuplaFunction.DIGIGLASS_HORIZONTAL,
      SuplaFunction.DIGIGLASS_VERTICAL ->
        ChannelState(ChannelState.Value.OPAQUE)

      SuplaFunction.HVAC_THERMOSTAT ->
        when (value.thermostatSubfunction) {
          ThermostatSubfunction.HEAT -> ChannelState(ChannelState.Value.HEAT)
          else -> ChannelState(ChannelState.Value.COOL)
        }

      SuplaFunction.CONTAINER,
      SuplaFunction.SEPTIC_TANK,
      SuplaFunction.WATER_TANK -> ChannelState(ChannelState.Value.EMPTY)

      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.THERMOMETER,
      SuplaFunction.HUMIDITY,
      SuplaFunction.HUMIDITY_AND_TEMPERATURE,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
      SuplaFunction.DEPTH_SENSOR,
      SuplaFunction.DISTANCE_SENSOR,
      SuplaFunction.WIND_SENSOR,
      SuplaFunction.PRESSURE_SENSOR,
      SuplaFunction.RAIN_SENSOR,
      SuplaFunction.WEIGHT_SENSOR,
      SuplaFunction.WEATHER_STATION,
      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER,
      SuplaFunction.IC_GAS_METER,
      SuplaFunction.IC_WATER_METER,
      SuplaFunction.IC_HEAT_METER,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
      SuplaFunction.GENERAL_PURPOSE_METER -> ChannelState(ChannelState.Value.NOT_USED)
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
  val containerValue: ContainerValue
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
  override val containerValue: ContainerValue
    get() = channelValueEntity.asContainerValue()
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
  override val containerValue: ContainerValue
    get() = ContainerValue(group.online > 0, emptyList(), 0)

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
  override val containerValue: ContainerValue
    get() = value?.asContainerValue() ?: ContainerValue(value?.onLine ?: false, emptyList(), 0)
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
  override val containerValue: ContainerValue
    get() = ContainerValue(group.onLine, emptyList(), 0)

  private fun getActivePercentage(valueIndex: Int = 0) =
    getGroupActivePercentageUseCase(group, valueIndex)
}
