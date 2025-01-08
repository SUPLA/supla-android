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

import android.content.ContentValues
import android.database.Cursor
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.profile.AuthInfo

data class AuthProfileItem(
  var name: String = "",
  var authInfo: AuthInfo,
  var advancedAuthSetup: Boolean,
  var isActive: Boolean
) : DbItem() {

  override fun AssignCursorData(cur: Cursor) {
    id = cur.getLong(0)
    fun stringOrNull(cur: Cursor, idx: Int): String? {
      return if (cur.isNull(idx)) null else cur.getString(idx)
    }

    fun string(cur: Cursor, idx: Int, default: String = ""): String {
      return stringOrNull(cur, idx) ?: default
    }

    name = cur.getString(1)
    authInfo = AuthInfo(
      emailAddress = string(cur, 2),
      serverForAccessID = string(cur, 3),
      serverForEmail = string(cur, 4),
      serverAutoDetect = cur.getInt(5) > 0,
      emailAuth = cur.getInt(6) > 0,
      accessID = cur.getInt(7),
      accessIDpwd = string(cur, 8),
      preferredProtocolVersion = cur.getInt(9),
      guid = if (cur.isNull(12)) byteArrayOf() else cur.getBlob(12),
      authKey = if (cur.isNull(13)) byteArrayOf() else cur.getBlob(13)
    )
    isActive = cur.getInt(10) > 0
    advancedAuthSetup = cur.getInt(11) > 0
  }

  fun getContentValuesV22(): ContentValues {
    val vals = ContentValues()
    vals.put(ProfileEntity.COLUMN_NAME, name)
    vals.put(ProfileEntity.COLUMN_EMAIL, authInfo.emailAddress)
    vals.put(ProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID, authInfo.serverForAccessID)
    vals.put(ProfileEntity.COLUMN_SERVER_FOR_EMAIL, authInfo.serverForEmail)
    vals.put(ProfileEntity.COLUMN_SERVER_AUTO_DETECT, if (authInfo.serverAutoDetect) 1 else 0)
    vals.put(ProfileEntity.COLUMN_EMAIL_AUTH, if (authInfo.emailAuth) 1 else 0)
    vals.put(ProfileEntity.COLUMN_ACCESS_ID, authInfo.accessID)
    vals.put(ProfileEntity.COLUMN_ACCESS_ID_PASSWORD, authInfo.accessIDpwd)
    vals.put(ProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION, authInfo.preferredProtocolVersion)
    vals.put(ProfileEntity.COLUMN_ACTIVE, if (isActive) 1 else 0)
    vals.put(ProfileEntity.COLUMN_ADVANCED_MODE, if (advancedAuthSetup) 1 else 0)

    return vals
  }

  override fun getContentValues(): ContentValues {
    val vals = getContentValuesV22()
    vals.put(ProfileEntity.COLUMN_GUID, authInfo.guid)
    vals.put(ProfileEntity.COLUMN_AUTH_KEY, authInfo.authKey)

    return vals
  }

  fun isEmailAuthorizationEnabled(): Boolean {
    return authInfo.emailAuth
  }

  fun authInfoChanged(other: AuthProfileItem): Boolean {
    val a = authInfo
    val b = other.authInfo

    return !(
      a.emailAuth == b.emailAuth && a.emailAddress == b.emailAddress &&
        a.serverAutoDetect == b.serverAutoDetect &&
        a.serverForEmail == b.serverForEmail &&
        a.serverForAccessID == b.serverForAccessID &&
        a.accessID == b.accessID &&
        a.accessIDpwd == b.accessIDpwd
      )
  }
}
