package org.supla.core.shared.data.model.channel
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

import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.SuplaFunction

val Channel.isProjectorScreen: Boolean
  get() = function == SuplaFunction.PROJECTOR_SCREEN

val Channel.isGarageDoorRoller: Boolean
  get() = function == SuplaFunction.ROLLER_GARAGE_DOOR

val Channel.isFacadeBlind: Boolean
  get() = function == SuplaFunction.CONTROLLING_THE_FACADE_BLIND

val Channel.isVerticalBlind: Boolean
  get() = function == SuplaFunction.VERTICAL_BLIND

val Channel.isHvacThermostat: Boolean
  get() = when (function) {
    SuplaFunction.HVAC_THERMOSTAT,
    SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL,
    SuplaFunction.HVAC_DOMESTIC_HOT_WATER -> true

    else -> false
  }

val Channel.isThermostat: Boolean
  get() = isHvacThermostat || function == SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS

val Channel.isShadingSystem: Boolean
  get() = when (function) {
    SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
    SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
    SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
    SuplaFunction.TERRACE_AWNING,
    SuplaFunction.CURTAIN,
    SuplaFunction.VERTICAL_BLIND -> true

    else -> false
  }
