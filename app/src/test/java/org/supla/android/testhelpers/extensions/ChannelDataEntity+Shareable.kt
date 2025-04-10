package org.supla.android.testhelpers.extensions
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

import io.mockk.every
import io.mockk.mockk
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.general.SuplaFunction

fun ChannelDataEntity.mockShareable(
  remoteId: Int = 1,
  caption: String = "",
  function: SuplaFunction = SuplaFunction.NONE,
  extendedValue: ChannelExtendedValueEntity? = null,
  status: SuplaChannelAvailabilityStatus = SuplaChannelAvailabilityStatus.ONLINE,
  value: ChannelValueEntity = mockk {
    every { getValueAsByteArray() } returns byteArrayOf()
    every { this@mockk.status } returns status
  }
) {
  every { this@mockShareable.remoteId } returns remoteId
  every { this@mockShareable.caption } returns caption
  every { this@mockShareable.channelExtendedValueEntity } returns extendedValue
  every { this@mockShareable.status } returns status
  every { this@mockShareable.channelValueEntity } returns value
  every { this@mockShareable.stateEntity } returns null
  every { this@mockShareable.function } returns function
}
