package org.supla.android.profile
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

import org.supla.android.db.DbItem

data class AuthInfo(var emailAuth: Boolean, 
                    var serverAutoDetect: Boolean,
                    var serverForEmail: String = "",
                    var serverForAccessID: String = "",
                    var emailAddress: String = "",
                    var accessID: Int = 0,
                    var accessIDpwd: String = "",
                    var preferredProtocolVersion: Int = 0) {

    /**
     Returns server used for current authentication method
     */
    val serverForCurrentAuthMethod: String 
        get() = if(emailAuth) serverForEmail else serverForAccessID

    /**
     A flag indicating if current authentication settings
     are complete (but not necessarily correct).
     */
    val isAuthDataComplete: Boolean 
        get() {
            if(emailAuth) {
                return !emailAddress.isEmpty() &&
                (serverAutoDetect || !serverForCurrentAuthMethod.isEmpty())
            } else {
                return !serverForCurrentAuthMethod.isEmpty() &&
                    accessID > 0 && !accessIDpwd.isEmpty()
            }
        }
}
