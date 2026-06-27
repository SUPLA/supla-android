@file:Suppress("DEPRECATION")

package org.supla.android.db.room.app.migrations
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

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.data.source.local.entity.LegacyScene
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_ACCESS_ID
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_ACCESS_ID_PASSWORD
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_ACTIVE
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_ADVANCED_MODE
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_AUTH_KEY
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_EMAIL
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_EMAIL_AUTH
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_GUID
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_NAME
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_PREFERRED_PROTOCOL_VERSION
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_SERVER_AUTO_DETECT
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_SERVER_FOR_ACCESS_ID
import org.supla.android.data.source.local.entity.ProfileEntity.Companion.COLUMN_SERVER_FOR_EMAIL
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.view.SceneView
import org.supla.android.db.room.SqlExecutor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration26to27 @Inject constructor() : Migration(26, 27), SqlExecutor {

  override fun migrate(db: SupportSQLiteDatabase) {
    migrateUserProfiles(db)
    migrateScenesDates(db)
  }

  private fun migrateUserProfiles(database: SupportSQLiteDatabase) {
    var validAccountAvailable = false

    getAllProfiles(database).use { cursor ->
      if (cursor.moveToFirst()) {
        do {
          try {
            val profileItem = AuthProfileItem()
            profileItem.assignCursorData(cursor)
            if (profileItem.isAuthDataComplete) {
              validAccountAvailable = true
            }
          } catch (ex: Exception) {
            Timber.e(ex, "Could not migrate profile")
          }
        } while (cursor.moveToNext())
      }
    }

    if (!validAccountAvailable) {
      // There is only empty account in the database which is not needed anymore.
      database.delete(ProfileEntity.TABLE_NAME, null, null)
    }
  }

  private fun getAllProfiles(database: SupportSQLiteDatabase): Cursor =
    database.query("SELECT $PROFILE_ALL_COLUMNS_VERSION_26 FROM ${ProfileEntity.TABLE_NAME}")

  private fun migrateScenesDates(db: SupportSQLiteDatabase) {
    val allScenes = getAllScenes(db)

    db.execSQL("DROP TABLE ${SceneEntity.TABLE_NAME}")
    db.execSQL("DROP VIEW ${SceneView.NAME}")
    execSQL(db, SceneEntity.SQL)
    execSQL(db, SceneView.SQL)

    for (legacyScene in allScenes) {
      db.insert(SceneEntity.TABLE_NAME, CONFLICT_REPLACE, legacyScene.getScene().contentValues)
    }
  }

  private fun getAllScenes(db: SupportSQLiteDatabase): List<LegacyScene> {
    sceneCursor(db).use { cursor ->
      val list = mutableListOf<LegacyScene>()

      if (!cursor.moveToFirst()) {
        return list
      }

      while (!cursor.isAfterLast) {
        val itm = LegacyScene()
        itm.AssignCursorData(cursor)
        list.add(itm)
        cursor.moveToNext()
      }

      return list
    }
  }

  private fun sceneCursor(db: SupportSQLiteDatabase): Cursor =
    db.query("SELECT ${SceneView.ALL_COLUMNS.joinToString(", ")} FROM ${SceneView.NAME}")
}

private const val PROFILE_ALL_COLUMNS_VERSION_26 =
  "$COLUMN_ID,$COLUMN_NAME,$COLUMN_EMAIL,$COLUMN_SERVER_FOR_ACCESS_ID,$COLUMN_SERVER_FOR_EMAIL," +
    "$COLUMN_SERVER_AUTO_DETECT,$COLUMN_EMAIL_AUTH,$COLUMN_ACCESS_ID,$COLUMN_ACCESS_ID_PASSWORD," +
    "$COLUMN_PREFERRED_PROTOCOL_VERSION,$COLUMN_ACTIVE,$COLUMN_ADVANCED_MODE,$COLUMN_GUID,$COLUMN_AUTH_KEY"

private data class AuthProfileItem(
  var id: Long = 0,
  var name: String = "",
  var email: String = "",
  var serverForAccessId: String = "",
  var serverForEmail: String = "",
  var serverAutoDetect: Boolean = true,
  var emailAuth: Boolean = true,
  var accessId: Int = 0,
  var accessIdPassword: String = "",
  var preferredProtocolVersion: Int = 0,
  var advancedAuthSetup: Boolean = false,
  var isActive: Boolean = false,
  var guid: ByteArray = byteArrayOf(),
  var authKey: ByteArray = byteArrayOf()
) {

  fun assignCursorData(cur: Cursor) {
    id = cur.getLong(cur.getColumnIndexOrThrow(COLUMN_ID))
    name = cur.getString(cur.getColumnIndexOrThrow(COLUMN_NAME))
    email = string(cur, cur.getColumnIndexOrThrow(COLUMN_EMAIL))
    serverForAccessId = string(cur, cur.getColumnIndexOrThrow(COLUMN_SERVER_FOR_ACCESS_ID))
    serverAutoDetect = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_SERVER_AUTO_DETECT)) > 0
    emailAuth = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_EMAIL_AUTH)) > 0
    accessId = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_ACCESS_ID))
    accessIdPassword = string(cur, cur.getColumnIndexOrThrow(COLUMN_ACCESS_ID_PASSWORD))
    preferredProtocolVersion = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_PREFERRED_PROTOCOL_VERSION))
    advancedAuthSetup = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_ADVANCED_MODE)) > 0
    isActive = cur.getInt(cur.getColumnIndexOrThrow(COLUMN_ACTIVE)) > 0

    val guidColumnId = cur.getColumnIndexOrThrow(COLUMN_GUID)
    val authKeyColumnId = cur.getColumnIndexOrThrow(COLUMN_AUTH_KEY)
    guid = if (cur.isNull(guidColumnId)) byteArrayOf() else cur.getBlob(guidColumnId)
    authKey = if (cur.isNull(authKeyColumnId)) byteArrayOf() else cur.getBlob(authKeyColumnId)
  }

  val isAuthDataComplete: Boolean
    get() {
      return if (emailAuth) {
        email.isNotEmpty() && (serverAutoDetect || serverForEmail.isNotEmpty())
      } else {
        serverForAccessId.isNotEmpty() && accessId > 0 && accessIdPassword.isNotEmpty()
      }
    }

  private fun stringOrNull(cur: Cursor, idx: Int): String? {
    return if (cur.isNull(idx)) null else cur.getString(idx)
  }

  private fun string(cur: Cursor, idx: Int, default: String = ""): String {
    return stringOrNull(cur, idx) ?: default
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as AuthProfileItem

    if (id != other.id) return false
    if (serverAutoDetect != other.serverAutoDetect) return false
    if (emailAuth != other.emailAuth) return false
    if (accessId != other.accessId) return false
    if (preferredProtocolVersion != other.preferredProtocolVersion) return false
    if (advancedAuthSetup != other.advancedAuthSetup) return false
    if (isActive != other.isActive) return false
    if (name != other.name) return false
    if (email != other.email) return false
    if (serverForAccessId != other.serverForAccessId) return false
    if (serverForEmail != other.serverForEmail) return false
    if (accessIdPassword != other.accessIdPassword) return false
    if (!guid.contentEquals(other.guid)) return false
    if (!authKey.contentEquals(other.authKey)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + serverAutoDetect.hashCode()
    result = 31 * result + emailAuth.hashCode()
    result = 31 * result + accessId
    result = 31 * result + preferredProtocolVersion
    result = 31 * result + advancedAuthSetup.hashCode()
    result = 31 * result + isActive.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + email.hashCode()
    result = 31 * result + serverForAccessId.hashCode()
    result = 31 * result + serverForEmail.hashCode()
    result = 31 * result + accessIdPassword.hashCode()
    result = 31 * result + guid.contentHashCode()
    result = 31 * result + authKey.contentHashCode()
    return result
  }
}
