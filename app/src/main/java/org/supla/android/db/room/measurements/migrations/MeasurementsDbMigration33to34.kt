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
import org.supla.android.data.source.local.entity.measurements.CurrentHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.data.source.local.entity.measurements.VoltageHistoryLogEntity
import org.supla.android.db.room.SqlExecutor
import org.supla.android.db.room.measurements.MeasurementsDatabase

val MEASUREMENTS_DB_MIGRATION_33_34: Migration = object : Migration(33, 34), SqlExecutor {

  private val ALTER_IC =
    """
    ALTER TABLE ${ImpulseCounterLogEntity.TABLE_NAME} 
    ADD COLUMN ${ImpulseCounterLogEntity.COLUMN_COUNTER_RESET} INTEGER NOT NULL DEFAULT (0)  
    """.trimIndent()

  private val ALTER_EM =
    """
    ALTER TABLE ${ElectricityMeterLogEntity.TABLE_NAME} 
    ADD COLUMN ${ElectricityMeterLogEntity.COLUMN_COUNTER_RESET} INTEGER NOT NULL DEFAULT (0)  
    """.trimIndent()

  private val ALTER_EM_CORRECT_DATE =
    """
      UPDATE ${ElectricityMeterLogEntity.TABLE_NAME} 
      SET ${ElectricityMeterLogEntity.COLUMN_TIMESTAMP} = ${ElectricityMeterLogEntity.COLUMN_TIMESTAMP} * 1000
    """.trimIndent()

  override fun getDatabaseNameForLog(): String = MeasurementsDatabase.NAME

  override fun migrate(db: SupportSQLiteDatabase) {
    execSQL(db, ALTER_IC)
    execSQL(db, ALTER_EM)
    execSQL(db, ALTER_EM_CORRECT_DATE)

    execSQL(db, CurrentHistoryLogEntity.SQL)
    execSQL(db, VoltageHistoryLogEntity.SQL)
  }
}
