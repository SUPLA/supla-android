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
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.db.room.SqlExecutor

val MIGRATION_45_46: Migration = object : Migration(45, 46), SqlExecutor {

  private val ALTER_TABLE_SQL =
    "ALTER TABLE ${ChannelValueEntity.TABLE_NAME} ADD ${ChannelValueEntity.COLUMN_LAST_ONLINE_STATE} INTEGER"

  override fun migrate(db: SupportSQLiteDatabase) {
    execSQL(db, ALTER_TABLE_SQL)
  }
}
