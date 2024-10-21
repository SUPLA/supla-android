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

import org.supla.core.shared.data.SuplaChannelFunction
import org.supla.core.shared.infrastructure.LocalizedString

class GetChannelActionStringUseCase {
  fun rightButton(function: SuplaChannelFunction): LocalizedString? =
    when (function) {
      SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaChannelFunction.ROLLER_GARAGE_DOOR -> LocalizedString.GENERAL_OPEN

      SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaChannelFunction.VERTICAL_BLIND,
      SuplaChannelFunction.CURTAIN -> LocalizedString.GENERAL_REVEAL

      SuplaChannelFunction.TERRACE_AWNING,
      SuplaChannelFunction.PROJECTOR_SCREEN -> LocalizedString.GENERAL_COLLAPSE

      SuplaChannelFunction.POWER_SWITCH,
      SuplaChannelFunction.LIGHTSWITCH,
      SuplaChannelFunction.STAIRCASE_TIMER -> LocalizedString.GENERAL_TURN_ON

      else -> null
    }

  fun leftButton(function: SuplaChannelFunction): LocalizedString? =
    when (function) {
      SuplaChannelFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaChannelFunction.ROLLER_GARAGE_DOOR -> LocalizedString.GENERAL_CLOSE

      SuplaChannelFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaChannelFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaChannelFunction.VERTICAL_BLIND,
      SuplaChannelFunction.CURTAIN -> LocalizedString.GENERAL_SHUT

      SuplaChannelFunction.TERRACE_AWNING,
      SuplaChannelFunction.PROJECTOR_SCREEN -> LocalizedString.GENERAL_EXPAND

      SuplaChannelFunction.POWER_SWITCH,
      SuplaChannelFunction.LIGHTSWITCH,
      SuplaChannelFunction.STAIRCASE_TIMER -> LocalizedString.GENERAL_TURN_OFF

      else -> null
    }
}