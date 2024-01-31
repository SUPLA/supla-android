package org.supla.android.testhelpers
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
import org.supla.android.data.source.local.entity.ChannelRelationType
import org.supla.android.db.AuthProfileItem
import org.supla.android.lib.SuplaChannel
import org.supla.android.lib.SuplaChannelRelation
import org.supla.android.lib.SuplaChannelValue

fun profileMock(profileId: Long) = mockk<AuthProfileItem>().also {
  every { it.id } returns profileId
}

fun relationMock(channelId: Int, parentId: Int, relationType: ChannelRelationType) = mockk<SuplaChannelRelation>().also {
  every { it.channelId } returns channelId
  every { it.parentId } returns parentId
  every { it.relationType } returns relationType.value
}

fun suplaChannel(
  locationId: Int? = null,
  channelId: Int? = null,
  caption: String = "",
  function: Int = 0,
  crc32: Long = 0,
  value: SuplaChannelValue? = null,
  online: Boolean = false
) = SuplaChannel().apply {
  locationId?.let { LocationID = it }
  channelId?.let { Id = it }
  Caption = caption
  Func = function
  DefaultConfigCRC32 = crc32
  value?.let { Value = value }
  OnLine = online
}

fun suplaChannelValue() = SuplaChannelValue().apply {
}
