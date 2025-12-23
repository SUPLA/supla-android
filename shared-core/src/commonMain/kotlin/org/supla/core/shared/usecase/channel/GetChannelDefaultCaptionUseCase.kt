package org.supla.core.shared.usecase.channel
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
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_ALARM_ARMAMENT_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_BINARY_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTAINER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_DOOR_LOCK
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_FACADE_BLIND
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_GARAGE_DOOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_GATE
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_GATEWAY_LOCK
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_ROLLER_SHUTTER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CONTROLLING_THE_ROOF_WINDOW
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_CURTAIN
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_DEPTH_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_DIGIGLASS
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_DIMMER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_DIMMER_AND_RGB_LIGHTING
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_DISTANCE_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_ELECTRICITY_METER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_FLOOD_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_GENERAL_PURPOSE_MEASUREMENT
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_GENERAL_PURPOSE_METER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_HEAT_OR_COLD_SOURCE_SWITCH
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_HOTEL_CARD_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_HUMIDITY
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_HUMIDITY_AND_TEMPERATURE
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_IC_GAS_METER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_IC_HEAT_METER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_IC_WATER_METER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_LIGHTSWITCH
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_MAIL_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_MOTION_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_NO_LIQUID_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_OPENING_SENSOR_WINDOW
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_DOOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_GARAGE_DOOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_GATE
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_GATEWAY
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_ROLLER_SHUTTER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_OPEN_SENSOR_ROOF_WINDOW
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_POWER_SWITCH
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_PRESSURE_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_PROJECTOR_SCREEN
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_PUMP_SWITCH
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_RAIN_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_RGB_LIGHTING
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_ROLLER_GARAGE_DOOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_SEPTIC_TANK
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_STAIRCASE_TIMER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_TERRACE_AWNING
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_THERMOMETER
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_THERMOSTAT
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_THERMOSTAT_HEATPOL_HOMEPLUS
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_UNKNOWN
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_VALVE
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_VERTICAL_BLIND
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_WATER_TANK
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_WEIGHT_SENSOR
import org.supla.core.shared.infrastructure.LocalizedStringId.CHANNEL_CAPTION_WIND_SENSOR
import org.supla.core.shared.infrastructure.localizedString

class GetChannelDefaultCaptionUseCase {
  operator fun invoke(function: SuplaFunction) =
    when (function) {
      SuplaFunction.OPEN_SENSOR_GATEWAY -> localizedString(CHANNEL_CAPTION_OPEN_SENSOR_GATEWAY)
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK -> localizedString(CHANNEL_CAPTION_CONTROLLING_THE_GATEWAY_LOCK)
      SuplaFunction.OPEN_SENSOR_GATE -> localizedString(CHANNEL_CAPTION_OPEN_SENSOR_GATE)
      SuplaFunction.CONTROLLING_THE_GATE -> localizedString(CHANNEL_CAPTION_CONTROLLING_THE_GATE)
      SuplaFunction.OPEN_SENSOR_GARAGE_DOOR -> localizedString(CHANNEL_CAPTION_OPEN_SENSOR_GARAGE_DOOR)
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR -> localizedString(CHANNEL_CAPTION_CONTROLLING_THE_GARAGE_DOOR)
      SuplaFunction.OPEN_SENSOR_DOOR -> localizedString(CHANNEL_CAPTION_OPEN_SENSOR_DOOR)
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK -> localizedString(CHANNEL_CAPTION_CONTROLLING_THE_DOOR_LOCK)
      SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER -> localizedString(CHANNEL_CAPTION_OPEN_SENSOR_ROLLER_SHUTTER)
      SuplaFunction.OPEN_SENSOR_ROOF_WINDOW -> localizedString(CHANNEL_CAPTION_OPEN_SENSOR_ROOF_WINDOW)
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER -> localizedString(CHANNEL_CAPTION_CONTROLLING_THE_ROLLER_SHUTTER)
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW -> localizedString(CHANNEL_CAPTION_CONTROLLING_THE_ROOF_WINDOW)
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND -> localizedString(CHANNEL_CAPTION_CONTROLLING_THE_FACADE_BLIND)
      SuplaFunction.POWER_SWITCH -> localizedString(CHANNEL_CAPTION_POWER_SWITCH)
      SuplaFunction.LIGHTSWITCH -> localizedString(CHANNEL_CAPTION_LIGHTSWITCH)
      SuplaFunction.THERMOMETER -> localizedString(CHANNEL_CAPTION_THERMOMETER)
      SuplaFunction.HUMIDITY -> localizedString(CHANNEL_CAPTION_HUMIDITY)
      SuplaFunction.HUMIDITY_AND_TEMPERATURE -> localizedString(CHANNEL_CAPTION_HUMIDITY_AND_TEMPERATURE)
      SuplaFunction.WIND_SENSOR -> localizedString(CHANNEL_CAPTION_WIND_SENSOR)
      SuplaFunction.PRESSURE_SENSOR -> localizedString(CHANNEL_CAPTION_PRESSURE_SENSOR)
      SuplaFunction.RAIN_SENSOR -> localizedString(CHANNEL_CAPTION_RAIN_SENSOR)
      SuplaFunction.WEIGHT_SENSOR -> localizedString(CHANNEL_CAPTION_WEIGHT_SENSOR)
      SuplaFunction.NO_LIQUID_SENSOR -> localizedString(CHANNEL_CAPTION_NO_LIQUID_SENSOR)
      SuplaFunction.DIMMER, SuplaFunction.DIMMER_CCT -> localizedString(CHANNEL_CAPTION_DIMMER)
      SuplaFunction.RGB_LIGHTING -> localizedString(CHANNEL_CAPTION_RGB_LIGHTING)
      SuplaFunction.DIMMER_AND_RGB_LIGHTING, SuplaFunction.DIMMER_CCT_AND_RGB ->
        localizedString(CHANNEL_CAPTION_DIMMER_AND_RGB_LIGHTING)

      SuplaFunction.DEPTH_SENSOR -> localizedString(CHANNEL_CAPTION_DEPTH_SENSOR)
      SuplaFunction.DISTANCE_SENSOR -> localizedString(CHANNEL_CAPTION_DISTANCE_SENSOR)
      SuplaFunction.OPENING_SENSOR_WINDOW -> localizedString(CHANNEL_CAPTION_OPENING_SENSOR_WINDOW)
      SuplaFunction.HOTEL_CARD_SENSOR -> localizedString(CHANNEL_CAPTION_HOTEL_CARD_SENSOR)
      SuplaFunction.ALARM_ARMAMENT_SENSOR -> localizedString(CHANNEL_CAPTION_ALARM_ARMAMENT_SENSOR)
      SuplaFunction.MAIL_SENSOR -> localizedString(CHANNEL_CAPTION_MAIL_SENSOR)
      SuplaFunction.STAIRCASE_TIMER -> localizedString(CHANNEL_CAPTION_STAIRCASE_TIMER)
      SuplaFunction.IC_GAS_METER -> localizedString(CHANNEL_CAPTION_IC_GAS_METER)
      SuplaFunction.IC_WATER_METER -> localizedString(CHANNEL_CAPTION_IC_WATER_METER)
      SuplaFunction.IC_HEAT_METER -> localizedString(CHANNEL_CAPTION_IC_HEAT_METER)
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> localizedString(CHANNEL_CAPTION_THERMOSTAT_HEATPOL_HOMEPLUS)
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE -> localizedString(CHANNEL_CAPTION_VALVE)

      SuplaFunction.GENERAL_PURPOSE_MEASUREMENT -> localizedString(CHANNEL_CAPTION_GENERAL_PURPOSE_MEASUREMENT)
      SuplaFunction.GENERAL_PURPOSE_METER -> localizedString(CHANNEL_CAPTION_GENERAL_PURPOSE_METER)
      SuplaFunction.HVAC_THERMOSTAT,
      SuplaFunction.HVAC_DOMESTIC_HOT_WATER -> localizedString(CHANNEL_CAPTION_THERMOSTAT)

      SuplaFunction.ELECTRICITY_METER,
      SuplaFunction.IC_ELECTRICITY_METER -> localizedString(CHANNEL_CAPTION_ELECTRICITY_METER)

      SuplaFunction.DIGIGLASS_VERTICAL,
      SuplaFunction.DIGIGLASS_HORIZONTAL -> localizedString(CHANNEL_CAPTION_DIGIGLASS)

      SuplaFunction.TERRACE_AWNING -> localizedString(CHANNEL_CAPTION_TERRACE_AWNING)
      SuplaFunction.PROJECTOR_SCREEN -> localizedString(CHANNEL_CAPTION_PROJECTOR_SCREEN)
      SuplaFunction.CURTAIN -> localizedString(CHANNEL_CAPTION_CURTAIN)
      SuplaFunction.VERTICAL_BLIND -> localizedString(CHANNEL_CAPTION_VERTICAL_BLIND)
      SuplaFunction.ROLLER_GARAGE_DOOR -> localizedString(CHANNEL_CAPTION_ROLLER_GARAGE_DOOR)
      SuplaFunction.PUMP_SWITCH -> localizedString(CHANNEL_CAPTION_PUMP_SWITCH)
      SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH -> localizedString(CHANNEL_CAPTION_HEAT_OR_COLD_SOURCE_SWITCH)
      SuplaFunction.CONTAINER -> localizedString(CHANNEL_CAPTION_CONTAINER)
      SuplaFunction.SEPTIC_TANK -> localizedString(CHANNEL_CAPTION_SEPTIC_TANK)
      SuplaFunction.WATER_TANK -> localizedString(CHANNEL_CAPTION_WATER_TANK)
      SuplaFunction.CONTAINER_LEVEL_SENSOR -> localizedString(CHANNEL_CAPTION_CONTAINER_LEVEL_SENSOR)
      SuplaFunction.FLOOD_SENSOR -> localizedString(CHANNEL_CAPTION_FLOOD_SENSOR)
      SuplaFunction.MOTION_SENSOR -> localizedString(CHANNEL_CAPTION_MOTION_SENSOR)
      SuplaFunction.BINARY_SENSOR -> localizedString(CHANNEL_CAPTION_BINARY_SENSOR)

      SuplaFunction.UNKNOWN,
      SuplaFunction.NONE,
      SuplaFunction.RING,
      SuplaFunction.ALARM,
      SuplaFunction.NOTIFICATION,
      SuplaFunction.WEATHER_STATION,
      SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL -> localizedString(CHANNEL_CAPTION_UNKNOWN)
    }
}
