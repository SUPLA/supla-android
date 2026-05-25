package org.supla.android.usecases.channelrelation
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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.lib.SuplaChannelGroupRelation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChannelGroupRelationUseCase @Inject constructor(
  private val channelGroupRelationRepository: ChannelGroupRelationRepository,
  private val profileRepository: ProfileRepository
) {

  operator fun invoke(suplaChannelGroupRelation: SuplaChannelGroupRelation): Boolean =
    runBlocking {
      withContext(Dispatchers.IO) {
        updateChannelGroupRelation(suplaChannelGroupRelation)
      }
    }

  private suspend fun updateChannelGroupRelation(
    suplaChannelGroupRelation: SuplaChannelGroupRelation
  ): Boolean {
    val relation =
      channelGroupRelationRepository.findByGroupAndChannel(
        suplaChannelGroupRelation.ChannelGroupID,
        suplaChannelGroupRelation.ChannelID
      )

    return if (relation == null) {
      insertChannelGroupRelation(suplaChannelGroupRelation)
    } else if (relation.visible != 1) {
      channelGroupRelationRepository.updateEntity(relation.copy(visible = 1))
      true
    } else {
      false
    }
  }

  private suspend fun insertChannelGroupRelation(
    suplaChannelGroupRelation: SuplaChannelGroupRelation
  ): Boolean {
    val profile = profileRepository.findActiveProfileKtx() ?: return false

    channelGroupRelationRepository.insert(
      ChannelGroupRelationEntity(
        id = null,
        groupId = suplaChannelGroupRelation.ChannelGroupID,
        channelId = suplaChannelGroupRelation.ChannelID,
        visible = 1,
        profileId = profile.id!!
      )
    )
    return true
  }
}
