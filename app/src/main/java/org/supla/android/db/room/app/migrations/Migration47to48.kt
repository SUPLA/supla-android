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

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import org.supla.android.Encryption
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.model.settings.ProfileCredentials
import org.supla.android.db.room.SqlExecutor
import org.supla.android.di.RANDOM_GENERATOR
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

private const val SCENE_VIEW_NAME = "scene_view"
private const val CHANNEL_VIEW_NAME = "channel_v1"

/**
 * Migrates all profiles to new table without credentials. Credentials are migrated to encrypted preferences.
 * Old profiles table is removed at the end of the migration.
 */
@Singleton
class Migration47to48 @Inject constructor(
  @param:Named(RANDOM_GENERATOR) private val randomGenerator: Random,
  private val encryptedPreferences: EncryptedPreferences,
  @param:ApplicationContext private val context: Context
) : Migration(47, 48), SqlExecutor {

  override fun migrate(db: SupportSQLiteDatabase) {
    silentSql(db, "DROP VIEW $SCENE_VIEW_NAME")
    silentSql(db, "DROP VIEW $CHANNEL_VIEW_NAME")

    val profiles = loadLegacyProfiles(db)
    execSQL(db, CREATE_PROFILES_TABLE_SQL)
    execSQL(db, COPY_PROFILES_DATA_SQL)

    profiles.forEach { profile ->
      try {
        val authorizationData = profile.profileAuthorizationData(context) ?: ProfileCredentials.create(randomGenerator)
        runBlocking {
          encryptedPreferences.setProfileCredentials(profile.id, authorizationData)
        }
      } catch (exception: Exception) {
        Timber.e(exception, "Profile (id: ${profile.id}, name: ${profile.name}) migration failed!")
      }
    }

    execSQL(db, "DROP TABLE ${LegacyProfileEntity.TABLE_NAME}")
  }

  private fun loadLegacyProfiles(db: SupportSQLiteDatabase): List<LegacyProfileEntity> {
    query(db, SELECT_LEGACY_PROFILES_SQL).use { cursor ->
      val legacyProfiles = mutableListOf<LegacyProfileEntity>()
      if (!cursor.moveToFirst()) {
        return legacyProfiles
      }

      do {
        legacyProfiles.add(cursor.toLegacyProfileEntity())
      } while (cursor.moveToNext())

      return legacyProfiles
    }
  }

  private fun Cursor.toLegacyProfileEntity(): LegacyProfileEntity =
    LegacyProfileEntity(
      id = getLong(getColumnIndexOrThrow(LegacyProfileEntity.COLUMN_ID)),
      name = getString(getColumnIndexOrThrow(LegacyProfileEntity.COLUMN_NAME)),
      email = stringOrNull(LegacyProfileEntity.COLUMN_EMAIL),
      serverForAccessId = stringOrNull(LegacyProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID),
      serverForEmail = stringOrNull(LegacyProfileEntity.COLUMN_SERVER_FOR_EMAIL),
      serverAutoDetect = getInt(getColumnIndexOrThrow(LegacyProfileEntity.COLUMN_SERVER_AUTO_DETECT)) > 0,
      emailAuth = getInt(getColumnIndexOrThrow(LegacyProfileEntity.COLUMN_EMAIL_AUTH)) > 0,
      accessId = intOrNull(LegacyProfileEntity.COLUMN_ACCESS_ID),
      accessIdPassword = stringOrNull(LegacyProfileEntity.COLUMN_ACCESS_ID_PASSWORD),
      preferredProtocolVersion = intOrNull(LegacyProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION),
      active = boolOrNull(LegacyProfileEntity.COLUMN_ACTIVE),
      advancedMode = boolOrNull(LegacyProfileEntity.COLUMN_ADVANCED_MODE),
      position = getInt(getColumnIndexOrThrow(LegacyProfileEntity.COLUMN_POSITION)),
      guid = blobOrEmpty(LegacyProfileEntity.COLUMN_GUID),
      authKey = blobOrEmpty(LegacyProfileEntity.COLUMN_AUTH_KEY)
    )

  private fun Cursor.stringOrNull(columnName: String): String? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) null else getString(columnIndex)
  }

  private fun Cursor.intOrNull(columnName: String): Int? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) null else getInt(columnIndex)
  }

  private fun Cursor.boolOrNull(columnName: String): Boolean? {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) null else getInt(columnIndex) > 0
  }

  private fun Cursor.blobOrEmpty(columnName: String): ByteArray {
    val columnIndex = getColumnIndexOrThrow(columnName)
    return if (isNull(columnIndex)) byteArrayOf() else getBlob(columnIndex)
  }
}

private val CREATE_PROFILES_TABLE_SQL =
  """
    CREATE TABLE ${LegacyProfileEntity.NEW_TABLE_NAME} (
      ${LegacyProfileEntity.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
      ${LegacyProfileEntity.COLUMN_NAME} TEXT NOT NULL,
      ${LegacyProfileEntity.COLUMN_EMAIL} TEXT NOT NULL,
      ${LegacyProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID} TEXT NOT NULL,
      ${LegacyProfileEntity.COLUMN_SERVER_FOR_EMAIL} TEXT NOT NULL,
      ${LegacyProfileEntity.COLUMN_SERVER_AUTO_DETECT} INTEGER NOT NULL,
      ${LegacyProfileEntity.COLUMN_EMAIL_AUTH} INTEGER NOT NULL,
      ${LegacyProfileEntity.COLUMN_ACCESS_ID} INTEGER NOT NULL,
      ${LegacyProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION} INTEGER NOT NULL,
      ${LegacyProfileEntity.COLUMN_ACTIVE} INTEGER NOT NULL,
      ${LegacyProfileEntity.COLUMN_ADVANCED_MODE} INTEGER NOT NULL,
      ${LegacyProfileEntity.COLUMN_POSITION} INTEGER NOT NULL DEFAULT 0
    )
  """.trimIndent()

private val COPY_PROFILES_DATA_SQL =
  """
    INSERT INTO ${LegacyProfileEntity.NEW_TABLE_NAME} (
      ${LegacyProfileEntity.COLUMN_ID},
      ${LegacyProfileEntity.COLUMN_NAME},
      ${LegacyProfileEntity.COLUMN_EMAIL},
      ${LegacyProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID},
      ${LegacyProfileEntity.COLUMN_SERVER_FOR_EMAIL},
      ${LegacyProfileEntity.COLUMN_SERVER_AUTO_DETECT},
      ${LegacyProfileEntity.COLUMN_EMAIL_AUTH},
      ${LegacyProfileEntity.COLUMN_ACCESS_ID},
      ${LegacyProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION},
      ${LegacyProfileEntity.COLUMN_ACTIVE},
      ${LegacyProfileEntity.COLUMN_ADVANCED_MODE},
      ${LegacyProfileEntity.COLUMN_POSITION}
    )
    SELECT
      ${LegacyProfileEntity.COLUMN_ID},
      ${LegacyProfileEntity.COLUMN_NAME},
      IFNULL(${LegacyProfileEntity.COLUMN_EMAIL}, ''),
      IFNULL(${LegacyProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID}, ''),
      IFNULL(${LegacyProfileEntity.COLUMN_SERVER_FOR_EMAIL}, ''),
      ${LegacyProfileEntity.COLUMN_SERVER_AUTO_DETECT},
      ${LegacyProfileEntity.COLUMN_EMAIL_AUTH},
      IFNULL(${LegacyProfileEntity.COLUMN_ACCESS_ID}, 0),
      IFNULL(${LegacyProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION}, 0),
      IFNULL(${LegacyProfileEntity.COLUMN_ACTIVE}, 0),
      IFNULL(${LegacyProfileEntity.COLUMN_ADVANCED_MODE}, 0),
      ${LegacyProfileEntity.COLUMN_POSITION}
    FROM ${LegacyProfileEntity.TABLE_NAME}
  """.trimIndent()

private val SELECT_LEGACY_PROFILES_SQL =
  """
  SELECT
    ${LegacyProfileEntity.COLUMN_ID},
    ${LegacyProfileEntity.COLUMN_NAME},
    ${LegacyProfileEntity.COLUMN_EMAIL},
    ${LegacyProfileEntity.COLUMN_SERVER_FOR_ACCESS_ID},
    ${LegacyProfileEntity.COLUMN_SERVER_FOR_EMAIL},
    ${LegacyProfileEntity.COLUMN_SERVER_AUTO_DETECT},
    ${LegacyProfileEntity.COLUMN_EMAIL_AUTH},
    ${LegacyProfileEntity.COLUMN_ACCESS_ID},
    ${LegacyProfileEntity.COLUMN_ACCESS_ID_PASSWORD},
    ${LegacyProfileEntity.COLUMN_PREFERRED_PROTOCOL_VERSION},
    ${LegacyProfileEntity.COLUMN_ACTIVE},
    ${LegacyProfileEntity.COLUMN_ADVANCED_MODE},
    ${LegacyProfileEntity.COLUMN_POSITION},
    ${LegacyProfileEntity.COLUMN_GUID},
    ${LegacyProfileEntity.COLUMN_AUTH_KEY}
  FROM ${LegacyProfileEntity.TABLE_NAME}
  """.trimIndent()

private data class LegacyProfileEntity(
  val id: Long,
  val name: String,
  val email: String?,
  val serverForAccessId: String?,
  val serverForEmail: String?,
  val serverAutoDetect: Boolean,
  val emailAuth: Boolean,
  val accessId: Int?,
  val accessIdPassword: String?,
  val preferredProtocolVersion: Int?,
  val active: Boolean?,
  val advancedMode: Boolean?,
  val position: Int,
  val guid: ByteArray,
  val authKey: ByteArray
) {

  fun profileAuthorizationData(context: Context): ProfileCredentials? {
    try {
      val guid = decrypt(guid, context)
      val authKey = decrypt(authKey, context)

      if (guid != null && guid.isNotEmpty() && authKey != null && authKey.isNotEmpty()) {
        return ProfileCredentials(
          accessIdPassword = accessIdPassword ?: "",
          guid = guid,
          authKey = authKey
        )
      }
    } catch (ex: Exception) {
      Timber.e(ex, "Could not get guid and auth key")
    }

    return null
  }

  companion object {
    const val TABLE_NAME = "auth_profile"
    const val NEW_TABLE_NAME = "profiles"
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
    const val COLUMN_POSITION = "position"
    const val COLUMN_GUID = "guid"
    const val COLUMN_AUTH_KEY = "auth_key"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as LegacyProfileEntity

    if (id != other.id) return false
    if (serverAutoDetect != other.serverAutoDetect) return false
    if (emailAuth != other.emailAuth) return false
    if (accessId != other.accessId) return false
    if (preferredProtocolVersion != other.preferredProtocolVersion) return false
    if (active != other.active) return false
    if (advancedMode != other.advancedMode) return false
    if (position != other.position) return false
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
    result = 31 * result + (accessId ?: 0)
    result = 31 * result + (preferredProtocolVersion ?: 0)
    result = 31 * result + (active?.hashCode() ?: 0)
    result = 31 * result + (advancedMode?.hashCode() ?: 0)
    result = 31 * result + position
    result = 31 * result + name.hashCode()
    result = 31 * result + (email?.hashCode() ?: 0)
    result = 31 * result + (serverForAccessId?.hashCode() ?: 0)
    result = 31 * result + (serverForEmail?.hashCode() ?: 0)
    result = 31 * result + (accessIdPassword?.hashCode() ?: 0)
    result = 31 * result + guid.contentHashCode()
    result = 31 * result + authKey.contentHashCode()
    return result
  }
}

private fun decrypt(payload: ByteArray, context: Context): ByteArray? {
  val key = getDeviceID(context)
  return Encryption.decryptDataWithNullOnException(payload, key)
}

@SuppressLint("HardwareIds")
@Suppress("DEPRECATION")
private fun getDeviceID(ctx: Context): String {
  var id: String? = null
  try {
    id = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      android.provider.Settings.Secure.getString(ctx.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
    } else {
      android.os.Build.SERIAL
    }

    id += "-" + android.os.Build.BOARD + "-" + android.os.Build.BRAND + "-" + android.os.Build.DEVICE + "-" + android.os.Build.HARDWARE
  } catch (e: Exception) {
    Timber.e(e, "getDeviceID error")
  }

  return id ?: "unknown"
}
