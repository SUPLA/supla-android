package org.supla.android.features.details.actiontrigger.schedule.data
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
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.data.source.remote.hvac.SuplaWeeklyScheduleProgram
import org.supla.android.data.source.remote.hvac.label
import org.supla.android.ui.views.schedule.editor.WeeklyScheduleProgram
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

data class ActionTriggerScheduleProgram(
  override val program: SuplaScheduleProgram,
  val buttonMode: SuplaButtonMode,
  val buttonTemporaryUnlockDurationS: Int?,
  override val label: LocalizedString,
  @param:DrawableRes override val iconRes: Int? = null
) : WeeklyScheduleProgram {

  companion object {
    operator fun invoke(program: SuplaWeeklyScheduleProgram): ActionTriggerScheduleProgram {
      val buttonMode = program.buttonMode?.let { SuplaButtonMode.from(it.rawValue) }
        ?: SuplaButtonMode.NOT_SET

      val durationS = program.buttonTemporaryUnlockDurationS
      val label = when (buttonMode) {
        SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED ->
          LocalizedString.WithFormat("%s ⏱\uFE0F", listOf(localizedString(R.string.general_locked)))
        else -> buttonMode.label
      }
      return ActionTriggerScheduleProgram(program.program, buttonMode, durationS, label)
    }

    val DEFAULT = ActionTriggerScheduleProgram(
      program = SuplaScheduleProgram.OFF,
      buttonMode = SuplaButtonMode.NOT_SET,
      buttonTemporaryUnlockDurationS = null,
      label = SuplaButtonMode.NOT_SET.label
    )
  }
}
