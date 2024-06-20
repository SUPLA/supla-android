package org.supla.android.data.source.remote.rollershutter

import org.supla.android.tools.UsedFromNativeCode

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

@UsedFromNativeCode
data class SuplaChannelFacadeBlindConfig(
  override val remoteId: Int,
  override val func: Int?,
  override val crc32: Long,
  override val closingTimeMs: Int,
  override val openingTimeMs: Int,
  val tiltingTimeMs: Int,
  override val motorUpsideDown: Boolean,
  override val buttonsUpsideDown: Boolean,
  override val timeMargin: Int,
  val tilt0Angle: Int,
  val tilt100Angle: Int,
  val type: SuplaTiltControlType
) : SuplaChannelRollerShutterConfig(
  remoteId,
  func,
  crc32,
  closingTimeMs,
  openingTimeMs,
  motorUpsideDown,
  buttonsUpsideDown,
  timeMargin
)

enum class SuplaTiltControlType(val rawValue: Int) {
  UNKNOWN(0),
  STANDS_IN_POSITION_WHILE_TILTING(1),
  CHANGES_POSITION_WHILE_TILTING(2),
  TILTS_ONLY_WHEN_FULLY_CLOSED(3)
}
