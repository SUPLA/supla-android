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

import androidx.room.rxjava3.EmptyResultSetException
import io.reactivex.rxjava3.core.Completable
import org.supla.android.Trace
import org.supla.android.data.source.ChannelConfigRepository
import org.supla.android.data.source.GeneralPurposeMeterLogRepository
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.android.data.source.remote.container.SuplaChannelContainerConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeasurementConfig
import org.supla.android.data.source.remote.gpm.SuplaChannelGeneralPurposeMeterConfig
import org.supla.android.data.source.remote.hvac.SuplaChannelHvacConfig
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.events.DownloadEventsManager
import org.supla.android.extensions.TAG
import org.supla.android.extensions.ifLet
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsertChannelConfigUseCase @Inject constructor(
  private val channelConfigRepository: ChannelConfigRepository,
  private val profileRepository: RoomProfileRepository,
  private val generalPurposeMeterLogRepository: GeneralPurposeMeterLogRepository,
  private val downloadEventsManager: DownloadEventsManager
) {

  operator fun invoke(config: SuplaChannelConfig?, result: ConfigResult): Completable {
    if (result != ConfigResult.RESULT_TRUE) {
      return Completable.complete()
    }

    ifLet(config as? SuplaChannelContainerConfig) { (config) ->
      return profileRepository.findActiveProfile().flatMapCompletable {
        Trace.d(TAG, "Saving config (remoteId: `${config.remoteId}`, function: `${config.func}`) - container")
        channelConfigRepository.insertOrUpdate(it.id!!, config)
      }
    }
    ifLet(config as? SuplaChannelHvacConfig) { (config) ->
      return profileRepository.findActiveProfile().flatMapCompletable {
        Trace.d(TAG, "Saving config (remoteId: `${config.remoteId}`, function: `${config.func}`) - hvac")
        channelConfigRepository.insertOrUpdate(it.id!!, config)
      }
    }
    ifLet(config as? SuplaChannelGeneralPurposeMeasurementConfig) { (config) ->
      return profileRepository.findActiveProfile().flatMapCompletable {
        Trace.d(TAG, "Saving config (remoteId: `${config.remoteId}`, function: `${config.func}`) - measurement")
        channelConfigRepository.insertOrUpdate(it.id!!, config)
      }
    }
    // Order is important as the SuplaChannelFacadeBlindConfig is child of SuplaChannelRollerShutterConfig
    ifLet(config as? SuplaChannelFacadeBlindConfig) { (config) ->
      return profileRepository.findActiveProfile().flatMapCompletable {
        Trace.d(TAG, "Saving config (remoteId: `${config.remoteId}`, function: `${config.func}`) - facade blind")
        channelConfigRepository.insertOrUpdate(it.id!!, config)
      }
    }
    ifLet(config as? SuplaChannelGeneralPurposeMeterConfig) { (config) ->
      return profileRepository.findActiveProfile().flatMapCompletable { profile ->
        Trace.d(TAG, "Saving config (remoteId: `${config.remoteId}`, function: `${config.func}`) - meter")
        channelConfigRepository.findChannelConfig(profile.id!!, config.remoteId, ChannelConfigType.GENERAL_PURPOSE_METER)
          .flatMapCompletable { oldConfig ->
            if (shouldCleanupHistory(oldConfig, config)) {
              Trace.d(TAG, "Cleaning history (remoteId: `${config.remoteId}`, function: `${config.func}`)")
              generalPurposeMeterLogRepository.delete(config.remoteId, profile.id)
                .andThen(
                  Completable.fromRunnable {
                    // After deleting emit refresh state, so opened detail view knows that history was removed and reload is needed.
                    downloadEventsManager.emitProgressState(config.remoteId, DownloadEventsManager.State.Refresh)
                  }
                )
            } else {
              Completable.complete()
            }
          }
          .andThen(channelConfigRepository.insertOrUpdate(profile.id, config))
          .onErrorResumeNext {
            if (it is EmptyResultSetException) {
              channelConfigRepository.insertOrUpdate(profile.id, config)
            } else {
              Completable.error(it)
            }
          }
      }
    }

    Trace.w(TAG, "Got config which cannot be stored (remoteId: `${config?.remoteId}`, function: `${config?.func}`)")

    ifLet(config) { (config) ->
      // if could not store try to delete
      if (shouldHandle(config)) {
        return profileRepository.findActiveProfile().flatMapCompletable {
          Trace.d(TAG, "Saving config (remoteId: `${config.remoteId}`, function: `${config.func}`)")
          channelConfigRepository.delete(it.id!!, config.remoteId)
        }
      }
    }

    return Completable.complete()
  }

  private fun shouldHandle(config: SuplaChannelConfig): Boolean =
    config.func == SuplaFunction.GENERAL_PURPOSE_MEASUREMENT.value ||
      config.func == SuplaFunction.GENERAL_PURPOSE_METER.value ||
      config.func == SuplaFunction.VERTICAL_BLIND.value ||
      config.func == SuplaFunction.CONTROLLING_THE_FACADE_BLIND.value ||
      config.func == SuplaFunction.CONTAINER.value ||
      config.func == SuplaFunction.WATER_TANK.value ||
      config.func == SuplaFunction.SEPTIC_TANK.value

  private fun shouldCleanupHistory(oldConfig: SuplaChannelConfig, newConfig: SuplaChannelGeneralPurposeMeterConfig): Boolean {
    if (oldConfig is SuplaChannelGeneralPurposeMeterConfig) {
      return oldConfig.counterType != newConfig.counterType || oldConfig.fillMissingData != newConfig.fillMissingData
    }

    return true
  }
}
