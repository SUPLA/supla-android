package org.supla.android.data.source.local.entity
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

import org.supla.android.data.model.general.ChannelBase
import org.supla.core.shared.data.model.general.SuplaFunction

fun ChannelBase.isHvacThermostat() =
  function == SuplaFunction.HVAC_THERMOSTAT ||
    function == SuplaFunction.HVAC_DOMESTIC_HOT_WATER

fun ChannelBase.isThermostat(): Boolean =
  isHvacThermostat() || function == SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS

fun ChannelBase.hasMeasurements(): Boolean =
  function == SuplaFunction.THERMOMETER ||
    function == SuplaFunction.HUMIDITY_AND_TEMPERATURE ||
    function == SuplaFunction.DEPTH_SENSOR

fun ChannelBase.isIconValueItem(): Boolean =
  function == SuplaFunction.ALARM_ARMAMENT_SENSOR ||
    function == SuplaFunction.HOTEL_CARD_SENSOR ||
    function == SuplaFunction.THERMOMETER ||
    function == SuplaFunction.DEPTH_SENSOR ||
    function == SuplaFunction.DISTANCE_SENSOR ||
    function == SuplaFunction.ELECTRICITY_METER ||
    function == SuplaFunction.HEAT_OR_COLD_SOURCE_SWITCH ||
    function == SuplaFunction.PUMP_SWITCH ||
    function == SuplaFunction.NO_LIQUID_SENSOR ||
    function == SuplaFunction.RAIN_SENSOR ||
    function == SuplaFunction.MAIL_SENSOR ||
    function == SuplaFunction.OPENING_SENSOR_WINDOW ||
    function == SuplaFunction.OPEN_SENSOR_DOOR ||
    function == SuplaFunction.OPEN_SENSOR_GATE ||
    function == SuplaFunction.OPEN_SENSOR_GATEWAY ||
    function == SuplaFunction.OPEN_SENSOR_GARAGE_DOOR ||
    function == SuplaFunction.OPEN_SENSOR_ROOF_WINDOW ||
    function == SuplaFunction.OPEN_SENSOR_ROLLER_SHUTTER ||
    function == SuplaFunction.PRESSURE_SENSOR ||
    function == SuplaFunction.HUMIDITY ||
    function == SuplaFunction.CONTAINER

fun ChannelBase.isSwitch(): Boolean =
  function == SuplaFunction.LIGHTSWITCH ||
    function == SuplaFunction.POWER_SWITCH ||
    function == SuplaFunction.STAIRCASE_TIMER

fun ChannelBase.isGpm(): Boolean =
  function == SuplaFunction.GENERAL_PURPOSE_METER ||
    function == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT

fun ChannelBase.isGpMeasurement(): Boolean =
  function == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT

fun ChannelBase.isGpMeter(): Boolean =
  function == SuplaFunction.GENERAL_PURPOSE_METER

fun ChannelBase.isElectricityMeter(): Boolean =
  function == SuplaFunction.ELECTRICITY_METER

fun ChannelBase.isImpulseCounter(): Boolean =
  function == SuplaFunction.IC_GAS_METER ||
    function == SuplaFunction.IC_HEAT_METER ||
    function == SuplaFunction.IC_WATER_METER ||
    function == SuplaFunction.IC_ELECTRICITY_METER

fun ChannelBase.isThermometer() =
  function == SuplaFunction.THERMOMETER ||
    function == SuplaFunction.HUMIDITY_AND_TEMPERATURE

fun ChannelBase.isFacadeBlind() =
  function == SuplaFunction.CONTROLLING_THE_FACADE_BLIND

fun ChannelBase.isVerticalBlind() =
  function == SuplaFunction.VERTICAL_BLIND

fun ChannelBase.isShadingSystem() =
  function == SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER ||
    function == SuplaFunction.CONTROLLING_THE_ROOF_WINDOW ||
    function == SuplaFunction.CONTROLLING_THE_FACADE_BLIND ||
    function == SuplaFunction.TERRACE_AWNING ||
    function == SuplaFunction.CURTAIN ||
    function == SuplaFunction.VERTICAL_BLIND

fun ChannelBase.isProjectorScreen() =
  function == SuplaFunction.PROJECTOR_SCREEN

fun ChannelBase.isGarageDoorRoller() =
  function == SuplaFunction.ROLLER_GARAGE_DOOR
