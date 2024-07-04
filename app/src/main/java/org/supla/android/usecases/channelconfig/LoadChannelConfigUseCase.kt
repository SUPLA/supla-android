package org.supla.android.usecases.channelconfig
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

import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.SuplaChannelConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadChannelConfigUseCase @Inject constructor(
  private val channelConfigRepository: ChannelConfigRepository,
  private val channelRepository: RoomChannelRepository
) {

  operator fun invoke(profileId: Long, remoteId: Int): Single<SuplaChannelConfig> {
    return channelRepository.findByRemoteId(profileId, remoteId)
      .map(ChannelConfigType::from)
      .flatMapSingle { channelConfigRepository.findChannelConfig(profileId, remoteId, it) }
      .toSingle()
  }
}
