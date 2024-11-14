package org.supla.core.shared.usecase
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
import org.supla.core.shared.infrastructure.LocalizedStringId

class GetChannelActionStringUseCase {
  fun rightButton(function: SuplaFunction): LocalizedStringId? =
    when (function) {
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.ROLLER_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE -> LocalizedStringId.GENERAL_OPEN

      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR -> LocalizedStringId.GENERAL_OPEN_CLOSE

      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.CURTAIN -> LocalizedStringId.GENERAL_REVEAL

      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN -> LocalizedStringId.GENERAL_COLLAPSE

      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> LocalizedStringId.GENERAL_TURN_ON

      else -> null
    }

  fun leftButton(function: SuplaFunction): LocalizedStringId? =
    when (function) {
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.ROLLER_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK,
      SuplaFunction.VALVE_OPEN_CLOSE,
      SuplaFunction.VALVE_PERCENTAGE -> LocalizedStringId.GENERAL_CLOSE

      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.CURTAIN -> LocalizedStringId.GENERAL_SHUT

      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN -> LocalizedStringId.GENERAL_EXPAND

      SuplaFunction.POWER_SWITCH,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS -> LocalizedStringId.GENERAL_TURN_OFF

      else -> null
    }
}
