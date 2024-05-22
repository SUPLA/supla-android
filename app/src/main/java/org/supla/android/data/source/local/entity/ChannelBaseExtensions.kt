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
import org.supla.android.lib.SuplaConst

fun ChannelBase.isHvacThermostat() =
  function == SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT ||
    function == SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER

fun ChannelBase.isThermostat(): Boolean =
  isHvacThermostat() || function == SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS

fun ChannelBase.hasMeasurements(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
    function == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ||
    function == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR

fun ChannelBase.hasValue(): Boolean =
  when (function) {
    SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT,
    SuplaConst.SUPLA_CHANNELFNC_THERMOMETER,
    SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR,
    SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR -> true

    else -> false
  }

fun ChannelBase.isMeasurement(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_ALARMARMAMENTSENSOR ||
    function == SuplaConst.SUPLA_CHANNELFNC_HOTELCARDSENSOR ||
    function == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
    function == SuplaConst.SUPLA_CHANNELFNC_DEPTHSENSOR ||
    function == SuplaConst.SUPLA_CHANNELFNC_DISTANCESENSOR

fun ChannelBase.isGpm(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER ||
    function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

fun ChannelBase.isGpMeasurement(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT

fun ChannelBase.isGpMeter(): Boolean =
  function == SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER

fun ChannelBase.isThermometer() =
  function == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
    function == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE

fun ChannelBase.isShadingSystem() =
  function == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER ||
    function == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW ||
    function == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND ||
    function == SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING ||
    function == SuplaConst.SUPLA_CHANNELFNC_CURTAIN

fun ChannelBase.isProjectorScreen() =
  function == SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN
