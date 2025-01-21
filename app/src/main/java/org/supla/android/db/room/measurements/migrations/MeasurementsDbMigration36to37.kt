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
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.local.entity.measurements.CurrentHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.ElectricityMeterLogEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeasurementEntity
import org.supla.android.data.source.local.entity.measurements.GeneralPurposeMeterEntity
import org.supla.android.data.source.local.entity.measurements.HomePlusThermostatLogEntity
import org.supla.android.data.source.local.entity.measurements.HumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.ImpulseCounterLogEntity
import org.supla.android.data.source.local.entity.measurements.PowerActiveHistoryLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureAndHumidityLogEntity
import org.supla.android.data.source.local.entity.measurements.TemperatureLogEntity
import org.supla.android.data.source.local.entity.measurements.VoltageHistoryLogEntity
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.room.SqlExecutor
import org.supla.android.extensions.TAG
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_MIGRATION_TIME = 10_000 // 10 seconds

@Singleton
class MeasurementsDbMigration36to37 @Inject constructor(
  private val dateProvider: DateProvider
) : Migration(36, 37), SqlExecutor {

  override fun getDatabaseNameForLog(): String = MeasurementsDbHelper.DATABASE_NAME

  private val tables: List<Triple<String, String, String>> = listOf(
    Triple(CurrentHistoryLogEntity.TABLE_NAME, CurrentHistoryLogEntity.COLUMN_GROUPING_STRING, CurrentHistoryLogEntity.COLUMN_TIMESTAMP),
    Triple(
      ElectricityMeterLogEntity.TABLE_NAME,
      ElectricityMeterLogEntity.COLUMN_GROUPING_STRING,
      ElectricityMeterLogEntity.COLUMN_TIMESTAMP
    ),
    Triple(
      GeneralPurposeMeasurementEntity.TABLE_NAME,
      GeneralPurposeMeasurementEntity.COLUMN_GROUPING_STRING,
      GeneralPurposeMeasurementEntity.COLUMN_DATE
    ),
    Triple(GeneralPurposeMeterEntity.TABLE_NAME, GeneralPurposeMeterEntity.COLUMN_GROUPING_STRING, GeneralPurposeMeterEntity.COLUMN_DATE),
    Triple(
      HomePlusThermostatLogEntity.TABLE_NAME,
      HomePlusThermostatLogEntity.COLUMN_GROUPING_STRING,
      HomePlusThermostatLogEntity.COLUMN_TIMESTAMP
    ),
    Triple(ImpulseCounterLogEntity.TABLE_NAME, ImpulseCounterLogEntity.COLUMN_GROUPING_STRING, ImpulseCounterLogEntity.COLUMN_TIMESTAMP),
    Triple(
      TemperatureAndHumidityLogEntity.TABLE_NAME,
      TemperatureAndHumidityLogEntity.COLUMN_GROUPING_STRING,
      TemperatureAndHumidityLogEntity.COLUMN_TIMESTAMP
    ),
    Triple(TemperatureLogEntity.TABLE_NAME, TemperatureLogEntity.COLUMN_GROUPING_STRING, TemperatureLogEntity.COLUMN_TIMESTAMP),
    Triple(VoltageHistoryLogEntity.TABLE_NAME, VoltageHistoryLogEntity.COLUMN_GROUPING_STRING, VoltageHistoryLogEntity.COLUMN_TIMESTAMP),
    Triple(HumidityLogEntity.TABLE_NAME, HumidityLogEntity.COLUMN_GROUPING_STRING, HumidityLogEntity.COLUMN_DATE)
  )

  override fun migrate(db: SupportSQLiteDatabase) {
    execSQL(db, PowerActiveHistoryLogEntity.SQL)

    val migrationStart = dateProvider.currentTimestamp()
    var migrateData = true

    tables.forEach { (table, groupingColumn, dateColumn) ->
      execSQL(db, alterString(table, groupingColumn))

      if (migrateData) {
        try {
          execSQL(db, updateString(table, groupingColumn, dateColumn))
        } catch (exception: Exception) {
          Trace.e(TAG, "Data migration in $table failed!", exception)

          try {
            execSQL(db, "DELETE FROM $table")
          } catch (deleteException: Exception) {
            Trace.e(TAG, "Deletion after failure in migration in $table failed!", deleteException)
          }
        }
      } else {
        try {
          execSQL(db, "DELETE FROM $table")
        } catch (exception: Exception) {
          Trace.e(TAG, "Deletion after failure in migration in $table failed!", exception)
        }
      }

      if (migrateData && dateProvider.currentTimestamp() - migrationStart > MAX_MIGRATION_TIME) {
        migrateData = false
      }
    }
  }

  private fun alterString(tableName: String, columnName: String): String =
    "ALTER TABLE $tableName ADD COLUMN $columnName TEXT NOT NULL DEFAULT ''"

  private fun updateString(tableName: String, groupingColumn: String, dateColumn: String): String =
    """
      UPDATE $tableName 
      SET $groupingColumn = 
        STRFTIME('%Y%m%d%H%M', DATETIME($dateColumn/1000, 'unixepoch')) || CASE (STRFTIME('%w', DATETIME($dateColumn/1000, 'unixepoch'))) 
          WHEN '1' THEN '1' 
          WHEN '2' THEN '2' 
          WHEN '3' THEN '3' 
          WHEN '4' THEN '4' 
          WHEN '5' THEN '5' 
          WHEN '6' THEN '6' 
          ELSE '7' 
        END
    """.trimIndent()
}
