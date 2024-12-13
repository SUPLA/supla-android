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

public class SuplaConnError {

  public int Code;

  public SuplaConnError() {
    // This constructor is used by native code
  }

  public SuplaConnError(int Code) {
    this.Code = Code;
  }

  public SuplaConnError(SuplaConnError error) {
    if (error != null) {
      Code = error.Code;
    }
  }

  public String codeToString(Context context) {

    int resId =
        switch (Code) {
          case SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND -> R.string.err_hostnotfound;
          case SuplaConst.SUPLA_RESULT_CANT_CONNECT_TO_HOST -> R.string.err_cantconnecttohost;
          default -> 0;
        };

    return resId != 0 ? context.getResources().getString(resId) : Integer.valueOf(Code).toString();
  }
}
