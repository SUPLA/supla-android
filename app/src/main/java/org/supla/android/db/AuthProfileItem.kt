package org.supla.android.db
 
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
import org.supla.android.profile.AuthInfo
import android.database.Cursor
import android.content.ContentValues

data class AuthProfileItem(var name: String = "",
                           var authInfo: AuthInfo,
                           var advancedAuthSetup: Boolean,
                           var isActive: Boolean) : DbItem() {

    override fun AssignCursorData(cur: Cursor) {
        setId(cur.getLong(0))
        fun stringOrNull(cur: Cursor, idx: Int): String? {
            return if(cur.isNull(idx)) null else cur.getString(idx)
        }

        fun string(cur: Cursor, idx: Int, default: String = ""): String {
            val str = stringOrNull(cur, idx)
            return if(str == null) default else str
        }

        name = cur.getString(1)
        authInfo = AuthInfo(emailAddress = string(cur, 2),
                            serverForAccessID = string(cur, 3),
                            serverForEmail = string(cur, 4),
                            serverAutoDetect = cur.getInt(5) > 0,
                            emailAuth = cur.getInt(6) > 0,
                            accessID = cur.getInt(7),
                            accessIDpwd = string(cur, 8),
                            preferredProtocolVersion = cur.getInt(9)
        )
        isActive = cur.getInt(10) > 0
        advancedAuthSetup = cur.getInt(11) > 0
    }

    override fun getContentValues(): ContentValues {
        val vals = ContentValues()
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_PROFILE_NAME, name)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_EMAIL_ADDR, authInfo.emailAddress)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_SERVER_ADDR_ACCESS_ID, authInfo.serverForAccessID)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_SERVER_ADDR_EMAIL, authInfo.serverForEmail)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_SERVER_AUTO_DETECT, if(authInfo.serverAutoDetect) 1 else 0)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_EMAIL_AUTH, if(authInfo.emailAuth) 1 else 0)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_ACCESS_ID, authInfo.accessID)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_ACCESS_ID_PWD, authInfo.accessIDpwd)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_PREFERRED_PROTOCOL_VERSION, authInfo.preferredProtocolVersion)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_IS_ACTIVE, if(isActive) 1 else 0)
        vals.put(SuplaContract.AuthProfileEntry.COLUMN_NAME_IS_ADVANCED_MODE, if(advancedAuthSetup) 1 else 0)

        return vals
    }
}
