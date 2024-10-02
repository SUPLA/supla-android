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

sealed interface SuplaClientEvent {
  data object Initialized : SuplaClientEvent
  data object NoAccount : SuplaClientEvent
  data object Connecting : SuplaClientEvent
  data object Connected : SuplaClientEvent
  data object Lock : SuplaClientEvent
  data object Unlock : SuplaClientEvent
  data object OnStart : SuplaClientEvent
  data object NetworkConnected : SuplaClientEvent
  data object AddWizardFinished : SuplaClientEvent
  data object AddWizardStopped : SuplaClientEvent

  data class Cancel(val reason: SuplaClientState.Reason? = null) : SuplaClientEvent
  data class Finish(val reason: SuplaClientState.Reason? = null) : SuplaClientEvent
  data class Error(val reason: SuplaClientState.Reason) : SuplaClientEvent
}

sealed class IllegalEvent(message: String) : IllegalStateException(message) {
  class IllegalInitializedEvent(message: String) : IllegalEvent(message)
  class IllegalNoAccountEvent(message: String) : IllegalEvent(message)
  class IllegalConnectingEvent(message: String) : IllegalEvent(message)
  class IllegalConnectedEvent(message: String) : IllegalEvent(message)
  class IllegalCancelEvent(message: String) : IllegalEvent(message)
  class IllegalLockEvent(message: String) : IllegalEvent(message)
  class IllegalUnlockEvent(message: String) : IllegalEvent(message)
  class IllegalFinishEvent(message: String) : IllegalEvent(message)
  class IllegalErrorEvent(message: String) : IllegalEvent(message)
  class IllegalAddWizardFinishedEvent(message: String) : IllegalEvent(message)
  class IllegalAddWizardStoppedEvent(message: String) : IllegalEvent(message)
}
