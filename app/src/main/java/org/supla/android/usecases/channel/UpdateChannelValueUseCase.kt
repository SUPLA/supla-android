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

import io.reactivex.rxjava3.core.Single
import org.supla.android.Trace
import org.supla.android.data.model.general.EntityUpdateResult
import org.supla.android.data.source.ChannelValueRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaChannel
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaChannelValueUpdate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChannelValueUseCase @Inject constructor(
  private val profileRepository: RoomProfileRepository,
  private val channelValueRepository: ChannelValueRepository
) {

  operator fun invoke(suplaChannelValueUpdate: SuplaChannelValueUpdate): Single<EntityUpdateResult> =
    update(suplaChannelValueUpdate.Value, suplaChannelValueUpdate.Id, suplaChannelValueUpdate.OnLine)

  operator fun invoke(suplaChannel: SuplaChannel): Single<EntityUpdateResult> =
    update(suplaChannel.Value, suplaChannel.Id, suplaChannel.OnLine)

  private fun update(suplaChannelValue: SuplaChannelValue, channelRemoteId: Int, online: Boolean): Single<EntityUpdateResult> =
    channelValueRepository.findByRemoteId(channelRemoteId)
      .toSingle()
      .flatMap { channelValueEntity ->
        if (channelValueEntity.differsFrom(suplaChannelValue, online)) {
          Trace.d(TAG, "Updating channel value for $channelRemoteId subtype ${suplaChannelValue.SubValueType}")
          update(channelValueEntity, suplaChannelValue, online)
        } else {
          Single.just(EntityUpdateResult.NOP)
        }
      }.onErrorResumeNext { throwable ->
        if (throwable is NoSuchElementException) {
          Trace.d(TAG, "Inserting channel value for $channelRemoteId subtype ${suplaChannelValue.SubValueType}")
          insert(suplaChannelValue, channelRemoteId, online)
        } else {
          Trace.e(TAG, "Channel value update failed!", throwable)
          Single.just(EntityUpdateResult.ERROR)
        }
      }

  private fun update(channelValueEntity: ChannelValueEntity, suplaChannelValue: SuplaChannelValue, online: Boolean) =
    channelValueRepository
      .update(channelValueEntity.updatedBy(suplaChannelValue, online))
      .andThen(Single.just(EntityUpdateResult.UPDATED))

  private fun insert(suplaChannelValue: SuplaChannelValue, channelRemoteId: Int, online: Boolean) =
    profileRepository.findActiveProfile()
      .flatMapCompletable {
        channelValueRepository.insert(ChannelValueEntity.from(suplaChannelValue, channelRemoteId, online, it.id!!))
      }
      .andThen(Single.just(EntityUpdateResult.UPDATED))
}
