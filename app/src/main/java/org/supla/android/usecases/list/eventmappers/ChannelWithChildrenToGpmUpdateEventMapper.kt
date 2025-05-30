package org.supla.android.usecases.list.eventmappers
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

import org.supla.android.core.shared.shareable
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.local.entity.extensions.onlineState
import org.supla.android.data.source.local.entity.isGpm
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.list.CreateListItemUpdateEventDataUseCase
import org.supla.core.shared.usecase.GetCaptionUseCase
import org.supla.core.shared.usecase.channel.GetChannelIssuesForListUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelWithChildrenToGpmUpdateEventMapper @Inject constructor(
  private val getChannelIssuesForListUseCase: GetChannelIssuesForListUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase
) : CreateListItemUpdateEventDataUseCase.Mapper {
  override fun handle(item: Any): Boolean {
    return (item as? ChannelWithChildren)?.channel?.channelEntity?.isGpm() == true
  }

  override fun map(item: Any): SlideableListItemData {
    val channelWithChildren = item as ChannelWithChildren
    return toSlideableListItemData(channelWithChildren)
  }

  private fun toSlideableListItemData(channelWithChildren: ChannelWithChildren): SlideableListItemData.Default =
    SlideableListItemData.Default(
      onlineState = channelWithChildren.onlineState,
      title = getCaptionUseCase(channelWithChildren.channel.shareable),
      icon = getChannelIconUseCase.invoke(channelWithChildren.channel),
      value = getChannelValueStringUseCase(channelWithChildren),
      issues = getChannelIssuesForListUseCase(channelWithChildren.shareable),
      estimatedTimerEndDate = null,
      infoSupported = SuplaChannelFlag.CHANNEL_STATE.inside(channelWithChildren.channel.flags)
    )
}
