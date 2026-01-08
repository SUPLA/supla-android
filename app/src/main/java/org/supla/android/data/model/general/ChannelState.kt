package org.supla.android.data.model.general

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

import org.supla.core.shared.data.model.general.SuplaFunction

sealed interface ChannelState {
  val value: Value
  val isActive: Boolean
    get() {
      return when (value) {
        Value.CLOSED, Value.ON, Value.TRANSPARENT -> true
        else -> false
      }
    }

  data class Default(
    override val value: Value
  ) : ChannelState

  data class RgbAndDimmer(
    val dimmer: Value,
    val rgb: Value
  ) : ChannelState {
    override val value: Value
      get() = when {
        dimmer == Value.ON || rgb == Value.ON -> Value.ON
        else -> Value.OFF
      }
  }

  enum class Value {
    // active states
    OPEN,
    ON,
    TRANSPARENT,

    // inactive states
    PARTIALLY_OPENED,
    CLOSED,
    OFF,
    OPAQUE,

    // thermostat
    HEAT,
    COOL,

    // fulfillment
    FULL,
    HALF,
    EMPTY,

    // others
    NOT_USED
  }

  companion object {
    fun active(function: SuplaFunction): ChannelState =
      when (function) {
        SuplaFunction.DIGIGLASS_VERTICAL,
        SuplaFunction.DIGIGLASS_HORIZONTAL -> Default(Value.TRANSPARENT)

        SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
        SuplaFunction.CONTROLLING_THE_GATE,
        SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
        SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
        SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
        SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
        SuplaFunction.TERRACE_AWNING,
        SuplaFunction.PROJECTOR_SCREEN,
        SuplaFunction.CURTAIN,
        SuplaFunction.VERTICAL_BLIND,
        SuplaFunction.ROLLER_GARAGE_DOOR,
        SuplaFunction.OPEN_SENSOR_GATEWAY,
        SuplaFunction.OPEN_SENSOR_GATE,
        SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
        SuplaFunction.OPEN_SENSOR_DOOR,
        SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
        SuplaFunction.OPENING_SENSOR_WINDOW,
        SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
        SuplaFunction.VALVE_OPEN_CLOSE,
        SuplaFunction.VALVE_PERCENTAGE -> Default(Value.CLOSED)

        SuplaFunction.POWER_SWITCH,
        SuplaFunction.STAIRCASE_TIMER,
        SuplaFunction.NO_LIQUID_SENSOR,
        SuplaFunction.MAIL_SENSOR,
        SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
        SuplaFunction.HOTEL_CARD_SENSOR,
        SuplaFunction.ALARM_ARMAMENT_SENSOR,
        SuplaFunction.LIGHTSWITCH,
        SuplaFunction.DIMMER,
        SuplaFunction.DIMMER_CCT,
        SuplaFunction.RGB_LIGHTING -> Default(Value.ON)

        SuplaFunction.DIMMER_AND_RGB_LIGHTING,
        SuplaFunction.DIMMER_CCT_AND_RGB -> RgbAndDimmer(Value.ON, Value.ON)

        SuplaFunction.CONTAINER,
        SuplaFunction.WATER_TANK,
        SuplaFunction.SEPTIC_TANK -> Default(Value.FULL)

        SuplaFunction.UNKNOWN,
        SuplaFunction.NONE,
        SuplaFunction.THERMOMETER,
        SuplaFunction.HUMIDITY,
        SuplaFunction.HUMIDITY_AND_TEMPERATURE,
        SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
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
        SuplaFunction.HVAC_THERMOSTAT,
        SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
        SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
        SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
        SuplaFunction.GENERAL_PURPOSE_METER,
        SuplaFunction.PUMP_SWITCH,
        SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
        SuplaFunction.CONTAINER_LEVEL_SENSOR,
        SuplaFunction.FLOOD_SENSOR,
        SuplaFunction.MOTION_SENSOR,
        SuplaFunction.BINARY_SENSOR -> Default(Value.NOT_USED)
      }

    fun inactive(function: SuplaFunction): ChannelState =
      when (function) {
        SuplaFunction.DIGIGLASS_VERTICAL,
        SuplaFunction.DIGIGLASS_HORIZONTAL -> Default(Value.OPAQUE)

        SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
        SuplaFunction.CONTROLLING_THE_GATE,
        SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
        SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
        SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
        SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
        SuplaFunction.TERRACE_AWNING,
        SuplaFunction.PROJECTOR_SCREEN,
        SuplaFunction.CURTAIN,
        SuplaFunction.VERTICAL_BLIND,
        SuplaFunction.ROLLER_GARAGE_DOOR,
        SuplaFunction.OPEN_SENSOR_GATEWAY,
        SuplaFunction.OPEN_SENSOR_GATE,
        SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
        SuplaFunction.OPEN_SENSOR_DOOR,
        SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
        SuplaFunction.OPENING_SENSOR_WINDOW,
        SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
        SuplaFunction.VALVE_OPEN_CLOSE,
        SuplaFunction.VALVE_PERCENTAGE -> Default(Value.OPEN)

        SuplaFunction.POWER_SWITCH,
        SuplaFunction.STAIRCASE_TIMER,
        SuplaFunction.NO_LIQUID_SENSOR,
        SuplaFunction.MAIL_SENSOR,
        SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
        SuplaFunction.HOTEL_CARD_SENSOR,
        SuplaFunction.ALARM_ARMAMENT_SENSOR,
        SuplaFunction.LIGHTSWITCH,
        SuplaFunction.DIMMER,
        SuplaFunction.DIMMER_CCT,
        SuplaFunction.RGB_LIGHTING -> Default(Value.OFF)

        SuplaFunction.DIMMER_AND_RGB_LIGHTING,
        SuplaFunction.DIMMER_CCT_AND_RGB -> RgbAndDimmer(Value.OFF, Value.OFF)

        SuplaFunction.CONTAINER,
        SuplaFunction.WATER_TANK,
        SuplaFunction.SEPTIC_TANK -> Default(Value.EMPTY)

        SuplaFunction.UNKNOWN,
        SuplaFunction.NONE,
        SuplaFunction.THERMOMETER,
        SuplaFunction.HUMIDITY,
        SuplaFunction.HUMIDITY_AND_TEMPERATURE,
        SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
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
        SuplaFunction.HVAC_THERMOSTAT,
        SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
        SuplaFunction.HVAC_DOMESTIC_HOT_WATER,
        SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
        SuplaFunction.GENERAL_PURPOSE_METER,
        SuplaFunction.PUMP_SWITCH,
        SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH,
        SuplaFunction.CONTAINER_LEVEL_SENSOR,
        SuplaFunction.FLOOD_SENSOR,
        SuplaFunction.MOTION_SENSOR,
        SuplaFunction.BINARY_SENSOR -> Default(Value.NOT_USED)
      }
  }
}
