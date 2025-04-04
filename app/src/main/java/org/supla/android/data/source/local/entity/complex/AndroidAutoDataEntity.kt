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
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.SceneEntity

data class AndroidAutoDataEntity(
  @Embedded(prefix = "channel_") val channelEntity: ChannelEntity?,
  @Embedded(prefix = "channel_group_") val groupEntity: ChannelGroupEntity?,
  @Embedded(prefix = "scene_") val sceneEntity: SceneEntity?,

  @Embedded(prefix = "profile_") val profileEntity: ProfileEntity,
  @Embedded(prefix = "item_") val androidAutoItemEntity: AndroidAutoItemEntity
)
