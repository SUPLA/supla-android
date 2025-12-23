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
import org.supla.android.data.source.local.entity.ColorEntity
import org.supla.android.data.source.local.entity.ColorEntity.Companion.CREATE_TYPE_INDEX
import org.supla.android.db.room.SqlExecutor

val MIGRATION_41_42: Migration = object : Migration(41, 42), SqlExecutor {

  private val ALTER_COLOR_TABLE_SQL =
    "ALTER TABLE ${ColorEntity.TABLE_NAME} ADD ${ColorEntity.COLUMN_TYPE} INTEGER NOT NULL DEFAULT 0"

  override fun migrate(db: SupportSQLiteDatabase) {
    createWidgetConfigurationTable(db)
  }

  private fun createWidgetConfigurationTable(db: SupportSQLiteDatabase) {
    execSQL(db, ALTER_COLOR_TABLE_SQL)
    execSQL(db, CREATE_TYPE_INDEX)
  }
}
