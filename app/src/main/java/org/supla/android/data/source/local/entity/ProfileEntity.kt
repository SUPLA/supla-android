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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.TABLE_NAME

@Entity(
  tableName = TABLE_NAME,
)
data class ProfileEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey(autoGenerate = true) val id: Long,
  @ColumnInfo(name = COLUMN_NAME) val name: String,
  @ColumnInfo(name = COLUMN_EMAIL) val email: String,
  @ColumnInfo(name = COLUMN_SERVER_FOR_ACCESS_ID) val serverForAccessId: String,
  @ColumnInfo(name = COLUMN_SERVER_FOR_EMAIL) val serverForEmail: String,
  @ColumnInfo(name = COLUMN_SERVER_AUTO_DETECT) val serverAutoDetect: Boolean,
  @ColumnInfo(name = COLUMN_EMAIL_AUTH) val emailAuth: Boolean,
  @ColumnInfo(name = COLUMN_ACCESS_ID) val accessId: Int,
  @ColumnInfo(name = COLUMN_PREFERRED_PROTOCOL_VERSION) val preferredProtocolVersion: Int,
  @ColumnInfo(name = COLUMN_ACTIVE) val active: Boolean,
  @ColumnInfo(name = COLUMN_ADVANCED_MODE) val advancedMode: Boolean,
  @ColumnInfo(name = COLUMN_POSITION, defaultValue = "0") val position: Int
) {
  /**
   Returns server used for current authentication method
   */
  val serverForCurrentAuthMethod: String
    get() = if (emailAuth) serverForEmail else serverForAccessId

  val serverUrlString: String
    get() = "https://$serverForCurrentAuthMethod"

  @Ignore
  val isCloudAccount = serverForEmail.contains(".supla.org")

  companion object {
    const val TABLE_NAME = "profiles"
    const val COLUMN_ID = "_auth_profile_id"
    const val COLUMN_NAME = "profile_name"
    const val COLUMN_EMAIL = "email_addr"
    const val COLUMN_SERVER_FOR_ACCESS_ID = "server_addr_access_id"
    const val COLUMN_SERVER_FOR_EMAIL = "server_addr_email"
    const val COLUMN_SERVER_AUTO_DETECT = "server_auto_detect"
    const val COLUMN_EMAIL_AUTH = "email_auth"
    const val COLUMN_ACCESS_ID = "access_id"
    const val COLUMN_PREFERRED_PROTOCOL_VERSION = "pref_protcol_ver"
    const val COLUMN_ACTIVE = "is_active"
    const val COLUMN_ADVANCED_MODE = "is_advanced"
    const val COLUMN_POSITION = "position"

    val ALL_COLUMNS = arrayOf(
      COLUMN_ID,
      COLUMN_NAME,
      COLUMN_EMAIL,
      COLUMN_SERVER_FOR_ACCESS_ID,
      COLUMN_SERVER_FOR_EMAIL,
      COLUMN_SERVER_AUTO_DETECT,
      COLUMN_EMAIL_AUTH,
      COLUMN_ACCESS_ID,
      COLUMN_PREFERRED_PROTOCOL_VERSION,
      COLUMN_ACTIVE,
      COLUMN_ADVANCED_MODE,
      COLUMN_POSITION
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
      $COLUMN_PREFERRED_PROTOCOL_VERSION,
      $COLUMN_ACTIVE,
      $COLUMN_ADVANCED_MODE,
      $COLUMN_POSITION
    """

    const val SUBQUERY_ACTIVE = "(SELECT $COLUMN_ID FROM $TABLE_NAME WHERE $COLUMN_ACTIVE = 1)"

    const val JOIN_COLUMNS =
      """
        profile.$COLUMN_ID profile_$COLUMN_ID,
        profile.$COLUMN_NAME profile_$COLUMN_NAME,
        profile.$COLUMN_EMAIL profile_$COLUMN_EMAIL,
        profile.$COLUMN_SERVER_FOR_ACCESS_ID profile_$COLUMN_SERVER_FOR_ACCESS_ID,
        profile.$COLUMN_SERVER_FOR_EMAIL profile_$COLUMN_SERVER_FOR_EMAIL,
        profile.$COLUMN_SERVER_AUTO_DETECT profile_$COLUMN_SERVER_AUTO_DETECT,
        profile.$COLUMN_EMAIL_AUTH profile_$COLUMN_EMAIL_AUTH,
        profile.$COLUMN_ACCESS_ID profile_$COLUMN_ACCESS_ID,
        profile.$COLUMN_PREFERRED_PROTOCOL_VERSION profile_$COLUMN_PREFERRED_PROTOCOL_VERSION,
        profile.$COLUMN_ACTIVE profile_$COLUMN_ACTIVE,
        profile.$COLUMN_ADVANCED_MODE profile_$COLUMN_ADVANCED_MODE,
        profile.$COLUMN_POSITION profile_$COLUMN_POSITION
      """
  }
}
