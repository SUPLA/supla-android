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
import org.supla.android.core.shared.shareableChannel
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.ui.lists.onlineState
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase

data class ChannelGroupRelationDataEntity(
  @Embedded(prefix = "group_") val channelGroupEntity: ChannelGroupEntity,
  @Embedded(prefix = "channel_") val channelEntity: ChannelEntity,
  @Embedded(prefix = "value_") val channelValueEntity: ChannelValueEntity,
  @Embedded(prefix = "state_") val channelStateEntity: ChannelStateEntity?
)

interface ChannelGroupRelationDataEntityConvertible {
  val getChannelStateUseCase: GetChannelStateUseCase
  val getChannelIconUseCase: GetChannelIconUseCase
  val getCaptionUseCase: GetCaptionUseCase

  val ChannelGroupRelationDataEntity.relatedChannelData: RelatedChannelData
    get() = RelatedChannelData(
      channelId = channelEntity.remoteId,
      profileId = channelEntity.profileId,
      onlineState = channelValueEntity.status.onlineState,
      icon = getChannelIconUseCase.forState(channelEntity, getChannelStateUseCase(channelEntity, channelValueEntity)),
      caption = getCaptionUseCase(shareableChannel),
      userCaption = channelEntity.caption,
      batteryIcon = null,
      showChannelStateIcon = channelValueEntity.status.online && SuplaChannelFlag.CHANNEL_STATE inside channelEntity.flags
    )
}
