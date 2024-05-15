package org.supla.android.data.source.local.entity
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
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.TABLE_NAME
import org.supla.android.profile.AuthInfo

@Entity(
  tableName = TABLE_NAME,
)
data class ProfileEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_NAME) val name: String,
  @ColumnInfo(name = COLUMN_EMAIL) val email: String?,
  @ColumnInfo(name = COLUMN_SERVER_FOR_ACCESS_ID) val serverForAccessId: String?,
  @ColumnInfo(name = COLUMN_SERVER_FOR_EMAIL) val serverForEmail: String?,
  @ColumnInfo(name = COLUMN_SERVER_AUTO_DETECT) val serverAutoDetect: Boolean,
  @ColumnInfo(name = COLUMN_EMAIL_AUTH) val emailAuth: Boolean,
  @ColumnInfo(name = COLUMN_ACCESS_ID) val accessId: Int?,
  @ColumnInfo(name = COLUMN_ACCESS_ID_PASSWORD) val accessIdPassword: String?,
  @ColumnInfo(name = COLUMN_PREFERRED_PROTOCOL_VERSION) val preferredProtocolVersion: Int?,
  @ColumnInfo(name = COLUMN_ACTIVE) val active: Boolean?,
  @ColumnInfo(name = COLUMN_ADVANCED_MODE) val advancedMode: Boolean?,
  @ColumnInfo(name = COLUMN_GUID, typeAffinity = ColumnInfo.BLOB) val guid: ByteArray?,
  @ColumnInfo(name = COLUMN_AUTH_KEY, typeAffinity = ColumnInfo.BLOB) val authKey: ByteArray?,
) {

  val authInfo: AuthInfo
    get() = AuthInfo(
      emailAuth = this.emailAuth,
      serverAutoDetect = this.serverAutoDetect,
      serverForEmail = this.serverForEmail ?: "",
      serverForAccessID = this.serverForAccessId ?: "",
      emailAddress = this.email ?: "",
      accessID = this.accessId ?: 0,
      accessIDpwd = this.accessIdPassword ?: "",
      preferredProtocolVersion = this.preferredProtocolVersion ?: 0,
      guid = this.guid ?: byteArrayOf(),
      authKey = this.authKey ?: byteArrayOf()
    )

  val contentValues: ContentValues
    get() = ContentValues().apply {
      put(COLUMN_NAME, name)
      put(COLUMN_EMAIL, email)
      put(COLUMN_SERVER_FOR_ACCESS_ID, serverForAccessId)
      put(COLUMN_SERVER_FOR_EMAIL, serverForEmail)
      put(COLUMN_SERVER_AUTO_DETECT, serverAutoDetect)
      put(COLUMN_EMAIL_AUTH, emailAuth)
      put(COLUMN_ACCESS_ID, accessId)
      put(COLUMN_ACCESS_ID_PASSWORD, accessIdPassword)
      put(COLUMN_PREFERRED_PROTOCOL_VERSION, preferredProtocolVersion)
      put(COLUMN_ACTIVE, active)
      put(COLUMN_ADVANCED_MODE, advancedMode)
      put(COLUMN_GUID, guid)
      put(COLUMN_AUTH_KEY, authKey)
    }

  companion object {
    const val TABLE_NAME = "auth_profile"
    const val COLUMN_ID = "_auth_profile_id"
    const val COLUMN_NAME = "profile_name"
    const val COLUMN_EMAIL = "email_addr"
    const val COLUMN_SERVER_FOR_ACCESS_ID = "server_addr_access_id"
    const val COLUMN_SERVER_FOR_EMAIL = "server_addr_email"
    const val COLUMN_SERVER_AUTO_DETECT = "server_auto_detect"
    const val COLUMN_EMAIL_AUTH = "email_auth"
    const val COLUMN_ACCESS_ID = "access_id"
    const val COLUMN_ACCESS_ID_PASSWORD = "access_id_pwd"
    const val COLUMN_PREFERRED_PROTOCOL_VERSION = "pref_protcol_ver"
    const val COLUMN_ACTIVE = "is_active"
    const val COLUMN_ADVANCED_MODE = "is_advanced"
    const val COLUMN_GUID = "guid"
    const val COLUMN_AUTH_KEY = "auth_key"

    val SQL = """
      CREATE TABLE $TABLE_NAME
      (
        $COLUMN_ID INTEGER PRIMARY KEY,
        $COLUMN_NAME TEXT NOT NULL UNIQUE,
        $COLUMN_EMAIL TEXT,
        $COLUMN_SERVER_FOR_ACCESS_ID TEXT,
        $COLUMN_SERVER_FOR_EMAIL TEXT,
        $COLUMN_SERVER_AUTO_DETECT INTEGER NOT NULL,
        $COLUMN_EMAIL_AUTH INTEGER NOT NULL,
        $COLUMN_ACCESS_ID INTEGER,
        $COLUMN_ACCESS_ID_PASSWORD TEXT,
        $COLUMN_PREFERRED_PROTOCOL_VERSION INTEGER,
        $COLUMN_ACTIVE INTEGER,
        $COLUMN_ADVANCED_MODE INTEGER,
        $COLUMN_GUID BLOB,
        $COLUMN_AUTH_KEY BLOB
      )
    """.trimIndent()

    val ALL_COLUMNS = arrayOf(
      COLUMN_ID,
      COLUMN_NAME,
      COLUMN_EMAIL,
      COLUMN_SERVER_FOR_ACCESS_ID,
      COLUMN_SERVER_FOR_EMAIL,
      COLUMN_SERVER_AUTO_DETECT,
      COLUMN_EMAIL_AUTH,
      COLUMN_ACCESS_ID,
      COLUMN_ACCESS_ID_PASSWORD,
      COLUMN_PREFERRED_PROTOCOL_VERSION,
      COLUMN_ACTIVE,
      COLUMN_ADVANCED_MODE,
      COLUMN_GUID,
      COLUMN_AUTH_KEY
    )

    const val ALL_COLUMNS_STRING = """
      $COLUMN_ID,
      $COLUMN_NAME,
      $COLUMN_EMAIL,
      $COLUMN_SERVER_FOR_ACCESS_ID,
      $COLUMN_SERVER_FOR_EMAIL,
      $COLUMN_SERVER_AUTO_DETECT,
      $COLUMN_EMAIL_AUTH,
      $COLUMN_ACCESS_ID,
      $COLUMN_ACCESS_ID_PASSWORD,
      $COLUMN_PREFERRED_PROTOCOL_VERSION,
      $COLUMN_ACTIVE,
      $COLUMN_ADVANCED_MODE,
      $COLUMN_GUID,
      $COLUMN_AUTH_KEY
    """

    const val SUBQUERY_ACTIVE = "(SELECT $COLUMN_ID FROM $TABLE_NAME WHERE $COLUMN_ACTIVE = 1)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProfileEntity

    if (id != other.id) return false
    if (name != other.name) return false
    if (email != other.email) return false
    if (serverForAccessId != other.serverForAccessId) return false
    if (serverForEmail != other.serverForEmail) return false
    if (serverAutoDetect != other.serverAutoDetect) return false
    if (emailAuth != other.emailAuth) return false
    if (accessId != other.accessId) return false
    if (accessIdPassword != other.accessIdPassword) return false
    if (preferredProtocolVersion != other.preferredProtocolVersion) return false
    if (active != other.active) return false
    if (advancedMode != other.advancedMode) return false
    if (!guid.contentEquals(other.guid)) return false
    return authKey.contentEquals(other.authKey)
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + (email?.hashCode() ?: 0)
    result = 31 * result + (serverForAccessId?.hashCode() ?: 0)
    result = 31 * result + (serverForEmail?.hashCode() ?: 0)
    result = 31 * result + serverAutoDetect.hashCode()
    result = 31 * result + emailAuth.hashCode()
    result = 31 * result + (accessId ?: 0)
    result = 31 * result + (accessIdPassword?.hashCode() ?: 0)
    result = 31 * result + (preferredProtocolVersion ?: 0)
    result = 31 * result + active.hashCode()
    result = 31 * result + advancedMode.hashCode()
    result = 31 * result + guid.contentHashCode()
    result = 31 * result + authKey.contentHashCode()
    return result
  }
}
