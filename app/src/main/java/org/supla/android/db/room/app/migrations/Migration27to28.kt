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
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity.Companion.COLUMN_TIMER_START_TIME
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity.Companion.TABLE_NAME
import org.supla.android.data.source.local.view.ChannelView
import org.supla.android.db.room.SqlExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Migration27to28 @Inject constructor() : Migration(27, 28), SqlExecutor {
  override fun migrate(database: SupportSQLiteDatabase) {
    val sql = "ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_TIMER_START_TIME INTEGER"
    execSQL(database, sql)
    execSQL(database, "DROP VIEW IF EXISTS " + ChannelView.NAME)
    execSQL(database, ChannelView.SQL)
  }
}
