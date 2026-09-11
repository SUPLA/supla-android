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
import org.supla.android.data.source.local.entity.ChannelStateEntity
import org.supla.android.data.source.local.entity.ChannelStateEntity.Companion.COLUMN_BATTERY_STATE
import org.supla.android.db.room.SqlExecutor

val MIGRATION_49_50: Migration = object : Migration(49, 50), SqlExecutor {

  private val ALTER_CHANNEL_STATE_TABLE_SQL =
    "ALTER TABLE ${ChannelStateEntity.TABLE_NAME} ADD COLUMN $COLUMN_BATTERY_STATE INTEGER DEFAULT NULL"

  override fun migrate(db: SupportSQLiteDatabase) {
    execSQL(db, ALTER_CHANNEL_STATE_TABLE_SQL)
  }
}
