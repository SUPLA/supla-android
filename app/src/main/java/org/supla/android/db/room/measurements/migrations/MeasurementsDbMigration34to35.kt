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
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity.Companion.COLUMN_CHANNEL_ID
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity.Companion.COLUMN_DATE
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity.Companion.COLUMN_HUMIDITY
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity.Companion.COLUMN_ID
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity.Companion.TABLE_NAME
import org.supla.android.db.room.SqlExecutor
import org.supla.android.db.room.measurements.MeasurementsDatabase

val MEASUREMENTS_DB_MIGRATION_34_35: Migration = object : Migration(34, 35), SqlExecutor {

  val CREATE_HUMIDITY_LOG_TABLE_SQLS = arrayOf(
    """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_CHANNEL_ID INTEGER NOT NULL,
          $COLUMN_DATE INTEGER NOT NULL,
          $COLUMN_HUMIDITY REAL NULL,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        );
    """.trimIndent(),
    "CREATE INDEX ${TABLE_NAME}_${COLUMN_CHANNEL_ID}_index ON $TABLE_NAME ($COLUMN_CHANNEL_ID)",
    "CREATE INDEX ${TABLE_NAME}_${COLUMN_DATE}_index ON $TABLE_NAME ($COLUMN_DATE)",
    """
        CREATE UNIQUE INDEX ${TABLE_NAME}_unique_index ON $TABLE_NAME
          ($COLUMN_CHANNEL_ID, $COLUMN_DATE, $COLUMN_PROFILE_ID)
    """.trimIndent()
  )

  override fun getDatabaseNameForLog(): String = MeasurementsDatabase.NAME

  override fun migrate(db: SupportSQLiteDatabase) {
    execSQL(db, CREATE_HUMIDITY_LOG_TABLE_SQLS)
  }
}
