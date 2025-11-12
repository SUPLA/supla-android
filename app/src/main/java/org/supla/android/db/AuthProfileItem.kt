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
  var isActive: Boolean,
  var position: Int
) : DbItem() {

  override fun AssignCursorData(cur: Cursor) {
    id = cur.getLong(0)
    fun stringOrNull(cur: Cursor, idx: Int): String? {
      return if (cur.isNull(idx)) null else cur.getString(idx)
    }

    fun string(cur: Cursor, idx: Int, default: String = ""): String {
      return stringOrNull(cur, idx) ?: default
    }

    val guidColumnId = cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_GUID)
    val authKeyColumnId = cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_AUTH_KEY)

    name = cur.getString(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_NAME))
    authInfo = AuthInfo(
      emailAddress = string(cur, cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_EMAIL)),
      serverForAccessID = string(cur, cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID)),
      serverForEmail = string(cur, cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_SERVER_FOR_EMAIL)),
      serverAutoDetect = cur.getInt(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_SERVER_AUTO_DETECT)) > 0,
      emailAuth = cur.getInt(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_EMAIL_AUTH)) > 0,
      accessID = cur.getInt(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_ACCESS_ID)),
      accessIDpwd = string(cur, cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_ACCESS_ID_PASSWORD)),
      preferredProtocolVersion = cur.getInt(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION)),
      guid = if (cur.isNull(guidColumnId)) byteArrayOf() else cur.getBlob(guidColumnId),
      authKey = if (cur.isNull(authKeyColumnId)) byteArrayOf() else cur.getBlob(authKeyColumnId)
    )
    isActive = cur.getInt(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_ACTIVE)) > 0
    advancedAuthSetup = cur.getInt(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_ADVANCED_MODE)) > 0
    position = cur.getInt(cur.getColumnIndexOrThrow(ProfileEntity.COLUMN_POSITION))
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
    vals.put(ProfileEntity.COLUMN_POSITION, position)
    vals.put(ProfileEntity.COLUMN_GUID, authInfo.guid)
    vals.put(ProfileEntity.COLUMN_AUTH_KEY, authInfo.authKey)

    return vals
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
