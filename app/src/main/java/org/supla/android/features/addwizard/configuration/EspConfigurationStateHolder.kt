package org.supla.android.features.addwizard.configuration
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

import org.supla.android.Trace
import org.supla.android.extensions.TAG

interface EspConfigurationController {
  fun checkRegistration()
  fun authorize()
  fun activateRegistration()
  fun findEspNetwork()
  fun showNetworkSelector(ssids: List<String>, cached: Boolean)
  fun connectToNetwork(ssid: String)
  fun configureEsp()
  fun reconnect()
  fun showFinished()
  fun showError(error: EspConfigurationError)
  fun cancel()
  fun close()
}

class EspConfigurationStateHolder(espConfigurationController: EspConfigurationController) {

  val isIdle: Boolean
    get() = state is Idle

  val isInactive: Boolean
    get() = when (state) {
      is Idle, Finished, Canceled, ConfigurationFailure -> true
      else -> false
    }

  private var state: EspConfigurationState = Idle(espConfigurationController)

  fun handleEvent(event: EspConfigurationEvent) {
    synchronized(this) {
      Trace.i(TAG, "Handling event `$event` by state `$state`")
      state = state.handle(event)
      Trace.i(TAG, "Event handled, new state `$state`")
    }
  }
}
