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
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.view.SceneView
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.room.SqlExecutor
import org.supla.android.profile.AuthInfo
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
            val profile: AuthProfileItem = makeEmptyAuthItem()
            profile.AssignCursorData(cursor)
            if (profile.authInfo.isAuthDataComplete) {
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
    database.query("SELECT ${ProfileEntity.ALL_COLUMNS.joinToString(", ")} FROM ${ProfileEntity.TABLE_NAME}")

  private fun makeEmptyAuthItem(): AuthProfileItem {
    return AuthProfileItem(
      name = "",
      authInfo = AuthInfo(
        emailAuth = true,
        serverAutoDetect = true,
        serverForEmail = "",
        serverForAccessID = "",
        emailAddress = "",
        accessID = 0,
        accessIDpwd = "",
        preferredProtocolVersion = 0,
        guid = byteArrayOf(0),
        authKey = byteArrayOf(0)
      ),
      advancedAuthSetup = false,
      isActive = false,
      position = 0
    )
  }

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
