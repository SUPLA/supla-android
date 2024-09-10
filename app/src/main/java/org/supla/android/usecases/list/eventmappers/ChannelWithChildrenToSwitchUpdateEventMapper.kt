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

import org.supla.android.data.source.local.entity.isSwitch
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.extensions.guardLet
import org.supla.android.ui.lists.data.SlideableListItemData
import org.supla.android.usecases.channel.ChannelWithChildren
import org.supla.android.usecases.channel.GetChannelCaptionUseCase
import org.supla.android.usecases.channel.GetChannelValueStringUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.list.CreateListItemUpdateEventDataUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelWithChildrenToSwitchUpdateEventMapper @Inject constructor(
  private val getChannelCaptionUseCase: GetChannelCaptionUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getChannelValueStringUseCase: GetChannelValueStringUseCase,
) : CreateListItemUpdateEventDataUseCase.Mapper {

  override fun handle(item: Any): Boolean {
    return (item as? ChannelWithChildren)?.channel?.isSwitch() == true
  }

  override fun map(item: Any): SlideableListItemData {
    val (channel) = guardLet(item as? ChannelWithChildren) {
      throw IllegalArgumentException("Expected Channel but got $item")
    }
    return toListItemData(channel)
  }

  private fun toListItemData(channelWithChildren: ChannelWithChildren): SlideableListItemData.Default {
    val channelData = channelWithChildren.channel
    val value: String? = channelWithChildren.children.firstOrNull()?.let {
      getChannelValueStringUseCase.valueOrNull(it.channelDataEntity)
    }

    return SlideableListItemData.Default(
      online = channelData.channelValueEntity.online,
      titleProvider = getChannelCaptionUseCase(channelData),
      icon = getChannelIconUseCase.invoke(channelData),
      value = value,
      issueIconType = null,
      estimatedTimerEndDate = channelData.channelExtendedValueEntity?.getSuplaValue()?.TimerStateValue?.countdownEndsAt,
      infoSupported = SuplaChannelFlag.CHANNEL_STATE.inside(channelData.flags)
    )
  }
}
