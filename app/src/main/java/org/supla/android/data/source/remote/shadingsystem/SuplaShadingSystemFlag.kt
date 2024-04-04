package org.supla.android.data.source.remote.shadingsystem
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
import org.supla.android.ui.lists.data.IssueIconType

enum class SuplaShadingSystemFlag(val value: Int) {
  TILT_IS_SET(1),
  CALIBRATION_FAILED(1 shl 1),
  CALIBRATION_LOST(1 shl 2),
  MOTOR_PROBLEM(1 shl 3),
  CALIBRATION_IN_PROGRESS(1 shl 4);

  fun getIssueIconType() =
    when (this) {
      MOTOR_PROBLEM -> IssueIconType.ERROR
      CALIBRATION_LOST -> IssueIconType.WARNING
      CALIBRATION_FAILED -> IssueIconType.WARNING
      else -> null
    }

  fun getIssueMessage(): Int? =
    when (this) {
      MOTOR_PROBLEM -> R.string.motor_problem
      CALIBRATION_LOST -> R.string.calibration_lost
      CALIBRATION_FAILED -> R.string.calibration_failed
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
      for (flag in SuplaShadingSystemFlag.values()) {
        if (flag.value and value > 0) {
          result.add(flag)
        }
      }

      return result
    }
  }
}
