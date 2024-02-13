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
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_CHANNEL_RELATION_TYPE
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_DELETE_FLAG
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_PARENT_ID
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelRelationEntity.Companion.TABLE_NAME
import org.supla.android.db.room.SqlExecutor

val MIGRATION_28_29: Migration = object : Migration(28, 29), SqlExecutor {

  private val CREATE_CHANNEL_RELATION_TABLE_SQL =
    """
      CREATE TABLE $TABLE_NAME 
      (
        $COLUMN_CHANNEL_ID INTEGER NOT NULL,
        $COLUMN_PARENT_ID INTEGER NOT NULL,
        $COLUMN_CHANNEL_RELATION_TYPE INTEGER NOT NULL,
        $COLUMN_PROFILE_ID INTEGER NOT NULL,
        $COLUMN_DELETE_FLAG INTEGER NOT NULL,
        PRIMARY KEY ($COLUMN_CHANNEL_ID, $COLUMN_PARENT_ID, $COLUMN_PROFILE_ID)
      )
    """.trimIndent()

  override fun migrate(database: SupportSQLiteDatabase) {
    createChannelRelationTable(database)
  }

  private fun createChannelRelationTable(db: SupportSQLiteDatabase) {
    execSQL(db, CREATE_CHANNEL_RELATION_TABLE_SQL)
  }
}
