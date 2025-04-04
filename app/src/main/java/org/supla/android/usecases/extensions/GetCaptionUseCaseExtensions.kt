package org.supla.android.usecases.extensions
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

import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.core.shared.data.model.general.BaseData
import org.supla.core.shared.data.model.general.Channel
import org.supla.core.shared.data.model.general.Group
import org.supla.core.shared.data.model.general.Scene
import org.supla.core.shared.usecase.GetCaptionUseCase

operator fun GetCaptionUseCase.invoke(channelEntity: ChannelEntity) = invoke(channelEntity.shareable)
operator fun GetCaptionUseCase.invoke(channelGroupEntity: ChannelGroupEntity) = invoke(channelGroupEntity.shareable)
operator fun GetCaptionUseCase.invoke(sceneEntity: SceneEntity) = invoke(sceneEntity.shareable)

private val ChannelEntity.shareable: BaseData
  get() = Channel(
    remoteId = remoteId,
    caption = caption,
    function = function,
    batteryInfo = null,
    status = SuplaChannelAvailabilityStatus.OFFLINE,
    value = null
  )

private val ChannelGroupEntity.shareable: BaseData
  get() = Group(
    remoteId = remoteId,
    caption = caption,
    function = function
  )

private val SceneEntity.shareable: BaseData
  get() = Scene(
    remoteId = remoteId,
    caption = caption
  )
