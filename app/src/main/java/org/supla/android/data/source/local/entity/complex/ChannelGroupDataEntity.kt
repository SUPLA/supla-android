package org.supla.android.data.source.local.entity.complex
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

import androidx.room.Embedded
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.db.ChannelGroup

data class ChannelGroupDataEntity(
  @Embedded(prefix = "group_") val channelGroupEntity: ChannelGroupEntity,
  @Embedded(prefix = "location_") val locationEntity: LocationEntity
) : ChannelDataBase {
  override val id: Long?
    get() = channelGroupEntity.id
  override val remoteId: Int
    get() = channelGroupEntity.remoteId
  override val function: Int
    get() = channelGroupEntity.function
  override val caption: String
    get() = channelGroupEntity.caption
  override val locationId: Int
    get() = channelGroupEntity.locationId
  override val flags: Long
    get() = channelGroupEntity.flags
  override val visible: Int
    get() = channelGroupEntity.visible
  override val userIcon: Int
    get() = channelGroupEntity.userIcon
  override val altIcon: Int
    get() = channelGroupEntity.altIcon
  override val profileId: Long
    get() = channelGroupEntity.profileId
  override val locationCaption: String
    get() = locationEntity.caption

  override fun isOnline(): Boolean = channelGroupEntity.online > 0

  override fun onlinePercentage(): Int =
    if (channelGroupEntity.online > 100) {
      100
    } else if (channelGroupEntity.online < 0) {
      0
    } else {
      channelGroupEntity.online
    }

  @Deprecated("Please use ChannelGroupDataEntity if possible")
  fun getLegacyGroup(): ChannelGroup = ChannelGroup().also {
    it.id = channelGroupEntity.id
    it.remoteId = channelGroupEntity.remoteId
    it.func = channelGroupEntity.function
    it.visible = channelGroupEntity.visible
    it.setOnline(channelGroupEntity.online)
    it.setCaption(channelGroupEntity.caption)
    it.totalValue = channelGroupEntity.totalValue
    it.locationId = channelGroupEntity.locationId.toLong()
    it.altIcon = channelGroupEntity.altIcon
    it.userIconId = channelGroupEntity.userIcon
    it.flags = channelGroupEntity.flags
    it.position = channelGroupEntity.position
    it.profileId = channelGroupEntity.profileId
  }
}
