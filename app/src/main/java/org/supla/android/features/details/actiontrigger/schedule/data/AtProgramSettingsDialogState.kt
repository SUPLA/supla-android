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

import org.supla.android.R
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.data.source.remote.hvac.SuplaScheduleProgram
import org.supla.android.ui.views.SegmentedComponentItem
import org.supla.core.shared.extensions.forTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

const val MAX_TEMPORARY_UNLOCK_DURATION_S = 64_800

data class AtProgramSettingsDialogState(
  val program: SuplaScheduleProgram,
  val selectedMode: SuplaButtonMode,
  val durationOptions: List<AtProgramDurationOption> = listOf(),
  val activeDuration: AtProgramDurationOption? = null,
  val currentTemporaryUnlockDurationS: Int? = null,
  val savedTemporaryUnlockDurationS: Int? = null
) {
  val saveEnabled: Boolean
    get() = activeDuration == AtProgramDurationOption.DEFAULT || currentTemporaryUnlockDurationS != 0

  val timePickerEnabled: Boolean
    get() = activeDuration == AtProgramDurationOption.CUSTOM

  val selectedDuration: Int
    get() = (lockedTemporaryUnlockAllowedActive && customDurationSelected).forTrue { currentTemporaryUnlockDurationS } ?: 0

  private val lockedTemporaryUnlockAllowedActive: Boolean
    get() = selectedMode == SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED

  private val customDurationSelected: Boolean
    get() = activeDuration == AtProgramDurationOption.CUSTOM
}

enum class AtProgramDurationOption(override val label: LocalizedString) : SegmentedComponentItem {
  DEFAULT(localizedString(R.string.action_trigger_schedule_default_temporary_unlock_duration)),
  CUSTOM(localizedString(R.string.action_trigger_schedule_other_temporary_unlock_duration));

  val isDefault: Boolean
    get() = this == DEFAULT
}
