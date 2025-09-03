package org.supla.android.db.room.app
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

import android.database.sqlite.SQLiteDatabase
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.Trace
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.local.view.ChannelView
import org.supla.android.data.source.local.view.SceneView
import org.supla.android.db.room.SqlExecutor
import org.supla.android.db.room.app.migrations.CHANNEL_GROUP_VALUE_VIEW_NAME
import org.supla.android.extensions.TAG
import org.supla.android.profile.ProfileMigrator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDatabaseCallback @Inject constructor(
  private val profileMigrator: ProfileMigrator
) : RoomDatabase.Callback(), SqlExecutor {

  private var destructivelyMigrated = false

  override fun onCreate(db: SupportSQLiteDatabase) {
    execSQL(db, SceneView.SQL)
    execSQL(db, ChannelView.SQL)
  }

  override fun onOpen(db: SupportSQLiteDatabase) {
    if (!destructivelyMigrated) {
      return
    }
    Trace.i(TAG, "Destructively migrated - trying to restore profile")

    val profile = profileMigrator.makeProfileUsingPreferences() ?: return
    try {
      db.insert(ProfileEntity.TABLE_NAME, SQLiteDatabase.CONFLICT_IGNORE, profile.contentValues)
      Trace.i(TAG, "Destructively migrated - profile restored")
    } catch (exception: Exception) {
      Trace.w(TAG, "Profile restore failed - ${exception.message}", exception)
    }
  }

  override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
    destructivelyMigrated = true

    silentSql(db, "DROP VIEW ${SceneView.NAME}")
    silentSql(db, "DROP VIEW $CHANNEL_GROUP_VALUE_VIEW_NAME")
    silentSql(db, "DROP VIEW ${ChannelView.NAME}")
    execSQL(db, SceneView.SQL)
    execSQL(db, ChannelView.SQL)
  }

  private fun silentSql(db: SupportSQLiteDatabase, sqlString: String) {
    try {
      execSQL(db, sqlString)
    } catch (exception: Exception) {
      Trace.w(TAG, "Failed by `$sqlString` - ${exception.message}", exception)
    }
  }
}
