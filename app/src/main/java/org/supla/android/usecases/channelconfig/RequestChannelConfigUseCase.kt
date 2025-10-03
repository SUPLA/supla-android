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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.lib.SuplaChannel
import org.supla.core.shared.data.model.general.SuplaFunction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestChannelConfigUseCase @Inject constructor(
  private val channelConfigRepository: ChannelConfigRepository,
  private val suplaClientProvider: SuplaClientProvider
) {

  operator fun invoke(suplaChannel: SuplaChannel): Completable =
    if (shouldObserveChannelConfig(suplaChannel)) {
      channelConfigRepository.findForRemoteId(suplaChannel.Id)
        .toSingle()
        .doOnSuccess { config ->
          Timber.i("Channel config found (remoteId: `${suplaChannel.Id}`)")
          if (config.configCrc32 != suplaChannel.DefaultConfigCRC32) {
            Timber.i("Channel config asked (remoteId: `${suplaChannel.Id}`)")
            suplaClientProvider.provide()?.getChannelConfig(suplaChannel.Id, ChannelConfigType.DEFAULT)
          }
        }
        .ignoreElement()
        .onErrorResumeNext {
          Completable.fromRunnable {
            if (it is NoSuchElementException) {
              Timber.i("Channel config not found (remoteId: `${suplaChannel.Id}`)")
              suplaClientProvider.provide()?.getChannelConfig(suplaChannel.Id, ChannelConfigType.DEFAULT)
            }
          }
        }
    } else {
      Completable.complete()
    }

  private fun shouldObserveChannelConfig(suplaChannel: SuplaChannel) =
    suplaChannel.Func == SuplaFunction.GENERAL_PURPOSE_METER.value ||
      suplaChannel.Func == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT.value ||
      suplaChannel.Func == SuplaFunction.CONTROLLING_THE_FACADE_BLIND.value ||
      suplaChannel.Func == SuplaFunction.VERTICAL_BLIND.value ||
      suplaChannel.Func == SuplaFunction.CONTAINER.value ||
      suplaChannel.Func == SuplaFunction.WATER_TANK.value ||
      suplaChannel.Func == SuplaFunction.SEPTIC_TANK.value
}
