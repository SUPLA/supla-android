package org.supla.android.lib;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

import android.content.Context;

import org.supla.android.R;
import org.supla.android.lib.SuplaConst;

public class SuplaRegisterError {

    public int ResultCode;

    public String codeToString(Context context) {

     int resid = 0;

     switch(ResultCode) {

      case SuplaConst.SUPLA_RESULTCODE_TEMPORARILY_UNAVAILABLE:
       resid = R.string.result_code_temp_unavail;
       break;

      case SuplaConst.SUPLA_RESULTCODE_BAD_CREDENTIALS:
       resid = R.string.result_code_bad_credentals;
       break;

      case SuplaConst.SUPLA_RESULTCODE_CLIENT_LIMITEXCEEDED:
       resid = R.string.result_code_bad_client_limit;
       break;

      case SuplaConst.SUPLA_RESULTCODE_CLIENT_DISABLED:
       resid = R.string.result_code_access_disabled;
       break;

     }

     return resid != 0 ? context.getResources().getString(resid) : new Integer(ResultCode).toString();
    }

}
