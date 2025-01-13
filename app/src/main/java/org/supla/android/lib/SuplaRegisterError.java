package org.supla.android.lib;

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

import android.content.Context;
import org.supla.android.R;
import org.supla.android.tools.UsedFromNativeCode;

public class SuplaRegisterError {

  public int ResultCode;

  @UsedFromNativeCode
  public SuplaRegisterError() {
    // This constructor is used by native code
  }

  SuplaRegisterError(SuplaRegisterError err) {
    if (err != null) {
      ResultCode = err.ResultCode;
    }
  }

  public String codeToString(Context context, boolean authDialog) {

    switch (ResultCode) {
      case SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE:
        return context.getResources().getString(R.string.status_temporarily_unavailable);
      case SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS:
        return context
            .getResources()
            .getString(
                authDialog
                    ? R.string.incorrect_email_or_password
                    : R.string.status_bad_credentials);
      case SuplaConst.SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED:
        return context.getResources().getString(R.string.status_climit_exceded);
      case SuplaConst.SUPLA_RESULTCODE_CLIENT_DISABLED:
        return context.getResources().getString(R.string.status_device_disabled);
      case SuplaConst.SUPLA_RESULTCODE_ACCESSID_DISABLED:
        return context.getResources().getString(R.string.status_accessid_disabled);
      case SuplaConst.SUPLA_RESULTCODE_REGISTRATION_DISABLED:
        return context.getResources().getString(R.string.status_reg_disabled);
      case SuplaConst.SUPLA_RESULTCODE_ACCESSID_NOT_ASSIGNED:
        return context.getResources().getString(R.string.status_access_id_not_assigned);
      case SuplaConst.SUPLA_RESULTCODE_ACCESSID_INACTIVE:
        return context.getResources().getString(R.string.status_accessid_inactive);
      default:
        return context.getResources().getString(R.string.status_unknown_err)
            + " ("
            + ResultCode
            + ")";
    }
  }

  public String codeToString(Context context) {
    return codeToString(context, false);
  }
}
