package org.supla.android.features.details.switchdetail.schedule.data
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

import androidx.annotation.DrawableRes
import org.supla.android.R
import org.supla.android.data.source.remote.hvac.SuplaRelayMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleProgram
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT

data class SwitchScheduleProgram(
  override val program: SuplaScheduleProgram,
  val relayMode: SuplaRelayMode,
  val relayModeDurationS: Int?,
  val relayOppositeModeDurationS: Int?,
  override val label: LocalizedString,
  @param:DrawableRes override val iconRes: Int? = null
) : WeeklyScheduleProgram {

  val isValid: Boolean
    get() = program == SuplaScheduleProgram.OFF || relayMode.isProgramMode

  companion object {
    operator fun invoke(program: SuplaWeeklyScheduleProgram): SwitchScheduleProgram {
      val relayMode = program.relayMode ?: SuplaRelayMode.NOT_SET
      return SwitchScheduleProgram(
        program = program.program,
        relayMode = relayMode,
        relayModeDurationS = program.relayModeDurationS,
        relayOppositeModeDurationS = program.relayOppositeModeDurationS,
        label = createLabel(relayMode, program.relayModeDurationS, program.relayOppositeModeDurationS)
      )
    }

    val DEFAULT = SwitchScheduleProgram(
      program = SuplaScheduleProgram.OFF,
      relayMode = SuplaRelayMode.NOT_SET,
      relayModeDurationS = 0,
      relayOppositeModeDurationS = 0,
      label = localizedString(R.string.schedule_program_default),
      iconRes = null
    )

    private fun createLabel(mode: SuplaRelayMode, durationS: Int?, oppositeDurationS: Int?): LocalizedString =
      when (mode) {
        SuplaRelayMode.NOT_SET -> LocalizedString.Constant(NO_VALUE_TEXT)
        SuplaRelayMode.START_ON -> startLabel(
          durationS = durationS,
          oppositeDurationS = oppositeDurationS,
          defaultLabel = R.string.turn_on,
          durationLabel = R.string.schedule_program_turn_on_seconds
        )
        SuplaRelayMode.START_OFF -> startLabel(
          durationS = durationS,
          oppositeDurationS = oppositeDurationS,
          defaultLabel = R.string.turn_off,
          durationLabel = R.string.schedule_program_turn_off_seconds
        )
        SuplaRelayMode.FORCED_ON -> localizedString(R.string.schedule_program_force_on)
        SuplaRelayMode.FORCED_OFF -> localizedString(R.string.schedule_program_force_off)
        SuplaRelayMode.AUTOMATIC -> localizedString(R.string.auto)
        SuplaRelayMode.CMD_WEEKLY_SCHEDULE,
        SuplaRelayMode.CMD_SWITCH_TO_MANUAL -> LocalizedString.Constant(NO_VALUE_TEXT)
      }

    private fun startLabel(
      durationS: Int?,
      oppositeDurationS: Int?,
      defaultLabel: Int,
      durationLabel: Int
    ): LocalizedString {
      val duration = durationS ?: 0
      val oppositeDuration = oppositeDurationS ?: 0

      return when {
        duration > 0 && oppositeDuration > 0 -> localizedString(R.string.schedule_program_cycle)
        duration > 0 -> localizedString(durationLabel, duration)
        else -> localizedString(defaultLabel)
      }
    }
  }
}

private val SuplaRelayMode.isProgramMode: Boolean
  get() = when (this) {
    SuplaRelayMode.START_ON,
    SuplaRelayMode.START_OFF,
    SuplaRelayMode.FORCED_ON,
    SuplaRelayMode.FORCED_OFF,
    SuplaRelayMode.AUTOMATIC -> true
    SuplaRelayMode.NOT_SET,
    SuplaRelayMode.CMD_WEEKLY_SCHEDULE,
    SuplaRelayMode.CMD_SWITCH_TO_MANUAL -> false
  }
