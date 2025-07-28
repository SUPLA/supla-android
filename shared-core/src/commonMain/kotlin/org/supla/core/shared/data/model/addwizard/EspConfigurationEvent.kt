package org.supla.core.shared.data.model.addwizard
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

sealed interface EspConfigurationEvent {
  data object Start : EspConfigurationEvent

  data object Close : EspConfigurationEvent // Used to cancel and close the add wizard
  data object Cancel : EspConfigurationEvent // Used to cancel and restart process staying on the configuration screen
  data object Back : EspConfigurationEvent // Used to cancel and go back to network selection screen
  data object Canceled : EspConfigurationEvent

  data object RegistrationDisabled : EspConfigurationEvent
  data object RegistrationEnabled : EspConfigurationEvent
  data object RegistrationUnknown : EspConfigurationEvent

  data object Authorized : EspConfigurationEvent
  // when not authorized state machine is recreated

  data object RegistrationActivated : EspConfigurationEvent
  data object RegistrationNotActivated : EspConfigurationEvent

  data class NetworkFound(val ssid: String) : EspConfigurationEvent
  data class MultipleNetworksFound(val ssids: List<String>) : EspConfigurationEvent
  data class NetworkScanDisabled(val cached: List<String>) : EspConfigurationEvent
  data object NetworkNotFound : EspConfigurationEvent

  data object NetworkConnected : EspConfigurationEvent
  data object NetworkConnectionFailure : EspConfigurationEvent

  data object EspConfigured : EspConfigurationEvent
  data object SetupNeeded : EspConfigurationEvent
  data object CredentialsNeeded : EspConfigurationEvent
  data class EspConfigurationFailure(val error: EspConfigurationError) : EspConfigurationEvent
  data object PasswordProvided : EspConfigurationEvent

  data object Reconnected : EspConfigurationEvent
  data object ReconnectTimeout : EspConfigurationEvent
}
