package org.supla.android.db.room.measurements.migrations
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
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.room.SqlExecutor

val MEASUREMENTS_DB_MIGRATION_35_36: Migration = object : Migration(35, 36), SqlExecutor {

  override fun getDatabaseNameForLog(): String = MeasurementsDbHelper.DATABASE_NAME

  override fun migrate(db: SupportSQLiteDatabase) {
    execSQL(db, "DROP VIEW IF EXISTS ic_log_v1")
    execSQL(db, "DROP VIEW IF EXISTS em_log_v1")
  }
}
