package org.supla.core.shared.data.model.shadingsystem
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

import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.lists.ChannelIssueItem
import kotlin.math.max

abstract class ShadingSystemValue {
  abstract val status: SuplaChannelAvailabilityStatus
  abstract val position: Int
  abstract val flags: List<SuplaShadingSystemFlag>

  val alwaysValidPosition: Int
    get() = max(0, position)

  fun hasValidPosition() = position != INVALID_VALUE

  fun getChannelIssue(): ChannelIssueItem? = when {
    status.online && flags.contains(SuplaShadingSystemFlag.MOTOR_PROBLEM) -> SuplaShadingSystemFlag.MOTOR_PROBLEM
    status.online && flags.contains(SuplaShadingSystemFlag.CALIBRATION_LOST) -> SuplaShadingSystemFlag.CALIBRATION_LOST
    status.online && flags.contains(SuplaShadingSystemFlag.CALIBRATION_FAILED) -> SuplaShadingSystemFlag.CALIBRATION_FAILED
    else -> null
  }?.asChannelIssues()

  companion object {
    const val INVALID_VALUE = -1
    const val MAX_VALUE = 100
  }
}
