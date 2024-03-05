package org.supla.android.data.source.remote.rollershutter
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

import org.supla.android.extensions.toShortVararg

private const val ROLLER_SHUTTER_VALUE_LENGTH = 5

data class RollerShutterValue(
  val online: Boolean,
  val position: Int,
  val tilt: Int,
  val bottomPosition: Int,
  val flags: List<SuplaRollerShutterFlag>
) {

  fun hasValidPosition() = position != INVALID_POSITION

  fun getIssueIconType() = when {
    online && flags.contains(SuplaRollerShutterFlag.MOTOR_PROBLEM) -> SuplaRollerShutterFlag.MOTOR_PROBLEM
    online && flags.contains(SuplaRollerShutterFlag.CALIBRATION_LOST) -> SuplaRollerShutterFlag.CALIBRATION_LOST
    online && flags.contains(SuplaRollerShutterFlag.CALIBRATION_FAILED) -> SuplaRollerShutterFlag.CALIBRATION_FAILED
    else -> null
  }?.getIssueIconType()

  fun getIssueMessage() = when {
    online && flags.contains(SuplaRollerShutterFlag.MOTOR_PROBLEM) -> SuplaRollerShutterFlag.MOTOR_PROBLEM
    online && flags.contains(SuplaRollerShutterFlag.CALIBRATION_LOST) -> SuplaRollerShutterFlag.CALIBRATION_LOST
    online && flags.contains(SuplaRollerShutterFlag.CALIBRATION_FAILED) -> SuplaRollerShutterFlag.CALIBRATION_FAILED
    else -> null
  }?.getIssueMessage()

  companion object {
    private const val INVALID_POSITION = -1
    private const val INVALID_BOTTOM_POSITION = 0 // more precisely < 0
    private const val MAX_POSITION = 100

    fun from(online: Boolean, bytes: ByteArray): RollerShutterValue {
      if (bytes.size < ROLLER_SHUTTER_VALUE_LENGTH) {
        return RollerShutterValue(online, INVALID_POSITION, 0, 0, listOf())
      }

      return RollerShutterValue(
        online = online,
        position = bytes[0].toInt().let { if (it < INVALID_POSITION || it > MAX_POSITION) INVALID_POSITION else it },
        tilt = bytes[1].toInt(),
        bottomPosition = bytes[2].toInt().let { if (it <= INVALID_BOTTOM_POSITION || it > MAX_POSITION) MAX_POSITION else it },
        flags = SuplaRollerShutterFlag.from(bytes.toShortVararg(3, 4).toInt())
      )
    }
  }
}
