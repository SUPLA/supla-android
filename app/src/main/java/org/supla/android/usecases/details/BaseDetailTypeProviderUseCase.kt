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

import org.supla.android.features.details.detailbase.standarddetail.DetailPage
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_CURTAIN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ELECTRICITY_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_ELECTRICITY_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_GAS_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_HEAT_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_IC_WATER_METER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_ROLLER_GARAGE_DOOR
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOMETER
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELFNC_VERTICAL_BLIND

abstract class BaseDetailTypeProviderUseCase {

  fun provide(function: Int): DetailType? = when (function) {
    SUPLA_CHANNELFNC_DIMMER,
    SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING,
    SUPLA_CHANNELFNC_RGBLIGHTING ->
      LegacyDetailType.RGBW

    SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER ->
      WindowDetailType(listOf(DetailPage.ROLLER_SHUTTER))

    SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW ->
      WindowDetailType(listOf(DetailPage.ROOF_WINDOW))

    SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND ->
      WindowDetailType(listOf(DetailPage.FACADE_BLINDS))

    SUPLA_CHANNELFNC_TERRACE_AWNING ->
      WindowDetailType(listOf(DetailPage.TERRACE_AWNING))

    SUPLA_CHANNELFNC_PROJECTOR_SCREEN ->
      WindowDetailType(listOf(DetailPage.PROJECTOR_SCREEN))

    SUPLA_CHANNELFNC_CURTAIN ->
      WindowDetailType(listOf(DetailPage.CURTAIN))

    SUPLA_CHANNELFNC_VERTICAL_BLIND ->
      WindowDetailType(listOf(DetailPage.VERTICAL_BLIND))

    SUPLA_CHANNELFNC_ROLLER_GARAGE_DOOR ->
      WindowDetailType(listOf(DetailPage.GARAGE_DOOR_ROLLER))

    SUPLA_CHANNELFNC_LIGHTSWITCH,
    SUPLA_CHANNELFNC_POWERSWITCH,
    SUPLA_CHANNELFNC_STAIRCASETIMER ->
      SwitchDetailType(listOf(DetailPage.SWITCH))

    SUPLA_CHANNELFNC_ELECTRICITY_METER ->
      EmDetailType(listOf(DetailPage.EM_GENERAL, DetailPage.EM_HISTORY, DetailPage.EM_SETTINGS))

    SUPLA_CHANNELFNC_IC_ELECTRICITY_METER,
    SUPLA_CHANNELFNC_IC_GAS_METER,
    SUPLA_CHANNELFNC_IC_WATER_METER,
    SUPLA_CHANNELFNC_IC_HEAT_METER ->
      LegacyDetailType.IC

    SUPLA_CHANNELFNC_THERMOMETER,
    SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE ->
      ThermometerDetailType(listOf(DetailPage.THERMOMETER_HISTORY))

    SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS ->
      LegacyDetailType.THERMOSTAT_HP

    SUPLA_CHANNELFNC_HVAC_THERMOSTAT,
//    Temporarily commented out, because is not supported yet.
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO,
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_DRYER,
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_FAN,
//    SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_DIFFERENTIAL,
    SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER ->
      ThermostatDetailType(
        listOf(
          DetailPage.THERMOSTAT,
          DetailPage.SCHEDULE,
          DetailPage.THERMOSTAT_TIMER,
          DetailPage.THERMOSTAT_HISTORY
        )
      )

    SUPLA_CHANNELFNC_DIGIGLASS_VERTICAL,
    SUPLA_CHANNELFNC_DIGIGLASS_HORIZONTAL ->
      LegacyDetailType.DIGIGLASS

    SUPLA_CHANNELFNC_GENERAL_PURPOSE_MEASUREMENT,
    SUPLA_CHANNELFNC_GENERAL_PURPOSE_METER ->
      GpmDetailType(listOf(DetailPage.GPM_HISTORY))

    else -> null
  }
}
