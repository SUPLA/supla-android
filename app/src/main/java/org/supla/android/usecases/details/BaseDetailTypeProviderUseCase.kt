package org.supla.android.usecases.details
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

import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.core.shared.data.model.general.SuplaFunction

abstract class BaseDetailTypeProviderUseCase {

  fun provide(function: SuplaFunction): DetailType? = when (function) {
    SuplaFunction.DIMMER -> RgbwDetailType(DetailPage.DIMMER)
    SuplaFunction.DIMMER_CCT -> RgbwDetailType(DetailPage.DIMMER_CCT)
    SuplaFunction.DIMMER_AND_RGB_LIGHTING -> RgbwDetailType(DetailPage.RGB, DetailPage.DIMMER)
    SuplaFunction.DIMMER_CCT_AND_RGB -> RgbwDetailType(DetailPage.RGB, DetailPage.DIMMER_CCT)
    SuplaFunction.RGB_LIGHTING -> RgbwDetailType(DetailPage.RGB)
    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER -> StandardDetailType(DetailPage.ROLLER_SHUTTER)
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW -> StandardDetailType(DetailPage.ROOF_WINDOW)
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND -> StandardDetailType(DetailPage.FACADE_BLINDS)
    SuplaFunction.TERRACE_AWNING -> StandardDetailType(DetailPage.TERRACE_AWNING)
    SuplaFunction.PROJECTOR_SCREEN -> StandardDetailType(DetailPage.PROJECTOR_SCREEN)
    SuplaFunction.CURTAIN -> StandardDetailType(DetailPage.CURTAIN)
    SuplaFunction.VERTICAL_BLIND -> StandardDetailType(DetailPage.VERTICAL_BLIND)
    SuplaFunction.ROLLER_GARAGE_DOOR -> StandardDetailType(DetailPage.GARAGE_DOOR_ROLLER)
    SuplaFunction.LIGHTSWITCH,
    SuplaFunction.POWER_SWITCH,
    SuplaFunction.STAIRCASE_TIMER,
    SuplaFunction.PUMP_SWITCH,
    SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH -> StandardDetailType(DetailPage.SWITCH)
    SuplaFunction.ELECTRICITY_METER -> StandardDetailType(DetailPage.EM_GENERAL, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS)
    SuplaFunction.IC_ELECTRICITY_METER,
    SuplaFunction.IC_GAS_METER,
    SuplaFunction.IC_WATER_METER,
    SuplaFunction.IC_HEAT_METER -> IcDetailType(DetailPage.IC_GENERAL, DetailPage.IC_HISTORY)
    SuplaFunction.THERMOMETER,
    SuplaFunction.HUMIDITY_AND_TEMPERATURE -> ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))
    SuplaFunction.HUMIDITY -> HumidityDetailType(listOf(DetailPage.HUMIDITY_HISTORY))
    SuplaFunction.HVAC_THERMOSTAT,
//    Temporarily commented out, because is not supported yet.
//    SuplaConst.SuplaChannelFunction.HVAC_THERMOSTAT_AUTO,
//    SuplaConst.SuplaChannelFunction.HVAC_DRYER,
//    SuplaConst.SuplaChannelFunction.HVAC_FAN,
//    SuplaConst.SuplaChannelFunction.HVAC_THERMOSTAT_DIFFERENTIAL,
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER ->
      ThermostatDetailType(
        listOf(
          DetailPage.THERMOSTAT,
          DetailPage.SCHEDULE,
          DetailPage.THERMOSTAT_TIMER,
          DetailPage.THERMOSTAT_HISTORY
        )
      )
    SuplaFunction.HVAC_HRV -> StandardDetailType(DetailPage.RECUPERATOR_GENERAL, DetailPage.RECUPERATOR_SCHEDULE)
    SuplaFunction.DIGIGLASS_VERTICAL,
    SuplaFunction.DIGIGLASS_HORIZONTAL -> LegacyDetailType.DIGIGLASS
    SuplaFunction.GENERAL_PURPOSE_MEASUREMENT,
    SuplaFunction.GENERAL_PURPOSE_METER -> GpmDetailType(listOf(DetailPage.GPM_HISTORY))
    SuplaFunction.CONTAINER,
    SuplaFunction.SEPTIC_TANK,
    SuplaFunction.WATER_TANK -> StandardDetailType(DetailPage.CONTAINER_GENERAL)
    SuplaFunction.CONTROLLING_THE_GATE,
    SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK -> StandardDetailType(DetailPage.GATE_GENERAL)
    SuplaFunction.UNKNOWN,
    SuplaFunction.NONE,
    SuplaFunction.OPEN_SENSOR_GATEWAY,
    SuplaFunction.OPEN_SENSOR_GATE,
    SuplaFunction.OPEN_SENSOR_GARAGE_DOOR,
    SuplaFunction.NO_LIQUID_SENSOR,
    SuplaFunction.OPEN_SENSOR_DOOR,
    SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER,
    SuplaFunction.OPEN_SENSOR_ROOF_WINDOW,
    SuplaFunction.RING,
    SuplaFunction.ALARM,
    SuplaFunction.NOTIFICATION,
    SuplaFunction.DEPTH_SENSOR,
    SuplaFunction.DISTANCE_SENSOR,
    SuplaFunction.OPENING_SENSOR_WINDOW,
    SuplaFunction.HOTEL_CARD_SENSOR,
    SuplaFunction.ALARM_ARMAMENT_SENSOR,
    SuplaFunction.MAIL_SENSOR,
    SuplaFunction.WIND_SENSOR,
    SuplaFunction.PRESSURE_SENSOR,
    SuplaFunction.RAIN_SENSOR,
    SuplaFunction.WEIGHT_SENSOR,
    SuplaFunction.WEATHER_STATION,
    SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaFunction.VALVE_OPEN_CLOSE,
    SuplaFunction.VALVE_PERCENTAGE,
    SuplaFunction.CONTAINER_LEVEL_SENSOR,
    SuplaFunction.FLOOD_SENSOR,
    SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS,
    SuplaFunction.MOTION_SENSOR,
    SuplaFunction.BINARY_SENSOR -> null
  }
}
