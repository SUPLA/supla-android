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
    private var initialized = false
    private var started = false

    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.OnStart -> {
          started = true
          if (initialized) Connecting() else null
        }
        SuplaClientEvent.NetworkConnected -> null

        SuplaClientEvent.Lock -> Locked
        SuplaClientEvent.Initialized -> {
          initialized = true
          if (started) Connecting() else null
        }
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
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in Locked")
        SuplaClientEvent.Connecting -> throw IllegalEvent.IllegalConnectingEvent("Unexpected event in Locked")
        is SuplaClientEvent.Cancel -> throw IllegalEvent.IllegalCancelEvent("Unexpected event in Locked")
        is SuplaClientEvent.Error -> throw IllegalEvent.IllegalErrorEvent("Unexpected event in Locked")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in Locked")
        SuplaClientEvent.AddWizardFinished -> throw IllegalEvent.IllegalAddWizardFinishedEvent("Unexpected event in Locked")
      }
    }
  }

  data object FirstProfileCreation : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.Connecting -> Connecting()
        SuplaClientEvent.OnStart,
        SuplaClientEvent.NetworkConnected,
        is SuplaClientEvent.Finish -> null

        // others which should not occur
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in FirstProfileCreation")
        is SuplaClientEvent.Cancel -> throw IllegalEvent.IllegalCancelEvent("Unexpected event in FirstProfileCreation")
        is SuplaClientEvent.Error -> throw IllegalEvent.IllegalErrorEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.Lock -> throw IllegalEvent.IllegalLockEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in FirstProfileCreation")
        SuplaClientEvent.AddWizardFinished -> throw IllegalEvent.IllegalAddWizardFinishedEvent("Unexpected event in FirstProfileCreation")
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
        SuplaClientEvent.NetworkConnected -> Connecting()
        is SuplaClientEvent.Cancel -> Disconnecting(event.reason)
        is SuplaClientEvent.Error -> Connecting(event.reason)
        is SuplaClientEvent.Finish -> Finished(event.reason ?: reason)

        // others which should not occur
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Connecting")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Connecting")
        SuplaClientEvent.AddWizardFinished -> throw IllegalEvent.IllegalAddWizardFinishedEvent("Unexpected event in Connecting")
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
        is SuplaClientEvent.Cancel -> Disconnecting(event.reason)
        is SuplaClientEvent.Finish -> Finished(event.reason)
        is SuplaClientEvent.Error -> Finished(event.reason)

        // others which should not occur
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in Connected")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in Connected")
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Connected")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Connected")
        SuplaClientEvent.AddWizardFinished -> throw IllegalEvent.IllegalAddWizardFinishedEvent("Unexpected event in Connected")
      }
    }
  }

  data class Disconnecting(val reason: Reason? = null) : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.OnStart,
        is SuplaClientEvent.Cancel,
        SuplaClientEvent.Connecting,
        SuplaClientEvent.Connected,
        SuplaClientEvent.NetworkConnected,
        SuplaClientEvent.Initialized -> null

        SuplaClientEvent.Lock -> Locking
        is SuplaClientEvent.Finish -> Finished(reason ?: event.reason)
        is SuplaClientEvent.Error -> Finished(if (reason == Reason.AddWizardStarted) reason else event.reason)

        // others which should not occur
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Disconnecting")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Disconnecting")
        SuplaClientEvent.AddWizardFinished -> throw IllegalEvent.IllegalAddWizardFinishedEvent("Unexpected event in Disconnecting")
      }
    }
  }

  data object Locking : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        SuplaClientEvent.OnStart,
        SuplaClientEvent.Lock,
        is SuplaClientEvent.Cancel,
        SuplaClientEvent.NetworkConnected -> null

        is SuplaClientEvent.Finish -> Locked

        // others which should not occur
        SuplaClientEvent.Connected -> throw IllegalEvent.IllegalConnectedEvent("Unexpected event in Locking")
        SuplaClientEvent.Connecting -> throw IllegalEvent.IllegalConnectingEvent("Unexpected event in Locking")
        is SuplaClientEvent.Error -> throw IllegalEvent.IllegalErrorEvent("Unexpected event in Locking")
        SuplaClientEvent.Initialized -> throw IllegalEvent.IllegalInitializedEvent("Unexpected event in Locking")
        SuplaClientEvent.NoAccount -> throw IllegalEvent.IllegalNoAccountEvent("Unexpected event in Locking")
        SuplaClientEvent.Unlock -> throw IllegalEvent.IllegalUnlockEvent("Unexpected event in Locking")
        SuplaClientEvent.AddWizardFinished -> throw IllegalEvent.IllegalAddWizardFinishedEvent("Unexpected event in Locking")
      }
    }
  }

  data class Finished(val reason: Reason? = null) : SuplaClientState {
    override fun nextState(event: SuplaClientEvent): SuplaClientState? {
      return when (event) {
        is SuplaClientEvent.Cancel,
        SuplaClientEvent.NetworkConnected -> null

        SuplaClientEvent.Initialized,
        SuplaClientEvent.AddWizardFinished -> Connecting()

        SuplaClientEvent.Connecting ->
          if (reason == Reason.AddWizardStarted) null else Connecting()

        SuplaClientEvent.Lock -> Locked
        SuplaClientEvent.OnStart ->
          when (reason) {
            Reason.AddWizardStarted -> null
            is Reason.NoNetwork -> Connecting(reason)
            else -> Connecting()
          }

        SuplaClientEvent.NoAccount -> FirstProfileCreation
        is SuplaClientEvent.Error ->
          if (event.reason != reason && reason != Reason.AddWizardStarted) Finished(event.reason) else null
        is SuplaClientEvent.Finish ->
          if (event.reason != reason && reason != Reason.AddWizardStarted) Finished(event.reason ?: reason) else null

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

    data object AddWizardStarted : Reason
  }
}
