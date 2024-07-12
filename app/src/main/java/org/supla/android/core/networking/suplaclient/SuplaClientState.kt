package org.supla.android.core.networking.suplaclient
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

import org.supla.android.lib.SuplaConnError
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED
import org.supla.android.lib.SuplaConst.SUPLA_RESULTCODE_REGISTRATION_DISABLED
import org.supla.android.lib.SuplaRegisterError

sealed interface SuplaClientState {

  fun nextState(event: SuplaClientEvent): SuplaClientState?

  data object Initialization : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.OnStart,
        SuplaClientEvent.NetworkConnected -> null

        SuplaClientEvent.Lock -> Locked
        SuplaClientEvent.Initialized -> Connecting()
        SuplaClientEvent.NoAccount -> FirstProfileCreation
        else -> {
          throw IllegalStateException("Unexpected event in Initialization: $event")
        }
      }
    }
  }

  data object Locked : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.Lock,
        SuplaClientEvent.OnStart,
        SuplaClientEvent.NetworkConnected,
        is SuplaClientEvent.Finish -> null

        SuplaClientEvent.Unlock -> Connecting()
        SuplaClientEvent.NoAccount -> FirstProfileCreation

        // others which should not occur
        SuplaClientEvent.Cancel -> throw IllegalEvent.IllegalCancelEvent("Unexpected event in Locked")
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in Locked")
        SuplaClientEvent.Connecting -> throw IllegalEvent.IllegalConnectingEvent("Unexpected event in Locked")
        is SuplaClientEvent.Error -> throw IllegalEvent.IllegalErrorEvent("Unexpected event in Locked")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in Locked")
      }
    }
  }

  data object FirstProfileCreation : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.Connecting -> Connecting()
        SuplaClientEvent.OnStart,
        SuplaClientEvent.NetworkConnected -> null

        // others which should not occur
        SuplaClientEvent.Cancel -> throw IllegalEvent.IllegalCancelEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in FirstProfileCreation")
        is SuplaClientEvent.Error -> throw IllegalEvent.IllegalErrorEvent("Unexpected event in FirstProfileCreation")
        is SuplaClientEvent.Finish -> throw IllegalEvent.IllegalFinishEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.Lock -> throw IllegalEvent.IllegalLockEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in FirstProfileCreation")
      }
    }
  }

  data class Connecting(val reason: Reason? = null) : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.Connected -> Connected
        SuplaClientEvent.Connecting,
        SuplaClientEvent.Initialized,
        SuplaClientEvent.OnStart -> null

        SuplaClientEvent.Lock -> Locked
        SuplaClientEvent.Cancel -> Disconnecting
        SuplaClientEvent.NetworkConnected -> Connecting()
        is SuplaClientEvent.Error -> Connecting(event.reason)
        is SuplaClientEvent.Finish -> Finished(event.reason ?: reason)

        // others which should not occur
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Connecting")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Connecting")
      }
    }
  }

  data object Connected : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.OnStart,
        SuplaClientEvent.NetworkConnected -> null

        SuplaClientEvent.Connecting -> Connecting()
        SuplaClientEvent.Lock -> Locked
        SuplaClientEvent.Cancel -> Disconnecting
        is SuplaClientEvent.Finish -> Finished(event.reason)
        is SuplaClientEvent.Error -> Finished(event.reason)

        // others which should not occur
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in Connected")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in Connected")
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Connected")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Connected")
      }
    }
  }

  data object Disconnecting : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.OnStart,
        SuplaClientEvent.Cancel,
        SuplaClientEvent.Connecting,
        SuplaClientEvent.Connected,
        SuplaClientEvent.NetworkConnected,
        SuplaClientEvent.Initialized -> null

        SuplaClientEvent.Lock -> Locking
        is SuplaClientEvent.Finish -> Finished(event.reason)
        is SuplaClientEvent.Error -> Finished(event.reason)

        // others which should not occur
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Disconnecting")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Disconnecting")
      }
    }
  }

  data object Locking : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.OnStart,
        SuplaClientEvent.Lock,
        SuplaClientEvent.Cancel,
        SuplaClientEvent.NetworkConnected -> null

        is SuplaClientEvent.Finish -> Locked

        // others which should not occur
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in Locking")
        SuplaClientEvent.Connecting -> throw IllegalEvent.IllegalConnectingEvent("Unexpected event in Locking")
        is SuplaClientEvent.Error -> throw IllegalEvent.IllegalErrorEvent("Unexpected event in Locking")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in Locking")
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Locking")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Locking")
      }
    }
  }

  data class Finished(val reason: Reason? = null) : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.Cancel,
        SuplaClientEvent.NetworkConnected -> null

        SuplaClientEvent.Initialized -> Connecting()
        SuplaClientEvent.Connecting -> Connecting()
        SuplaClientEvent.Lock -> Locked
        SuplaClientEvent.OnStart -> if (reason is Reason.NoNetwork) Connecting(reason) else Connecting()
        SuplaClientEvent.NoAccount -> FirstProfileCreation
        is SuplaClientEvent.Error -> if (event.reason != reason) Finished(event.reason) else null
        is SuplaClientEvent.Finish -> if (event.reason != reason) Finished(event.reason ?: reason) else null

        // others which should not occur
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in Finished")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Finished")
      }
    }
  }

  sealed interface Reason {
    data class ConnectionError(val error: SuplaConnError) : Reason

    data class RegisterError(val error: SuplaRegisterError) : Reason {
      fun shouldAuthorize() =
        error.ResultCode == SUPLA_RESULTCODE_REGISTRATION_DISABLED ||
          error.ResultCode == SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED
    }

    data object VersionError : Reason

    data object NoNetwork : Reason

    data object AppInBackground : Reason
  }
}
