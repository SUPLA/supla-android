package org.supla.android.usecases.group
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

import io.reactivex.rxjava3.core.Observable
import org.supla.android.core.shared.shareable
import org.supla.android.data.model.Quadruple
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelFlag
import org.supla.android.ui.lists.onlineState
import org.supla.android.ui.lists.sensordata.RelatedChannelData
import org.supla.android.usecases.channel.GetChannelChildrenTreeUseCase
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.group.totalvalue.DimmerAndRgbGroupValue
import org.supla.android.usecases.group.totalvalue.DimmerGroupValue
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.OpenedClosedGroupValue
import org.supla.android.usecases.group.totalvalue.RgbGroupValue
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.usecase.GetCaptionUseCase
import java.util.LinkedList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadGroupWithChannelsUseCase @Inject constructor(
  private val channelGroupRepository: ChannelGroupRepository,
  private val channelRepository: RoomChannelRepository,
  private val channelRelationRepository: ChannelRelationRepository,
  private val channelGroupRelationRepository: ChannelGroupRelationRepository,
  private val getChannelChildrenTreeUseCase: GetChannelChildrenTreeUseCase
) {
  operator fun invoke(remoteId: Int): Observable<GroupWithChannels> =
    Observable.combineLatest(
      channelGroupRepository.findGroupDataEntity(remoteId),
      channelRepository.findObservableList(),
      channelRelationRepository.findChildrenToParentsRelations(),
      channelGroupRelationRepository.findGroupRelations(remoteId)
    ) { group, allChannels, childrenToParents, groupRelations ->
      Quadruple(group, allChannels, childrenToParents, groupRelations)
    }
      .map { (group, allChannels, childrenToParents, groupRelations) ->
        val channelsMap = mutableMapOf<Int, ChannelDataEntity>().also { map -> allChannels.forEach { map[it.remoteId] = it } }
        val channels = groupRelations
          .map { groupRelation ->
            val channel = allChannels.firstOrNull { channel -> channel.remoteId == groupRelation.channelId }
            val childrenList = LinkedList<Int>()
            val children = getChannelChildrenTreeUseCase(groupRelation.channelId, childrenToParents, channelsMap, childrenList)

            if (channel == null) {
              ChannelInGroup.Invisible
            } else {
              ChannelInGroup.Visible(ChannelWithChildren(channel, children))
            }
          }

        GroupWithChannels(group, channels)
      }
}

data class GroupWithChannels(
  val group: ChannelGroupDataEntity,
  val channels: List<ChannelInGroup>
) {
  fun aggregatedState(policy: Policy = Policy.OnOff): ChannelState.Value? =
    group.channelGroupEntity.groupTotalValues
      .map { policy.map(it) }
      .fold(null) { acc, value ->
        when (acc) {
          null -> value
          value -> acc
          else -> ChannelState.Value.NOT_USED
        }
      }

  sealed interface Policy {
    fun map(value: GroupValue): ChannelState.Value

    object OnOff : Policy {
      override fun map(value: GroupValue): ChannelState.Value =
        (value as? OpenedClosedGroupValue)?.active?.ifTrue { ChannelState.Value.ON } ?: ChannelState.Value.OFF
    }

    object OpenClosed : Policy {
      override fun map(value: GroupValue): ChannelState.Value =
        (value as? OpenedClosedGroupValue)?.active?.ifTrue { ChannelState.Value.CLOSED } ?: ChannelState.Value.OPEN
    }

    object Dimmer : Policy {
      override fun map(value: GroupValue): ChannelState.Value =
        when (value) {
          is DimmerGroupValue -> if (value.brightness == 0) ChannelState.Value.OFF else ChannelState.Value.ON
          is DimmerAndRgbGroupValue -> if (value.brightness == 0) ChannelState.Value.OFF else ChannelState.Value.ON
          else -> ChannelState.Value.OFF
        }
    }

    object Rgb : Policy {
      override fun map(value: GroupValue): ChannelState.Value =
        when (value) {
          is RgbGroupValue -> if (value.brightness == 0) ChannelState.Value.OFF else ChannelState.Value.ON
          is DimmerAndRgbGroupValue -> if (value.brightnessColor == 0) ChannelState.Value.OFF else ChannelState.Value.ON
          else -> ChannelState.Value.OFF
        }
    }
  }
}

sealed interface ChannelInGroup {
  val function: SuplaFunction

  data object Invisible : ChannelInGroup {
    override val function: SuplaFunction = SuplaFunction.UNKNOWN
  }

  data class Visible(val channelWithChildren: ChannelWithChildren) : ChannelInGroup {
    override val function: SuplaFunction = channelWithChildren.function
  }
}

interface ChannelGroupRelationDataEntityConvertible {
  val getChannelStateUseCase: GetChannelStateUseCase
  val getChannelIconUseCase: GetChannelIconUseCase
  val getCaptionUseCase: GetCaptionUseCase

  val GroupWithChannels.relatedChannelData: List<RelatedChannelData>
    get() = channels.map {
      when (it) {
        ChannelInGroup.Invisible -> RelatedChannelData.Invisible
        is ChannelInGroup.Visible -> RelatedChannelData.Visible(
          channelId = it.channelWithChildren.remoteId,
          profileId = it.channelWithChildren.profileId,
          onlineState = it.channelWithChildren.status.onlineState,
          icon = getChannelIconUseCase.forState(it.channelWithChildren, getChannelStateUseCase(it.channelWithChildren)),
          caption = getCaptionUseCase(it.channelWithChildren.channel.shareable),
          userCaption = it.channelWithChildren.caption,
          batteryIcon = null,
          showChannelStateIcon = SuplaChannelFlag.CHANNEL_STATE inside it.channelWithChildren.flags &&
            it.channelWithChildren.channel.stateEntity != null
        )
      }
    }
}
