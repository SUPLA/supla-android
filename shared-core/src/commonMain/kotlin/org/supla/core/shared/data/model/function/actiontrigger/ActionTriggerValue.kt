package org.supla.core.shared.data.model.function.actiontrigger
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
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.core.shared.extensions.toInt

private const val VALUE_LENGTH = 7

data class ActionTriggerValue(
  val status: SuplaChannelAvailabilityStatus,
  val relatedChannelNumber: Int,
  val disablesLocalOperation: Int,
  val buttonMode: SuplaButtonMode,
  val flags: List<SuplaActionTriggerFlag>
) {

  companion object {
    fun from(status: SuplaChannelAvailabilityStatus, bytes: ByteArray): ActionTriggerValue {
      if (bytes.size < VALUE_LENGTH) {
        return ActionTriggerValue(
          status = status,
          relatedChannelNumber = 0,
          disablesLocalOperation = 0,
          buttonMode = SuplaButtonMode.NOT_SET,
          flags = emptyList()
        )
      }

      return ActionTriggerValue(
        status = status,
        relatedChannelNumber = bytes[0].toInt(),
        disablesLocalOperation = bytes.toInt(first = 1),
        buttonMode = SuplaButtonMode.from(bytes[5]),
        flags = SuplaActionTriggerFlag.from(bytes[6])
      )
    }
  }
}
