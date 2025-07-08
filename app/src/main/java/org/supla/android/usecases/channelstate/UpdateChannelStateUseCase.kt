package org.supla.android.usecases.channelstate
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

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import org.supla.android.Trace
import org.supla.android.data.source.ChannelStateRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.extensions.TAG
import org.supla.android.lib.SuplaChannelState
import org.supla.android.lib.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChannelStateUseCase @Inject constructor(
  private val profileRepository: RoomProfileRepository,
  private val channelStateRepository: ChannelStateRepository
) {

  operator fun invoke(state: SuplaChannelState?): Completable =
    if (state != null) {
      channelStateRepository.getState(state.channelId)
        .map { it.updateBy(state) }
        .switchIfEmpty(create(state))
        .flatMapCompletable { channelStateRepository.insertOrUpdate(it) }
        .onErrorComplete {
          Trace.e(TAG, "Could not stare channel state", it)
          true
        }
    } else {
      Completable.complete()
    }

  private fun create(state: SuplaChannelState): Maybe<ChannelStateEntity> =
    profileRepository.findActiveProfile()
      .map { state.toEntity(it.id!!) }
      .toMaybe()
}

private fun ChannelStateEntity.updateBy(state: SuplaChannelState): ChannelStateEntity =
  copy(
    batteryHealth = state.batteryHealth,
    batteryLevel = state.batteryLevel,
    batteryPowered = state.batteryPowered,
    bridgeNodeOnline = state.bridgeNodeOnline,
    bridgeNodeSignalStrength = state.bridgeNodeSignalStrength,
    connectionUptime = state.connectionUptime,
    ipV4 = state.ipV4,
    lastConnectionResetCause = state.lastConnectionResetCause,
    lightSourceLifespan = state.lightSourceLifespan,
    lightSourceLifespanLeft = state.lightSourceLifespanLeft,
    lightSourceOperatingTime = state.lightSourceOperatingTime,
    macAddress = state.macAddress,
    uptime = state.uptime,
    wifiRssi = state.wifiRssi,
    wifiSignalStrength = state.wifiSignalStrength
  )
