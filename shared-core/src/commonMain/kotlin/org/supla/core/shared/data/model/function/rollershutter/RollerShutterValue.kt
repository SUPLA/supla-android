package org.supla.core.shared.data.model.function.rollershutter
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

import org.supla.core.shared.data.model.shadingsystem.ShadingSystemValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import org.supla.core.shared.extensions.toShort

private const val ROLLER_SHUTTER_VALUE_LENGTH = 5

data class RollerShutterValue(
  override val online: Boolean,
  override val position: Int,
  val bottomPosition: Int,
  override val flags: List<SuplaShadingSystemFlag>
) : ShadingSystemValue() {

  companion object {
    private const val INVALID_BOTTOM_POSITION = 0 // more precisely <= 0

    fun from(online: Boolean, bytes: ByteArray): RollerShutterValue {
      if (bytes.size < ROLLER_SHUTTER_VALUE_LENGTH) {
        return RollerShutterValue(online, INVALID_VALUE, 0, emptyList())
      }

      return RollerShutterValue(
        online = online,
        position = bytes[0].toInt().let { if (it < INVALID_VALUE || it > MAX_VALUE) INVALID_VALUE else it },
        bottomPosition = bytes[2].toInt().let { if (it <= INVALID_BOTTOM_POSITION || it > MAX_VALUE) MAX_VALUE else it },
        flags = SuplaShadingSystemFlag.from(bytes.toShort(3, 4).toInt())
      )
    }
  }
}
