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
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity
import org.supla.android.data.source.local.entity.AndroidAutoItemEntity.Companion.COLUMN_ORDER
import org.supla.android.db.room.SqlExecutor

val MIGRATION_38_39: Migration = object : Migration(38, 39), SqlExecutor {

  private val ALTER_ANDROID_AUTO_ITEM_TABLE_SQL =
    "ALTER TABLE ${AndroidAutoItemEntity.TABLE_NAME} ADD COLUMN $COLUMN_ORDER INTEGER NOT NULL DEFAULT 0"

  override fun migrate(db: SupportSQLiteDatabase) {
    createAndroidAutoItemTable(db)
  }

  private fun createAndroidAutoItemTable(db: SupportSQLiteDatabase) {
    execSQL(db, ALTER_ANDROID_AUTO_ITEM_TABLE_SQL)
  }
}
