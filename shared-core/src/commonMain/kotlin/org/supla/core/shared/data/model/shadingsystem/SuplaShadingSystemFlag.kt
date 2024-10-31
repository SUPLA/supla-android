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

import org.supla.core.shared.data.model.lists.ChannelIssueItem
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString

enum class SuplaShadingSystemFlag(val value: Int) {
  TILT_IS_SET(1),
  CALIBRATION_FAILED(1 shl 1),
  CALIBRATION_LOST(1 shl 2),
  MOTOR_PROBLEM(1 shl 3),
  CALIBRATION_IN_PROGRESS(1 shl 4);

  fun asChannelIssues(): ChannelIssueItem? =
    when (this) {
      MOTOR_PROBLEM -> ChannelIssueItem.Error(localizedString(LocalizedStringId.MOTOR_PROBLEM))
      CALIBRATION_LOST -> ChannelIssueItem.Warning(localizedString(LocalizedStringId.CALIBRATION_LOST))
      CALIBRATION_FAILED -> ChannelIssueItem.Warning(localizedString(LocalizedStringId.CALIBRATION_FAILED))
      else -> null
    }

  fun isIssueFlag(): Boolean =
    when (this) {
      CALIBRATION_FAILED, CALIBRATION_LOST, MOTOR_PROBLEM -> true
      else -> false
    }

  companion object {
    fun from(value: Int): List<SuplaShadingSystemFlag> {
      val result = mutableListOf<SuplaShadingSystemFlag>()
      for (flag in entries) {
        if (flag.value and value > 0) {
          result.add(flag)
        }
      }

      return result
    }
  }
}
