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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.db.room.SqlExecutor

val MIGRATION_33_34: Migration = object : Migration(33, 34), SqlExecutor {

  val CREATE_NOTIFICATION_TABLE = """
      CREATE TABLE ${NotificationEntity.TABLE_NAME}
      (
        ${NotificationEntity.COLUMN_ID} INTEGER NOT NULL PRIMARY KEY,
        ${NotificationEntity.COLUMN_TITLE} TEXT NOT NULL,
        ${NotificationEntity.COLUMN_MESSAGE} TEXT NOT NULL,
        ${NotificationEntity.COLUMN_PROFILE_NAME} TEXT,
        ${NotificationEntity.COLUMN_DATE} INTEGER NOT NULL
      )
  """.trimIndent()

  override fun migrate(database: SupportSQLiteDatabase) {
    execSQL(database, "DROP TABLE ${NotificationEntity.TABLE_NAME}")
    execSQL(database, CREATE_NOTIFICATION_TABLE)
  }
}
