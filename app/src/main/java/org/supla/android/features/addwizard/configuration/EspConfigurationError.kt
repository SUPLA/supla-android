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

import androidx.annotation.DrawableRes
import org.supla.android.R
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

sealed class EspConfigurationError(
  val message: LocalizedString,
  @DrawableRes val iconRes: Int = R.drawable.add_wizard_error
) {
  data object RegistrationCheck : EspConfigurationError(message = localizedString(R.string.device_reg_request_timeout))
  data object RegistrationEnable : EspConfigurationError(message = localizedString(R.string.enabling_registration_timeout))
  data object Scan : EspConfigurationError(message = localizedString(R.string.wizard_scan_timeout))
  data object NotFound : EspConfigurationError(message = localizedString(R.string.wizard_iodevice_notfound))
  data object Connect : EspConfigurationError(message = localizedString(R.string.wizard_connect_timeout))
  data object ConfigureTimeout : EspConfigurationError(message = localizedString(R.string.wizard_configure_timeout))
  data object Wifi : EspConfigurationError(message = localizedString(R.string.wizard_wifi_error))
  data object Compatibility : EspConfigurationError(message = localizedString(R.string.wizard_result_compat_error))
  data object Communication : EspConfigurationError(message = localizedString(R.string.wizard_result_conn_error))
  data object Configuration : EspConfigurationError(message = localizedString(R.string.wizard_result_failed))
  data object Reconnect : EspConfigurationError(message = localizedString(R.string.wizard_reconnect_timeout))
}
