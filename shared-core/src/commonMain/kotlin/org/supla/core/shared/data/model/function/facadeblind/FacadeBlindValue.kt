package org.supla.core.shared.data.model.function.facadeblind
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
import org.supla.core.shared.data.model.shadingsystem.ShadingSystemValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import org.supla.core.shared.extensions.toShort
import kotlin.math.max

private const val FACADE_BLIND_VALUE_LENGTH = 5

data class FacadeBlindValue(
  override val status: SuplaChannelAvailabilityStatus,
  override val position: Int,
  val tilt: Int,
  override val flags: List<SuplaShadingSystemFlag>
) : ShadingSystemValue() {

  val alwaysValidTilt: Int
    get() = max(0, tilt)

  fun hasValidTilt() = tilt != INVALID_VALUE

  companion object {
    fun from(status: SuplaChannelAvailabilityStatus, bytes: ByteArray): FacadeBlindValue {
      if (bytes.size < FACADE_BLIND_VALUE_LENGTH) {
        return FacadeBlindValue(status, INVALID_VALUE, INVALID_VALUE, listOf())
      }

      return FacadeBlindValue(
        status = status,
        position = bytes[0].toInt().let { if (it < INVALID_VALUE || it > MAX_VALUE) INVALID_VALUE else it },
        tilt = bytes[1].toInt().let { if (it < INVALID_VALUE || it > MAX_VALUE) INVALID_VALUE else it },
        flags = SuplaShadingSystemFlag.from(bytes.toShort(3, 4).toInt())
      )
    }
  }
}
