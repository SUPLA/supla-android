package org.supla.android.usecases.channel
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

import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.core.shared.data.model.general.SuplaFunction
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelsTreeSnapshotFactory @Inject constructor(
  private val getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase
) {

  fun create(
    channels: List<ChannelDataEntity>,
    relationMap: Map<Int, List<ChannelRelationEntity>>
  ): ChannelsTreeSnapshot {
    val channelsById = channels.associateBy { it.remoteId }
    val childrenByParentId = mutableMapOf<Int, List<ChannelChildEntity>>()

    val channelsWithChildren = channels.associate { channel ->
      channel.remoteId to ChannelWithChildren(
        channel = channel,
        children = getChannelChildrenTreeUseCase(
          channel.remoteId,
          relationMap,
          channelsById,
          LinkedList(),
          childrenByParentId
        )
      )
    }

    return ChannelsTreeSnapshot(
      channels = channels,
      channelsWithChildren = channelsWithChildren,
      childChannelIds = relationMap.flatMap { (parentId, relations) ->
        val parentFunction = channelsById[parentId]?.function
        relations
          .filter { relation ->
            val child = channelsById[relation.channelId]
            child?.function != SuplaFunction.ACTION_TRIGGER || (parentFunction != null && parentFunction in ACTION_TRIGGER_PARENT_FUNCTIONS)
          }
          .map { it.channelId }
      }.toSet()
    )
  }

  private companion object {
    val ACTION_TRIGGER_PARENT_FUNCTIONS = setOf(
      SuplaFunction.DIMMER,
      SuplaFunction.DIMMER_CCT,
      SuplaFunction.DIMMER_AND_RGB_LIGHTING,
      SuplaFunction.DIMMER_CCT_AND_RGB,
      SuplaFunction.RGB_LIGHTING,
      SuplaFunction.CONTROLLING_THE_ROLLER_SHUTTER,
      SuplaFunction.CONTROLLING_THE_ROOF_WINDOW,
      SuplaFunction.CONTROLLING_THE_FACADE_BLIND,
      SuplaFunction.TERRACE_AWNING,
      SuplaFunction.PROJECTOR_SCREEN,
      SuplaFunction.CURTAIN,
      SuplaFunction.VERTICAL_BLIND,
      SuplaFunction.ROLLER_GARAGE_DOOR,
      SuplaFunction.LIGHTSWITCH,
      SuplaFunction.POWER_SWITCH,
      SuplaFunction.STAIRCASE_TIMER,
      SuplaFunction.CONTROLLING_THE_GATE,
      SuplaFunction.CONTROLLING_THE_DOOR_LOCK,
      SuplaFunction.CONTROLLING_THE_GARAGE_DOOR,
      SuplaFunction.CONTROLLING_THE_GATEWAY_LOCK
    )
  }
}
