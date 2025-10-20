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

import io.reactivex.rxjava3.core.Single
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.core.shared.extensions.ifLet
import javax.inject.Inject
import javax.inject.Singleton

sealed interface TiltingDetails {
  data class Similar(
    val tilt0Angle: Int,
    val tilt100Angle: Int,
    val tiltControlType: SuplaTiltControlType
  ) : TiltingDetails

  object Different : TiltingDetails
  object Unknown : TiltingDetails
}

@Singleton
class ReadGroupTiltingDetailsUseCase @Inject constructor(
  private val groupRelationRepository: ChannelGroupRelationRepository,
  private val channelConfigRepository: ChannelConfigRepository
) {

  operator fun invoke(remoteId: Int): Single<TiltingDetails> {
    return groupRelationRepository.findGroupRelationsData(remoteId)
      .firstOrError()
      .flatMap { relations ->
        if (relations.isEmpty()) {
          return@flatMap Single.just(TiltingDetails.Unknown)
        }

        val channelConfigs = relations.map {
          channelConfigRepository.findChannelConfig(
            it.channelEntity.profileId,
            it.channelEntity.remoteId,
            ChannelConfigType.from(it.channelEntity)
          )
        }

        Single.zip(channelConfigs) { list ->
          list.fold(null as TiltingDetails?) { acc, any ->
            val details = any.toTiltingDetails()

            if (acc == TiltingDetails.Unknown || details == null) {
              TiltingDetails.Unknown
            } else if (acc == TiltingDetails.Different) {
              TiltingDetails.Different
            } else if (acc == null) {
              details
            } else if (acc != details) {
              TiltingDetails.Different
            } else {
              details
            }
          } ?: TiltingDetails.Unknown
        }
      }
  }

  private fun Any.toTiltingDetails(): TiltingDetails? {
    ifLet(this as? SuplaChannelFacadeBlindConfig) { (config) ->
      return TiltingDetails.Similar(config.tilt0Angle, config.tilt100Angle, config.type)
    }

    return null
  }
}
