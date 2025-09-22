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

import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId
import org.supla.core.shared.infrastructure.localizedString

sealed class EspConfigurationError(
  open val messages: List<LocalizedString>
) {

  constructor(message: LocalizedString) : this(listOf(message))

  fun combine(error: EspConfigurationError): EspConfigurationError =
    Combined(
      mutableListOf<LocalizedString>().apply {
        addAll(messages)
        addAll(error.messages)
      }
    )

  data object RegistrationCheck : EspConfigurationError(message = localizedString(LocalizedStringId.DEVICE_REGISTRATION_REQUEST_TIMEOUT))
  data object RegistrationEnable : EspConfigurationError(message = localizedString(LocalizedStringId.ENABLING_REGISTRATION_TIMEOUT))
  data object Scan : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_SCAN_TIMEOUT))
  data object NotFound : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_DEVICE_NOT_FOUND))
  data object Connect : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_CONNECT_TIMEOUT))
  data object ConfigureTimeout : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_CONFIGURE_TIMEOUT))
  data object Wifi : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_WIFI_ERROR))
  data object Compatibility : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_RESULT_NOT_COMPATIBLE))
  data object Communication : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_RESULT_CONNECTION_ERROR))
  data object Configuration : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_RESULT_FAILED))
  data object Reconnect : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_RECONNECT_TIMEOUT))
  data class Combined(override val messages: List<LocalizedString>) : EspConfigurationError(messages)
  data object TemporarilyLocked : EspConfigurationError(message = localizedString(LocalizedStringId.ADD_WIZARD_DEVICE_TEMPORARILY_LOCKED))
}
