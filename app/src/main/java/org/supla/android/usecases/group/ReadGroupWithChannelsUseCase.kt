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
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupRelationDataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadGroupWithChannelsUseCase @Inject constructor(
  private val channelGroupRepository: ChannelGroupRepository,
  private val channelGroupRelationRepository: ChannelGroupRelationRepository
) {
  operator fun invoke(remoteId: Int): Observable<GroupWithChannels> =
    channelGroupRepository.findGroupDataEntity(remoteId)
      .flatMap { group ->
        channelGroupRelationRepository.findGroupRelations(remoteId)
          .map { GroupWithChannels(group, it) }
      }
}

data class GroupWithChannels(
  val group: ChannelGroupDataEntity,
  val channels: List<ChannelGroupRelationDataEntity>
)
