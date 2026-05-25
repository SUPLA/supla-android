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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx3.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.lib.SuplaChannelGroup
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChannelGroupUseCase @Inject constructor(
  private val locationRepository: LocationRepository,
  private val channelGroupRepository: ChannelGroupRepository,
  private val profileRepository: ProfileRepository
) {

  operator fun invoke(suplaChannelGroup: SuplaChannelGroup): Boolean =
    runBlocking {
      withContext(Dispatchers.IO) {
        updateChannelGroup(suplaChannelGroup)
      }
    }

  private suspend fun updateChannelGroup(suplaChannelGroup: SuplaChannelGroup): Boolean {
    val location = locationRepository.findByRemoteId(suplaChannelGroup.LocationID).awaitSingleOrNull()
      ?: return false

    val channelGroup = channelGroupRepository.findByRemoteId(suplaChannelGroup.Id).awaitSingleOrNull()
    return if (channelGroup == null) {
      insertChannelGroup(location, suplaChannelGroup)
    } else if (channelGroup.differsFrom(suplaChannelGroup) || channelGroup.visible != 1) {
      val position = if (channelGroup.locationId != suplaChannelGroup.LocationID) {
        nextPosition(location.remoteId)
      } else {
        channelGroup.position
      }

      channelGroupRepository.updateEntity(channelGroup.updatedBy(suplaChannelGroup, position))
      true
    } else {
      false
    }
  }

  private suspend fun insertChannelGroup(location: LocationEntity, suplaChannelGroup: SuplaChannelGroup): Boolean {
    val profile = profileRepository.findActiveProfileKtx() ?: return false

    channelGroupRepository.insert(
      ChannelGroupEntity(
        id = null,
        remoteId = suplaChannelGroup.Id,
        caption = suplaChannelGroup.Caption,
        function = SuplaFunction.from(suplaChannelGroup.Func),
        online = suplaChannelGroup.status.rawValue,
        visible = 1,
        locationId = suplaChannelGroup.LocationID,
        altIcon = suplaChannelGroup.AltIcon,
        userIcon = suplaChannelGroup.UserIcon,
        flags = suplaChannelGroup.Flags,
        totalValue = null,
        position = nextPosition(location.remoteId),
        profileId = profile.id!!
      )
    )
    return true
  }

  private suspend fun nextPosition(locationRemoteId: Int): Int {
    val lastPosition = channelGroupRepository.findMaxPositionInLocation(locationRemoteId) ?: 0
    return if (lastPosition == 0) 0 else lastPosition + 1
  }

  private fun ChannelGroupEntity.updatedBy(
    suplaChannelGroup: SuplaChannelGroup,
    position: Int
  ): ChannelGroupEntity =
    copy(
      remoteId = suplaChannelGroup.Id,
      caption = suplaChannelGroup.Caption,
      function = SuplaFunction.from(suplaChannelGroup.Func),
      online = suplaChannelGroup.status.rawValue,
      visible = 1,
      locationId = suplaChannelGroup.LocationID,
      altIcon = suplaChannelGroup.AltIcon,
      userIcon = suplaChannelGroup.UserIcon,
      flags = suplaChannelGroup.Flags,
      position = position
    )

  private fun ChannelGroupEntity.differsFrom(suplaChannelGroup: SuplaChannelGroup): Boolean =
    remoteId != suplaChannelGroup.Id ||
      caption != suplaChannelGroup.Caption ||
      online != suplaChannelGroup.status.rawValue ||
      flags != suplaChannelGroup.Flags ||
      altIcon != suplaChannelGroup.AltIcon ||
      userIcon != suplaChannelGroup.UserIcon ||
      locationId != suplaChannelGroup.LocationID
}
