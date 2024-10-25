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
import org.supla.core.shared.data.SuplaChannelFunction

fun ChannelBase.isHvacThermostat() =
  function == SuplaChannelFunction.HVAC_THERMOSTAT ||
    function == SuplaChannelFunction.HVAC_DOMESTIC_HOT_WATER

fun ChannelBase.isThermostat(): Boolean =
  isHvacThermostat() || function == SuplaChannelFunction.THERMOSTAT_HEATPOL_HOMEPLUS

fun ChannelBase.hasMeasurements(): Boolean =
  function == SuplaChannelFunction.THERMOMETER ||
    function == SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE ||
    function == SuplaChannelFunction.DEPTH_SENSOR

fun ChannelBase.isIconValueItem(): Boolean =
  function == SuplaChannelFunction.ALARM_ARMAMENT_SENSOR ||
    function == SuplaChannelFunction.HOTEL_CARD_SENSOR ||
    function == SuplaChannelFunction.THERMOMETER ||
    function == SuplaChannelFunction.DEPTH_SENSOR ||
    function == SuplaChannelFunction.DISTANCE_SENSOR ||
    function == SuplaChannelFunction.ELECTRICITY_METER ||
    function == SuplaChannelFunction.HEAT_OR_COLD_SOURCE_SWITCH ||
    function == SuplaChannelFunction.PUMP_SWITCH

fun ChannelBase.isSwitch(): Boolean =
  function == SuplaChannelFunction.LIGHTSWITCH ||
    function == SuplaChannelFunction.POWER_SWITCH ||
    function == SuplaChannelFunction.STAIRCASE_TIMER

fun ChannelBase.isGpm(): Boolean =
  function == SuplaChannelFunction.GENERAL_PURPOSE_METER ||
    function == SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT

fun ChannelBase.isGpMeasurement(): Boolean =
  function == SuplaChannelFunction.GENERAL_PURPOSE_MEASUREMENT

fun ChannelBase.isGpMeter(): Boolean =
  function == SuplaChannelFunction.GENERAL_PURPOSE_METER

fun ChannelBase.isElectricityMeter(): Boolean =
  function == SuplaChannelFunction.ELECTRICITY_METER

fun ChannelBase.isImpulseCounter(): Boolean =
  function == SuplaChannelFunction.IC_GAS_METER ||
    function == SuplaChannelFunction.IC_HEAT_METER ||
    function == SuplaChannelFunction.IC_WATER_METER ||
    function == SuplaChannelFunction.IC_ELECTRICITY_METER

fun ChannelBase.isThermometer() =
  function == SuplaChannelFunction.THERMOMETER ||
    function == SuplaChannelFunction.HUMIDITY_AND_TEMPERATURE

fun ChannelBase.isFacadeBlind() =
  function == SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND

fun ChannelBase.isVerticalBlind() =
  function == SuplaChannelFunction.VERTICAL_BLIND

fun ChannelBase.isShadingSystem() =
  function == SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER ||
    function == SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW ||
    function == SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND ||
    function == SuplaChannelFunction.TERRACE_AWNING ||
    function == SuplaChannelFunction.CURTAIN ||
    function == SuplaChannelFunction.VERTICAL_BLIND

fun ChannelBase.isProjectorScreen() =
  function == SuplaChannelFunction.PROJECTOR_SCREEN

fun ChannelBase.isGarageDoorRoller() =
  function == SuplaChannelFunction.ROLLER_GARAGE_DOOR
