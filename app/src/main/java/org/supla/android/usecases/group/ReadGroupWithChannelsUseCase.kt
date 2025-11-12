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
import org.supla.android.usecases.group.totalvalue.OpenedClosedGroupValue
import org.supla.android.usecases.icon.GetChannelIconUseCase
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
          .map {
            val channel = allChannels.first { channel -> channel.remoteId == it.channelId }
            val childrenList = LinkedList<Int>()
            val children = getChannelChildrenTreeUseCase(it.channelId, childrenToParents, channelsMap, childrenList)

            ChannelWithChildren(channel, children)
          }

        GroupWithChannels(group, channels)
      }
}

data class GroupWithChannels(
  val group: ChannelGroupDataEntity,
  val channels: List<ChannelWithChildren>
) {
  /**
   * Represents the aggregated state of all channels within the group
   *
   * @param activeValue value for active state
   * @param inactiveValue value for inactive state
   *
   * returns:
   * - activeValue if all channels have active status
   * - inactiveValue if all channels have inactive status
   * - `ChannelState.Value.NOT_USED` if there is a mixture of active and inactive channels
   * - null - if there is no channel in group
   */
  fun aggregatedState(activeValue: ChannelState.Value, inactiveValue: ChannelState.Value): ChannelState.Value? =
    group.channelGroupEntity.groupTotalValues
      .map { (it as? OpenedClosedGroupValue)?.active }
      .map {
        when (it) {
          true -> activeValue
          false -> inactiveValue
          null -> ChannelState.Value.NOT_USED
        }
      }
      .fold(null) { acc, value ->
        when (acc) {
          null -> value
          value -> acc
          else -> ChannelState.Value.NOT_USED
        }
      }
}

interface ChannelGroupRelationDataEntityConvertible {
  val getChannelStateUseCase: GetChannelStateUseCase
  val getChannelIconUseCase: GetChannelIconUseCase
  val getCaptionUseCase: GetCaptionUseCase

  val GroupWithChannels.relatedChannelData: List<RelatedChannelData>
    get() = channels.map {
      RelatedChannelData(
        channelId = it.remoteId,
        profileId = it.profileId,
        onlineState = it.status.onlineState,
        icon = getChannelIconUseCase.forState(it, getChannelStateUseCase(it)),
        caption = getCaptionUseCase(it.channel.shareable),
        userCaption = it.caption,
        batteryIcon = null,
        showChannelStateIcon = SuplaChannelFlag.CHANNEL_STATE inside it.flags && it.channel.stateEntity != null
      )
    }
}
