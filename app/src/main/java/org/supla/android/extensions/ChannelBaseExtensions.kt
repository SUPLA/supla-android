package org.supla.android.extensions
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

import org.supla.android.db.ChannelBase
import org.supla.android.lib.SuplaConst

fun ChannelBase.isHvacThermostat(): Boolean =
  func == SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT ||
//    Temporarily commented out, because is not supported yet.
//    func == SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_AUTO ||
//    func == SuplaConst.SUPLA_CHANNELFNC_HVAC_DRYER ||
//    func == SuplaConst.SUPLA_CHANNELFNC_HVAC_FAN ||
//    func == SuplaConst.SUPLA_CHANNELFNC_HVAC_THERMOSTAT_DIFFERENTIAL ||
    func == SuplaConst.SUPLA_CHANNELFNC_HVAC_DOMESTIC_HOT_WATER

fun ChannelBase.isSwitch() =
  func == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH ||
    func == SuplaConst.SUPLA_CHANNELFNC_DIMMER ||
    func == SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING ||
    func == SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING ||
    func == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH ||
    func == SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER

fun ChannelBase.isRollerShutter() =
  func == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER ||
    func == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW

fun ChannelBase.isGateController() =
  func == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE ||
    func == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR

fun ChannelBase.isDoorLock() =
  func == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK ||
    func == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK

fun ChannelBase.isThermometer() =
  func == SuplaConst.SUPLA_CHANNELFNC_THERMOMETER ||
    func == SuplaConst.SUPLA_CHANNELFNC_HUMIDITYANDTEMPERATURE
