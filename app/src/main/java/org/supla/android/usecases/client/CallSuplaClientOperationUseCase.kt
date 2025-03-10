package org.supla.android.usecases.client
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
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.tools.VibrationHelper
import org.supla.core.shared.data.model.general.SuplaCallConfigCommand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class created to charm away magic numbers in [org.supla.android.lib.SuplaClient] calls.
 */
@Singleton
class CallSuplaClientOperationUseCase @Inject constructor(
  private val suplaClientProvider: SuplaClientProvider,
  private val vibrationHelper: VibrationHelper
) {

  operator fun invoke(remoteId: Int, type: ItemType, operation: SuplaClientOperation): Completable =
    Completable.fromRunnable {
      suplaClientProvider.provide()?.run {
        when (operation) {
          is SuplaClientOperation.MoveUp -> open(remoteId, type.isGroup(), 2)
          is SuplaClientOperation.MoveDown -> open(remoteId, type.isGroup(), 1)
          is SuplaClientOperation.Command -> deviceCalCfgRequest(remoteId, type.isGroup(), operation.command.value, 0, null)
        }.let { success ->
          if (success) {
            vibrationHelper.vibrate()
          }
        }
      }
    }
}

sealed interface SuplaClientOperation {

  // We need to introduce support for the "ACTION_UP" actions,
  // which are partially supported on the server side by "rsActionUp".
  // https://github.com/SUPLA/supla-core/blob/5958a128fcd04db30482863d8809a7ace15b0bb8/supla-server/src/device/devicechannels.cpp#L1006
  object MoveUp : SuplaClientOperation

  // We need to introduce support for the "ACTION_DOWN" actions,
  // which are partially supported on the server side by "rsActionDown".
  // https://github.com/SUPLA/supla-core/blob/5958a128fcd04db30482863d8809a7ace15b0bb8/supla-server/src/device/devicechannels.cpp#L1006
  object MoveDown : SuplaClientOperation

  sealed interface Command : SuplaClientOperation {

    val command: SuplaCallConfigCommand

    data object Recalibrate : Command {
      override val command: SuplaCallConfigCommand
        get() = SuplaCallConfigCommand.RECALIBRATE
    }

    data object ZwaveGetNodeList : Command {
      override val command: SuplaCallConfigCommand
        get() = SuplaCallConfigCommand.ZWAVE_GET_NODE_LIST
    }

    data object MuteAlarmSound : Command {
      override val command: SuplaCallConfigCommand
        get() = SuplaCallConfigCommand.MUTE_ALARM_SOUND
    }
  }
}
