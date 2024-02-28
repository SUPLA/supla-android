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

import org.supla.android.lib.SuplaConst

fun ChannelEntity.isHvacThermostat() =
  function == SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT ||
    function == SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER

fun ChannelEntity.hasMeasurements(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
    function == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ||
    function == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR

fun ChannelEntity.hasValue(): Boolean =
  when (function) {
    SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT,
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER,
    SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR,
    SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR -> true

    else -> false
  }

fun ChannelEntity.isMeasurement(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR ||
    function == SuplaConst.SUPLA_CHANNELFNC_HOTELCARDSENSOR ||
    function == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
    function == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR ||
    function == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR

fun ChannelEntity.isGpm(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER ||
    function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

fun ChannelEntity.isGpMeasurement(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

fun ChannelEntity.isGpMeter(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

fun ChannelEntity.isThermometer() =
  function == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
    function == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
