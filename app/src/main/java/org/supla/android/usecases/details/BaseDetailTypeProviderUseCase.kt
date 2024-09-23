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

import org.supla.android.data.source.remote.channel.SuplaChannelFunction
import org.supla.android.features.details.detailbase.standarddetail.DetailPage

abstract class BaseDetailTypeProviderUseCase {

  fun provide(function: SuplaChannelFunction): DetailType? = when (function) {
    SuplaChannelFunction.DIMMER,
    SuplaChannelFunction.DIMMER_AND_RGB_LIGHTING,
    SuplaChannelFunction.RGB_LIGHTING ->
      LegacyDetailType.RGBW

    SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER ->
      WindowDetailType(listOf(DetailPage.ROLLER_SHUTTER))

    SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW ->
      WindowDetailType(listOf(DetailPage.ROOF_WINDOW))

    SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND ->
      WindowDetailType(listOf(DetailPage.FACADE_BLINDS))

    SuplaChannelFunction.TERRACE_AWNING ->
      WindowDetailType(listOf(DetailPage.TERRACE_AWNING))

    SuplaChannelFunction.PROJECTOR_SCREEN ->
      WindowDetailType(listOf(DetailPage.PROJECTOR_SCREEN))

    SuplaChannelFunction.CURTAIN ->
      WindowDetailType(listOf(DetailPage.CURTAIN))

    SuplaChannelFunction.VERTICAL_BLIND ->
      WindowDetailType(listOf(DetailPage.VERTICAL_BLIND))

    SuplaChannelFunction.ROLLER_GARAGE_DOOR ->
      WindowDetailType(listOf(DetailPage.GARAGE_DOOR_ROLLER))

    SuplaChannelFunction.LIGHTSWITCH,
    SuplaChannelFunction.POWER_SWITCH,
    SuplaChannelFunction.STAIRCASE_TIMER,
    SuplaChannelFunction.PUMP_SWITCH,
    SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH ->
      SwitchDetailType(listOf(DetailPage.SWITCH))

    SuplaChannelFunction.ELECTRICITY_METER ->
      EmDetailType(listOf(DetailPage.EM_GENERAL, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))

    SuplaChannelFunction.IC_ELECTRICITY_METER,
    SuplaChannelFunction.IC_GAS_METER,
    SuplaChannelFunction.IC_WATER_METER,
    SuplaChannelFunction.IC_HEAT_METER ->
      LegacyDetailType.IC

    SuplaChannelFunction.THERMOMETER,
    SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE ->
      ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))

    SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS ->
      LegacyDetailType.THERMOSTAT_HP

    SuplaChannelFunction.HVAC_THERMOSTAT,
//    Temporarily commented out, because is not supported yet.
//    SuplaConst.SuplaChannelFunction.HVAC_THERMOSTAT_AUTO,
//    SuplaConst.SuplaChannelFunction.HVAC_DRYER,
//    SuplaConst.SuplaChannelFunction.HVAC_FAN,
//    SuplaConst.SuplaChannelFunction.HVAC_THERMOSTAT_DIFFERENTIAL,
    SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER ->
      ThermostatDetailType(
        listOf(
          DetailPage.THERMOSTAT,
          DetailPage.SCHEDULE,
          DetailPage.THERMOSTAT_TIMER,
          DetailPage.THERMOSTAT_HISTORY
        )
      )

    SuplaChannelFunction.DIGIGLASS_VERTICAL,
    SuplaChannelFunction.DIGIGLASS_HORIZONTAL ->
      LegacyDetailType.DIGIGLASS

    SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT,
    SuplaChannelFunction.GENERAL_PURPOSE_METER ->
      GpmDetailType(listOf(DetailPage.GPM_HISTORY))

    SuplaChannelFunction.UNKNOWN,
    SuplaChannelFunction.NONE,
    SuplaChannelFunction.CONTROLLING_THE_GATEWAY_LOCK,
    SuplaChannelFunction.CONTROLLING_THE_GATE,
    SuplaChannelFunction.CONTROLLING_THE_GARAGE_DOOR,
    SuplaChannelFunction.HUMIDITY,
    SuplaChannelFunction.OPEN_SENSOR_GATEWAY,
    SuplaChannelFunction.OPEN_SENSOR_GATE,
    SuplaChannelFunction.OPEN_SENSOR_GARAGE_DOOR,
    SuplaChannelFunction.NO_LIQUID_SENSOR,
    SuplaChannelFunction.CONTROLLING_THE_DOOR_LOCK,
    SuplaChannelFunction.OPEN_SENSOR_DOOR,
    SuplaChannelFunction.OPEN_SENSOR_ROLLER_SHUTTER,
    SuplaChannelFunction.OPEN_SENSOR_ROOF_WINDOW,
    SuplaChannelFunction.RING,
    SuplaChannelFunction.ALARM,
    SuplaChannelFunction.NOTIFICATION,
    SuplaChannelFunction.DEPTH_SENSOR,
    SuplaChannelFunction.DISTANCE_SENSOR,
    SuplaChannelFunction.OPENING_SENSOR_WINDOW,
    SuplaChannelFunction.HOTEL_CARD_SENSOR,
    SuplaChannelFunction.ALARM_ARMAMENT_SENSOR,
    SuplaChannelFunction.MAIL_SENSOR,
    SuplaChannelFunction.WIND_SENSOR,
    SuplaChannelFunction.PRESSURE_SENSOR,
    SuplaChannelFunction.RAIN_SENSOR,
    SuplaChannelFunction.WEIGHT_SENSOR,
    SuplaChannelFunction.WEATHER_STATION,
    SuplaChannelFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaChannelFunction.VALVE_OPEN_CLOSE,
    SuplaChannelFunction.VALVE_PERCENTAGE -> null
  }
}
