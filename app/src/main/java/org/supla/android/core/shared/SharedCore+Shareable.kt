package org.supla.android.core.shared
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

import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.complex.batteryInfo
import org.supla.android.data.source.local.entity.complex.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.core.shared.data.model.channel.ChannelChild
import org.supla.core.shared.data.model.channel.ChannelRelation
import org.supla.core.shared.data.model.general.BaseData
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.Group

val ChannelDataBase.shareable: BaseData
  get() = when (this) {
    is ChannelDataEntity -> Channel(
      remoteId = remoteId,
      caption = caption,
      function = function,
      batteryInfo = batteryInfo,
      online = isOnline(),
      value = channelValueEntity.getValueAsByteArray()
    )

    is ChannelGroupDataEntity -> Group(
      remoteId = remoteId,
      function = function,
      caption = caption
    )

    else -> throw IllegalStateException("Unexpected type")
  }

val ChannelRelationEntity.shareable: ChannelRelation
  get() = ChannelRelation(
    channelId = channelId,
    parentId = parentId,
    relationType = relationType
  )

val ChannelChildEntity.shareable: ChannelChild
  get() = ChannelChild(
    channel = channelDataEntity.shareable,
    relation = channelRelationEntity.shareable,
    children = children.map { it.shareable }
  )

val ChannelWithChildren.shareable: org.supla.core.shared.data.model.channel.ChannelWithChildren
  get() = org.supla.core.shared.data.model.channel.ChannelWithChildren(
    channel = channel.shareable,
    children = children.map { it.shareable }
  )
