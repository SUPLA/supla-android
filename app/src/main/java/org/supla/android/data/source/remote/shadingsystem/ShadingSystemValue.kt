package org.supla.android.data.source.remote.shadingsystem

import kotlin.math.max

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

abstract class ShadingSystemValue {
  abstract val online: Boolean
  abstract val position: Int
  abstract val flags: List<SuplaShadingSystemFlag>

  val alwaysValidPosition: Int
    get() = max(0, position)

  fun hasValidPosition() = position != INVALID_VALUE

  fun getIssueIconType() = when {
    online && flags.contains(SuplaShadingSystemFlag.MOTOR_PROBLEM) -> SuplaShadingSystemFlag.MOTOR_PROBLEM
    online && flags.contains(SuplaShadingSystemFlag.CALIBRATION_LOST) -> SuplaShadingSystemFlag.CALIBRATION_LOST
    online && flags.contains(SuplaShadingSystemFlag.CALIBRATION_FAILED) -> SuplaShadingSystemFlag.CALIBRATION_FAILED
    else -> null
  }?.getIssueIconType()

  fun getIssueMessage() = when {
    online && flags.contains(SuplaShadingSystemFlag.MOTOR_PROBLEM) -> SuplaShadingSystemFlag.MOTOR_PROBLEM
    online && flags.contains(SuplaShadingSystemFlag.CALIBRATION_LOST) -> SuplaShadingSystemFlag.CALIBRATION_LOST
    online && flags.contains(SuplaShadingSystemFlag.CALIBRATION_FAILED) -> SuplaShadingSystemFlag.CALIBRATION_FAILED
    else -> null
  }?.getIssueMessage()

  companion object {
    const val INVALID_VALUE = -1
    const val MAX_VALUE = 100
  }
}
