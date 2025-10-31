package org.supla.android.lib

import android.content.Context
import org.supla.android.R
import org.supla.android.core.shared.invoke
import org.supla.android.tools.UsedFromNativeCode
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString

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

class SuplaRegisterError {
  @JvmField
  var ResultCode: Int = 0

  @UsedFromNativeCode
  constructor()

  internal constructor(err: SuplaRegisterError?) {
    ResultCode = err?.ResultCode ?: 0
  }

  fun codeToString(authDialog: Boolean = false): LocalizedString {
    return when (ResultCode) {
      SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE -> localizedString(R.string.status_temporarily_unavailable)
      SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS ->
        localizedString(if (authDialog) R.string.incorrect_email_or_password else R.string.status_bad_credentials)

      SuplaConst.SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED -> localizedString(R.string.status_climit_exceded)
      SuplaConst.SUPLA_RESULTCODE_CLIENT_DISABLED -> localizedString(R.string.status_device_disabled)
      SuplaConst.SUPLA_RESULTCODE_ACCESSID_DISABLED -> localizedString(R.string.status_accessid_disabled)
      SuplaConst.SUPLA_RESULTCODE_REGISTRATION_DISABLED -> localizedString(R.string.status_reg_disabled)
      SuplaConst.SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED -> localizedString(R.string.status_access_id_not_assigned)
      SuplaConst.SUPLA_RESULTCODE_ACCESSID_INACTIVE -> localizedString(R.string.status_accessid_inactive)
      else -> LocalizedString.WithResourceAndString(R.string.status_unknown_err, " ($ResultCode)")
    }
  }

  fun codeToString(context: Context): String {
    return codeToString(false)(context)
  }
}
