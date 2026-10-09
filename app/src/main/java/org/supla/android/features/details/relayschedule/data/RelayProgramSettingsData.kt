package org.supla.android.features.details.relayschedule.data
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

import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.core.shared.data.model.general.SuplaFunction

const val MAX_PROGRAM_DURATION_S = 65_535

data class RelayProgramSettingsData(
  val program: SuplaScheduleProgram,
  val selectedMode: SuplaRelayMode,
  val relayDurationS: Int = 0,
  val relayOppositeDurationS: Int = 0
)

enum class RelayProgramDuration {
  RELAY_MODE,
  OPPOSITE_MODE
}

data class RelayProgramSettingsConstraints(
  val modes: List<SuplaRelayMode> = listOf(
    SuplaRelayMode.START_ON,
    SuplaRelayMode.START_OFF,
    SuplaRelayMode.FORCED_ON,
    SuplaRelayMode.FORCED_OFF,
    SuplaRelayMode.AUTOMATIC
  ),
  val durationSupported: Boolean = true,
  val oppositeDurationSupported: Boolean = true
) {
  companion object {
    operator fun invoke(function: SuplaFunction): RelayProgramSettingsConstraints = when (function) {
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK -> RelayProgramSettingsConstraints(
        modes = listOf(SuplaRelayMode.START_ON, SuplaRelayMode.FORCED_OFF),
        durationSupported = false,
        oppositeDurationSupported = false
      )
      SuplaFunction.STAIRCASE_TIMER -> RelayProgramSettingsConstraints(oppositeDurationSupported = false)
      else -> RelayProgramSettingsConstraints()
    }
  }
}
